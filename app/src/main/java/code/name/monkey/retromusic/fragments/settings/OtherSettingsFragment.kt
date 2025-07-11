/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package code.name.monkey.retromusic.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.name.monkey.retromusic.LANGUAGE_NAME
import code.name.monkey.retromusic.LAST_ADDED_CUTOFF
import code.name.monkey.retromusic.FIX_YEAR
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.extensions.installLanguageAndRecreate
import code.name.monkey.retromusic.fragments.LibraryViewModel
import code.name.monkey.retromusic.fragments.ReloadType.HomeSections
import code.name.monkey.retromusic.util.PreferenceUtil
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.navigation.fragment.findNavController
import android.app.ProgressDialog
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author Hemanth S (h4h13).
 */

class OtherSettingsFragment : AbsSettingsFragment() {
    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    override fun invalidateSettings() {
        val languagePreference: ATEListPreference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            return@setOnPreferenceChangeListener true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceUtil.languageCode =
            AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "auto" }
        addPreferencesFromResource(R.xml.pref_advanced)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preference: Preference? = findPreference(LAST_ADDED_CUTOFF)
        preference?.setOnPreferenceChangeListener { lastAdded, newValue ->
            setSummary(lastAdded, newValue)
            libraryViewModel.forceReload(HomeSections)
            true
        }
        val languagePreference: Preference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            if (newValue as? String == "auto") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                // Install the languages from Play Store first and then set the application locale
                requireActivity().installLanguageAndRecreate(newValue.toString()) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(
                            newValue as? String
                        )
                    )
                }
            }
            true
        }

        val libraryPreference: Preference? = findPreference(FIX_YEAR)
        libraryPreference?.setOnPreferenceChangeListener { prefs, newValue ->

            if (newValue as? Boolean == true) {
                 val progressDialog = ProgressDialog(requireContext()).apply {
                     setTitle("Scanning songs")
                     setMessage("Please wait...")
                     setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                     setCancelable(false)
                     show()
                 }
                
                libraryViewModel.startMetadataScan(
                    requireContext(),
                    onProgress = { songTitle, index, total ->
                        progressDialog.max = total
                        progressDialog.progress = index
                    },
                    onComplete = {
                        lifecycleScope.launch(Dispatchers.Main) {
                            progressDialog.dismiss()
                            Toast.makeText(requireContext(), "Scan completed!", Toast.LENGTH_SHORT).show()

                            findNavController().previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("data_changed", true)
                        }
                    }
                )
            }
            true
        }
    }
}
