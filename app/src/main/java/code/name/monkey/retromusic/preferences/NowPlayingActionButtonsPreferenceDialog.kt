package code.name.monkey.retromusic.preferences

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.ListItemActionButtonReorderableBinding // Use the correct binding
import code.name.monkey.retromusic.databinding.PreferenceDialogLibraryCategoriesBinding // Keep for the dialog layout
import code.name.monkey.retromusic.extensions.colorButtons
import code.name.monkey.retromusic.extensions.materialDialog
import code.name.monkey.retromusic.util.PreferenceUtil

// Data class to represent a Now Playing action button
data class NowPlayingActionButton(
    val id: Int, // Resource ID of the menu item
    val name: String, // Display name of the button
    var visible: Boolean // Whether the button is visible
)

class NowPlayingActionButtonsPreferenceDialog : DialogFragment() {

    private var _binding: PreferenceDialogLibraryCategoriesBinding? = null // Reuse for now
    private val binding get() = _binding!!

    private lateinit var actionButtonAdapter: NowPlayingActionButtonAdapter

    // Define the available action buttons with default visibility
    private val defaultActionButtons = listOf(
        NowPlayingActionButton(R.id.action_sleep_timer, "Sleep Timer", true),
        NowPlayingActionButton(R.id.action_toggle_lyrics, "Lyrics", true),
        NowPlayingActionButton(R.id.action_toggle_favorite, "Favorite", true),
        NowPlayingActionButton(R.id.now_playing, "Now Playing Queue", true)
        // Options menu visibility will be handled separately
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = PreferenceDialogLibraryCategoriesBinding.inflate(layoutInflater) // Reuse dialog layout

        // Load the current order and visibility from preferences
        val currentButtonOrder = PreferenceUtil.nowPlayingActionButtonsOrder
        val currentButtonVisibility = PreferenceUtil.nowPlayingActionButtonsVisibility

        // Create the initial list of buttons based on loaded preferences, maintaining order and visibility
        val initialButtonList = mutableListOf<NowPlayingActionButton>()

        // Add buttons based on saved order and visibility
        if (currentButtonOrder.isNotEmpty()) {
            for (buttonId in currentButtonOrder) {
                val button = defaultActionButtons.find { it.id == buttonId }
                button?.let { // Use 'it' to refer to the found button
                    it.visible = currentButtonVisibility[buttonId.toString()] ?: true // Default to visible if not in preferences
                    initialButtonList.add(it)
                }
            }
            // Add any new buttons that might not be in the saved order, at the end
            for (button in defaultActionButtons) {
                if (!initialButtonList.any { it.id == button.id }) {
                    initialButtonList.add(button.copy()) // Add a copy to avoid modifying the default list
                }
            }
        } else {
            // If no order is saved, use the default order and visibility
            initialButtonList.addAll(defaultActionButtons.map { it.copy() }) // Add copies
        }


        actionButtonAdapter = NowPlayingActionButtonAdapter(initialButtonList) // Initialize adapter

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = actionButtonAdapter
            // Set up drag and drop later using ItemTouchHelper
        }

        return materialDialog(R.string.pref_title_player_action_buttons) // Use a suitable string resource
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.done) { _, _ -> saveChanges() }
            .setView(binding.root)
            .create()
            .colorButtons()
    }

    private fun saveChanges() {
        val savedOrder = actionButtonAdapter.actionButtons.map { it.id }
        val savedVisibility = actionButtonAdapter.actionButtons.associate { it.id.toString() to it.visible }

        PreferenceUtil.nowPlayingActionButtonsOrder = savedOrder
        PreferenceUtil.nowPlayingActionButtonsVisibility = savedVisibility

        // Trigger an update in AbsPlayerFragment to reflect the changes immediately
        // This could be done via a shared ViewModel or a LocalBroadcast
        // For now, we'll rely on onResume which will be called when the dialog is dismissed and the fragment resumes.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter for the RecyclerView
    private class NowPlayingActionButtonAdapter(val actionButtons: MutableList<NowPlayingActionButton>) :
        RecyclerView.Adapter<NowPlayingActionButtonAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListItemActionButtonReorderableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val button = actionButtons[position]
            holder.bind(button)
        }

        override fun getItemCount(): Int {
            return actionButtons.size
        }

        inner class ViewHolder(private val binding: ListItemActionButtonReorderableBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(button: NowPlayingActionButton) {
                binding.buttonName.text = button.name
                binding.checkboxVisibility.isChecked = button.visible

                binding.checkboxVisibility.setOnCheckedChangeListener { _, isChecked ->
                    button.visible = isChecked
                }

                // Drag handle will be set up with ItemTouchHelper later
            }
        }

        // Methods for reordering will be added here when implementing ItemTouchHelper
    }

    companion object {
        fun newInstance(): NowPlayingActionButtonsPreferenceDialog {
            return NowPlayingActionButtonsPreferenceDialog()
        }
    }
}
