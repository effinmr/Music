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
package code.name.monkey.retromusic.fragments.player.adaptive

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentAdaptivePlayerBinding
import code.name.monkey.retromusic.extensions.*
import code.name.monkey.retromusic.fragments.base.AbsPlayerFragment
import code.name.monkey.retromusic.fragments.base.goToAlbum
import code.name.monkey.retromusic.fragments.base.goToArtist
import code.name.monkey.retromusic.fragments.player.PlayerAlbumCoverFragment
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.util.color.MediaNotificationProcessor
import android.text.TextUtils
import android.widget.TextView

class AdaptiveFragment : AbsPlayerFragment(R.layout.fragment_adaptive_player) {

    private var _binding: FragmentAdaptivePlayerBinding? = null
    private val binding get() = _binding!!

    private var lastColor: Int = 0
    private lateinit var playbackControlsFragment: AdaptivePlaybackControlsFragment

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdaptivePlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        binding.playbackControlsFragment.drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        playbackControlsFragment =
            whichFragment(R.id.playbackControlsFragment) as AdaptivePlaybackControlsFragment
        val playerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        playerAlbumCoverFragment.apply {
            removeSlideEffect()
            setCallbacks(this@AdaptiveFragment)
        }
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            ToolbarContentTintHelper.colorizeToolbar(this, surfaceColor(), requireActivity())
            setTitleTextColor(textColorPrimary())
            setSubtitleTextColor(textColorSecondary())
            setOnMenuItemClickListener(this@AdaptiveFragment)
        }
    }

    private fun applyMarqueeToToolbarTitle() {
        _binding?.playerToolbar?.postDelayed({
            val binding = _binding ?: return@postDelayed
            for (i in 0 until binding.playerToolbar.childCount) {
                val view = binding.playerToolbar.getChildAt(i)
                if (view is TextView && view.text == binding.playerToolbar.title) {
                    view.apply {
                        ellipsize = TextUtils.TruncateAt.MARQUEE
                        isSingleLine = true
                        marqueeRepeatLimit = -1
                        isSelected = true
                        setHorizontallyScrolling(true)
                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()
                        setOnClickListener {
                            goToAlbum(requireActivity())
                        }
                    }
                } else if (view is TextView && view.text == binding.playerToolbar.subtitle) {
                    view.setOnClickListener {
                        goToArtist(
                            requireActivity(),
                            MusicPlayerRemote.currentSong.artistName,
                            MusicPlayerRemote.currentSong.artistId
                        )
                    }
                }
            }
        }, 300)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateIsFavorite()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.playerToolbar.title = song.title
        binding.playerToolbar.subtitle = song.allArtists
        applyMarqueeToToolbarTitle()
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onColorChanged(color: MediaNotificationProcessor) {
        playbackControlsFragment.setColor(color)
        lastColor = color.primaryTextColor
        libraryViewModel.updateColor(color.primaryTextColor)
        ToolbarContentTintHelper.colorizeToolbar(
            binding.playerToolbar,
            colorControlNormal(),
            requireActivity()
        )
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun toolbarIconColor(): Int {
        return colorControlNormal()
    }

    override val paletteColor: Int
        get() = lastColor
}
