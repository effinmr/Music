/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package code.name.monkey.retromusic.views

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.ListSettingItemViewBinding

/**
 * Created by hemanths on 2019-12-10.
 */
class SettingListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ListSettingItemViewBinding =
        ListSettingItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    var settingListItemTitle: CharSequence? = null
        set(value) {
            field = value
            binding.title.text = value // Explicitly set text
            originalSettingListItemTitle = value // Also update original for reset
        }
    var settingListItemText: CharSequence? = null
        set(value) {
            field = value
            binding.text.text = value // Explicitly set text
            originalSettingListItemText = value // Also update original for reset
        }

    private var originalSettingListItemTitle: CharSequence? = null
    private var originalSettingListItemText: CharSequence? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.SettingListItemView) {
            if (hasValue(R.styleable.SettingListItemView_settingListItemIcon)) {
                binding.icon.setImageDrawable(getDrawable(R.styleable.SettingListItemView_settingListItemIcon))
            }
            binding.icon.setIconBackgroundColor(
                getColor(R.styleable.SettingListItemView_settingListItemIconColor, Color.WHITE)
            )
            // Call setters to ensure text views are updated
            settingListItemTitle = getText(R.styleable.SettingListItemView_settingListItemTitle)
            settingListItemText = getText(R.styleable.SettingListItemView_settingListItemText)
        }
    }

    fun setSearchQuery(query: String?) {
        val highlightColor = ContextCompat.getColor(context, R.color.highlight_yellow)

        if (query.isNullOrEmpty()) {
            binding.title.text = originalSettingListItemTitle
            binding.text.text = originalSettingListItemText
        } else {
            highlightText(binding.title, originalSettingListItemTitle, query, highlightColor)
            highlightText(binding.text, originalSettingListItemText, query, highlightColor)
        }
    }

    private fun highlightText(textView: com.google.android.material.textview.MaterialTextView, originalText: CharSequence?, query: String, highlightColor: Int) {
        if (originalText == null) return

        val spannable = SpannableString(originalText)
        val lowerCaseText = originalText.toString().lowercase()
        val lowerCaseQuery = query.lowercase()

        var startIndex = lowerCaseText.indexOf(lowerCaseQuery)
        while (startIndex != -1) {
            val endIndex = startIndex + lowerCaseQuery.length
            spannable.setSpan(BackgroundColorSpan(highlightColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            startIndex = lowerCaseText.indexOf(lowerCaseQuery, startIndex + 1)
        }
        textView.text = spannable
    }
}
