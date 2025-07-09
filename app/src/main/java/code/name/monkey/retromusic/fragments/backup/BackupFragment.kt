package code.name.monkey.retromusic.fragments.backup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.backup.BackupAdapter
import code.name.monkey.retromusic.databinding.FragmentBackupBinding
import code.name.monkey.retromusic.extensions.accentColor
import code.name.monkey.retromusic.extensions.accentOutlineColor
import code.name.monkey.retromusic.extensions.materialDialog
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.helper.BackupHelper
import code.name.monkey.retromusic.util.Share
import com.afollestad.materialdialogs.input.input
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BackupFragment : Fragment(R.layout.fragment_backup), BackupAdapter.BackupClickedListener {

    private val backupViewModel by viewModels<BackupViewModel>()
    private var backupAdapter: BackupAdapter? = null

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                BackupHelper.createBackup(requireContext(), uri = it)
                backupViewModel.loadBackups()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBackupBinding.bind(view)
        initAdapter()
        setupRecyclerview()

        backupViewModel.backupsLiveData.observe(viewLifecycleOwner) {
            backupAdapter?.swapDataset(it.ifEmpty { listOf() })
        }

        backupViewModel.loadBackups()

        val openFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            lifecycleScope.launch(Dispatchers.IO) {
                it?.let {
                    startActivity(Intent(context, RestoreActivity::class.java).apply {
                        data = it
                    })
                }
            }
        }

        binding.createBackup.accentOutlineColor()
        binding.restoreBackup.accentColor()

        binding.createBackup.setOnClickListener {
            val name = BackupHelper.getTimeStamp() + BackupHelper.APPEND_EXTENSION
            createDocumentLauncher.launch(name)
        }

        binding.restoreBackup.setOnClickListener {
            openFilePicker.launch(arrayOf("application/octet-stream"))
        }
    }

    private fun initAdapter() {
        backupAdapter = BackupAdapter(requireActivity(), ArrayList(), this)
        backupAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun checkIsEmpty() {
        val isEmpty = backupAdapter?.itemCount == 0
        binding.backupTitle.isVisible = !isEmpty
        binding.backupRecyclerview.isVisible = !isEmpty
    }

    private fun setupRecyclerview() {
        binding.backupRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = backupAdapter
        }
    }

    override fun onBackupClicked(file: File) {
        lifecycleScope.launch {
            startActivity(Intent(context, RestoreActivity::class.java).apply {
                data = file.toUri()
            })
        }
    }

    override fun onBackupMenuClicked(file: File, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_delete -> {
                try {
                    file.delete()
                } catch (e: SecurityException) {
                    showToast(R.string.error_delete_backup)
                }
                backupViewModel.loadBackups()
                return true
            }
            R.id.action_share -> {
                Share.shareFile(requireContext(), file, "*/*")
                return true
            }
            R.id.action_rename -> {
                materialDialog().show {
                    title(res = R.string.action_rename)
                    input(prefill = file.nameWithoutExtension) { _, text ->
                        val renamedFile = File(file.parent, "$text${BackupHelper.APPEND_EXTENSION}")
                        if (!renamedFile.exists()) {
                            file.renameTo(renamedFile)
                            backupViewModel.loadBackups()
                        } else {
                            showToast(R.string.file_already_exists)
                        }
                    }
                    positiveButton(android.R.string.ok)
                    negativeButton(R.string.action_cancel)
                }
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
