package code.name.monkey.appthemehelper.common.prefs.supportv7

import android.content.Context
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.preference.CheckBoxPreference
import code.name.monkey.appthemehelper.R
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.common.views.ATESwitch

/**
 * @author Aidan Follestad (afollestad)
 */
import androidx.preference.PreferenceViewHolder

class ATESwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) :
    CheckBoxPreference(context, attrs, defStyleAttr, defStyleRes) {

    private var switchView: ATESwitch? = null

    init {
        widgetLayoutResource = R.layout.ate_preference_switch_support
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ATHUtil.resolveColor(
                context,
                android.R.attr.colorControlNormal
            ), BlendModeCompat.SRC_IN
        )
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        // Find the switch view by its ID
        switchView = holder.findViewById(android.R.id.checkbox) as? ATESwitch
        // Apply the initial state color
        updateSwitchColor(isChecked)
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        // Update the color when the checked state changes
        updateSwitchColor(checked)
    }

    private fun updateSwitchColor(isChecked: Boolean) {
        switchView?.alpha = if (isChecked) 1.0f else 0.5f
    }
}