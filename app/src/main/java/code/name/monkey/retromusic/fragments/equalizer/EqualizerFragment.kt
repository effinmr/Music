package code.name.monkey.retromusic.fragments.equalizer

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentEqualizerBinding
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.service.MultiPlayer

class EqualizerFragment : Fragment(R.layout.fragment_equalizer) {

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!

    private val presets by lazy { resources.getStringArray(R.array.equalizer_presets) }
    private var selectedPresetIndex = 0

    private var destinationListener: androidx.navigation.NavController.OnDestinationChangedListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEqualizerBinding.bind(view)

        val prefs = requireContext().getSharedPreferences("equalizer_prefs", android.content.Context.MODE_PRIVATE)

        setupToolbar()

        binding.appBarLayout.toolbar.inflateMenu(R.menu.menu_equalizer)
        binding.appBarLayout.toolbar.setOnMenuItemClickListener {
            item ->
            when (item.itemId) {
                R.id.action_reset_equalizer -> {
                    setBandValues(0f, 0f, 0f, 0f, 0f)
                    binding.virtualizerSlider.value = 0f
                    binding.bassBoostSlider.value = 0f
                    binding.amplifierSlider.value = 0f
                    selectedPresetIndex = 0
                    updatePresetSelectorText()
                    true
                }
                else -> false
            }
        }
        
        binding.presetSelector.setOnClickListener { showPresetDialog() }

        binding.enableEqualizerSwitch.isChecked = prefs.getBoolean("equalizer_enabled", false)

        binding.enableEqualizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            setSlidersEnabled(isChecked)
            prefs.edit().putBoolean("equalizer_enabled", isChecked).apply()
            val player = MusicPlayerRemote.musicService as? MultiPlayer ?: return@setOnCheckedChangeListener
            player.setEqualizerEnabled(isChecked)
        }

        binding.resetButton.setOnClickListener {
            selectedPresetIndex = 0
            applyPreset(0)
            updatePresetSelectorText()
        }

        setupSlider(binding.bandSlider1, isDb = true, bandIndex = 0)
        setupSlider(binding.bandSlider2, isDb = true, bandIndex = 1)
        setupSlider(binding.bandSlider3, isDb = true, bandIndex = 2)
        setupSlider(binding.bandSlider4, isDb = true, bandIndex = 3)
        setupSlider(binding.bandSlider5, isDb = true, bandIndex = 4)

        // Restore saved band levels
        binding.bandSlider1.value = prefs.getFloat("band_0", 0f)
        binding.bandSlider2.value = prefs.getFloat("band_1", 0f)
        binding.bandSlider3.value = prefs.getFloat("band_2", 0f)
        binding.bandSlider4.value = prefs.getFloat("band_3", 0f)
        binding.bandSlider5.value = prefs.getFloat("band_4", 0f)

        setupSlider(binding.virtualizerSlider, binding.virtualizerValue, "virtualizer_strength")
        setupSlider(binding.bassBoostSlider, binding.bassBoostValue, "bass_boost_strength")
        setupSlider(binding.amplifierSlider, binding.amplifierValue, "amplifier_strength")

        setSlidersEnabled(binding.enableEqualizerSwitch.isChecked)
        updatePresetSelectorText()
    }

    private fun showPresetDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_preset)
            .setSingleChoiceItems(presets, selectedPresetIndex) { dialog, which ->
                selectedPresetIndex = which
                applyPreset(which)
                updatePresetSelectorText()
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updatePresetSelectorText() {
        binding.presetSelector.text = presets[selectedPresetIndex]
    }

    private fun setSlidersEnabled(isEnabled: Boolean) {
        binding.equalizerContent.isEnabled = isEnabled
        binding.equalizerContent.alpha = if (isEnabled) 1.0f else 0.5f
        setViewGroupEnabled(binding.equalizerContent, isEnabled)
    }

    private fun setViewGroupEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setViewGroupEnabled(view.getChildAt(i), enabled)
            }
        }
    }

    private fun setupSlider(slider: Slider, isDb: Boolean, bandIndex: Short = -1) {
        val prefs = requireContext().getSharedPreferences("equalizer_prefs", android.content.Context.MODE_PRIVATE)

        if (isDb) {
            slider.valueFrom = -15f
            slider.valueTo = 15f
            slider.stepSize = 0.1f

            slider.setLabelFormatter { value -> String.format("%+.1f dB", value) }
            slider.addOnChangeListener { _, value, _ ->
                binding.presetSelector.text = getString(R.string.custom_preset)

                if (bandIndex >= 0) {
                    prefs.edit().putFloat("band_$bandIndex", value).apply()
                }
            }
        }
    }

    private fun setupSlider(slider: Slider, valueText: TextView, key: String) {
        val prefs = requireContext().getSharedPreferences("equalizer_prefs", android.content.Context.MODE_PRIVATE)

        val savedValue = prefs.getFloat(key, 0f)
        slider.value = savedValue
        valueText.text = "${savedValue.toInt()}%"
        
        slider.setLabelFormatter { value -> "${value.toInt()}%" }
        slider.addOnChangeListener { _, value, _ ->
            binding.presetSelector.text = getString(R.string.custom_preset)
            valueText.text = "${value.toInt()}%"

            prefs.edit().putFloat(key, value).apply()
        }
    }

    private fun setupToolbar() {
        val navController = findNavController()

        with(binding.appBarLayout.toolbar) {
            setNavigationIcon(R.drawable.ic_arrow_back)
            isTitleCentered = false
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        destinationListener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.title = destination.label?.toString() ?: getString(R.string.equalizer)
        }
        navController.addOnDestinationChangedListener(destinationListener!!)
    }

    private fun applyPreset(position: Int) {
        when (position) {
            0 -> setBandValues(0f, 0f, 0f, 0f, 0f)
            1 -> setBandValues(5f, 3f, 0f, -2f, -4f)
            2 -> setBandValues(3f, 2f, 0f, -2f, -3f)
            3 -> setBandValues(4f, 3f, 0f, -3f, -5f)
            4 -> setBandValues(2f, 1f, 0f, -1f, -2f)
            5 -> setBandValues(6f, 4f, -2f, -4f, -6f)
            6 -> setBandValues(4f, 2f, 0f, 2f, 4f)
            7 -> setBandValues(5f, 5f, 5f, 5f, 5f)
            8 -> setBandValues(-5f, -5f, -5f, -5f, -5f)
        }
    }

    private fun setBandValues(b1: Float, b2: Float, b3: Float, b4: Float, b5: Float) {
        binding.bandSlider1.value = b1
        binding.bandSlider2.value = b2
        binding.bandSlider3.value = b3
        binding.bandSlider4.value = b4
        binding.bandSlider5.value = b5

        val player = MusicPlayerRemote.musicService as? MultiPlayer ?: return
        if (binding.enableEqualizerSwitch.isChecked) {
            val minLevel = player.getEqualizerMinBandLevel()
            val maxLevel = player.getEqualizerMaxBandLevel()

            player.setEqualizerBandLevel(0, (minLevel + (b1 - -10f) * (maxLevel - minLevel) / (20f)).toInt().toShort())
            player.setEqualizerBandLevel(1, (minLevel + (b2 - -10f) * (maxLevel - minLevel) / (20f)).toInt().toShort())
            player.setEqualizerBandLevel(2, (minLevel + (b3 - -10f) * (maxLevel - minLevel) / (20f)).toInt().toShort())
            player.setEqualizerBandLevel(3, (minLevel + (b4 - -10f) * (maxLevel - minLevel) / (20f)).toInt().toShort())
            player.setEqualizerBandLevel(4, (minLevel + (b5 - -10f) * (maxLevel - minLevel) / (20f)).toInt().toShort())
        }
    }

    override fun onDestroyView() {
        findNavController().removeOnDestinationChangedListener(destinationListener!!)
        destinationListener = null
        _binding = null
        super.onDestroyView()
    }
}
