package code.name.monkey.retromusic.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import code.name.monkey.retromusic.dialogs.PlayerActionButtonsDialog

class PlayerActionButtonsPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        // Set a click listener to open the selection/reordering UI
        setOnPreferenceClickListener {
            val fragmentManager = (context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
            fragmentManager?.let {
                PlayerActionButtonsDialog().show(it, PlayerActionButtonsDialog.TAG)
            }
            true
        }
    }
}
