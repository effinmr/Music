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
package code.name.monkey.retromusic.fragments.player.normal

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.appthemehelper.util.TintHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentPlayerPlaybackControlsBinding
import code.name.monkey.retromusic.extensions.*
import code.name.monkey.retromusic.fragments.base.AbsPlayerControlsFragment
import code.name.monkey.retromusic.fragments.base.goToAlbum
import code.name.monkey.retromusic.fragments.base.goToArtist
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.color.MediaNotificationProcessor
import com.google.android.material.slider.Slider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import code.name.monkey.retromusic.fragments.base.AbsPlayerFragment
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.MetadataField
import code.name.monkey.retromusic.util.FileUtil
import code.name.monkey.retromusic.util.MusicUtil
import java.io.File
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaMetadataRetriever
import android.net.Uri


class PlayerPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_player_playback_controls),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    private var individualArtists: List<String> = emptyList()

    override val progressSlider: Slider
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerPlaybackControlsBinding.bind(view)

        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.text.isSelected = true

        binding.title.setOnClickListener {
            if (!PreferenceUtil.disabledNowPlayingTaps.contains("title")) {
                goToAlbum(requireActivity())
            }
        }

        binding.text.setOnClickListener {
            if (!PreferenceUtil.disabledNowPlayingTaps.contains("artist")) {
                if (individualArtists.size > 1) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.select_artist)
                        .setItems(individualArtists.toTypedArray()) { _, which ->
                            val selectedArtistName = individualArtists[which]
                            lifecycleScope.launch(Dispatchers.IO) {
                                val allArtists = (requireParentFragment() as AbsPlayerFragment).libraryViewModel.artists.value
                                val selectedArtist = allArtists?.find {
                                    it.name.equals(selectedArtistName, ignoreCase = true)
                                }
                                withContext(Dispatchers.Main) {
                                    if (selectedArtist != null) {
                                        goToArtist(requireActivity(), selectedArtist.name, selectedArtist.id)
                                    } else {
                                        Toast.makeText(requireContext(), "Artist not found: $selectedArtistName", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        .show()
                } else {
                    val artistName = MusicPlayerRemote.currentSong.artistName
                    val artistId = MusicPlayerRemote.currentSong.artistId

                    lifecycleScope.launch(Dispatchers.IO) {
                        val allArtists = (requireParentFragment() as AbsPlayerFragment).libraryViewModel.artists.value
                        val artist = allArtists?.find {
                            it.name.equals(artistName, ignoreCase = true)
                        }
                        withContext(Dispatchers.Main) {
                            if (artist != null) {
                                goToArtist(requireActivity(), artist.name, artist.id)
                            } else {
                                Toast.makeText(requireContext(), "Artist not found: $artistName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        _binding = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.NOW_PLAYING_METADATA_ORDER,
            PreferenceUtil.NOW_PLAYING_METADATA_VISIBILITY -> {
                updateSong()
            }
            PreferenceUtil.DISABLED_NOW_PLAYING_TAPS -> {
                // No UI update needed for tap preference change, just the click listener logic
            }
        }
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val colorBg = ATHUtil.resolveColor(requireContext(), android.R.attr.colorBackground)
        if (ColorUtil.isColorLight(colorBg)) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
        }

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            accentColor()
        }.ripAlpha()

        TintHelper.setTintAuto(
            binding.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(
                requireContext(),
                ColorUtil.isColorLight(colorFinal)
            ),
            false
        )
        TintHelper.setTintAuto(binding.playPauseButton, colorFinal, true)
        binding.progressSlider.applyColor(colorFinal)
        volumeFragment?.setTintable(colorFinal)
        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title

        val artistName = song.artistName
        val delimiters = PreferenceUtil.artistDelimiters.split(",").map { it.trim() }
        individualArtists = artistName.split(*delimiters.toTypedArray())
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Always display the full artist name string
        binding.text.text = artistName

        val metadataOrder = PreferenceUtil.nowPlayingMetadataOrder
        val metadataVisibility = PreferenceUtil.nowPlayingMetadataVisibility
        val stringBuilder = StringBuilder()

        var retriever: MediaMetadataRetriever? = null
        try {
            if (song.data.isNotEmpty()) {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(requireContext(), Uri.parse(song.data))
            }

            for (fieldId in metadataOrder) {
                if (metadataVisibility.contains(fieldId)) {
                    val metadataField = MetadataField.fromId(fieldId)
                    metadataField?.let {
                        val label = getString(it.labelRes)
                        val value = when (it) {
                            MetadataField.ALBUM -> song.albumName
                            MetadataField.ARTIST -> song.artistName
                            MetadataField.YEAR -> if (!song.year.isNullOrEmpty()) song.year else null
                            MetadataField.BITRATE -> {
                                retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.let { bitrateStr ->
                                    try {
                                        val bitrate = bitrateStr.toLong() / 1000 // Convert to kbps
                                        "${bitrate}kbps"
                                    } catch (e: NumberFormatException) {
                                        null
                                    }
                                }
                            }
                            MetadataField.FORMAT -> {
                                retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let { mimeType ->
                                    mimeType.substringAfterLast('/') // e.g., "mpeg" from "audio/mpeg"
                                }
                            }
                            MetadataField.TRACK_LENGTH -> if (song.duration != 0L) MusicUtil.getReadableDurationString(song.duration) else null
                            MetadataField.FILE_NAME -> if (song.data.isNotEmpty()) File(song.data).name else null
                            MetadataField.FILE_PATH -> if (song.data.isNotEmpty()) song.data else null
                            MetadataField.FILE_SIZE -> {
                                val file = File(song.data)
                                if (file.exists()) FileUtil.getReadableFileSize(file.length()) else null
                            }
                            MetadataField.SAMPLING_RATE -> {
                                retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.let { sampleRateStr ->
                                    try {
                                        val sampleRate = sampleRateStr.toLong() / 1000 // Convert to kHz
                                        "${sampleRate}kHz"
                                    } catch (e: NumberFormatException) {
                                        null
                                    }
                                }
                            }
                            MetadataField.LAST_MODIFIED -> if (song.dateModified != 0L) {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                dateFormat.format(Date(song.dateModified * 1000L))
                            } else null
                        }

                        if (!value.isNullOrEmpty()) {
                            if (stringBuilder.isNotEmpty()) {
                                stringBuilder.append("  •  ")
                            }
                            stringBuilder.append("$label: $value")
                        }
                    }
                }
            }
        } finally {
            retriever?.release()
        }

        if (stringBuilder.isNotEmpty()) {
            binding.songInfo.text = stringBuilder.toString()
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }


    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            it.showBounceAnimation()
        }
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }
}
