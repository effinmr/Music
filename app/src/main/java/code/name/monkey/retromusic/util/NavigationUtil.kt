package code.name.monkey.retromusic.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.DriveModeActivity
import code.name.monkey.retromusic.activities.LicenseActivity
import code.name.monkey.retromusic.activities.SupportDevelopmentActivity
import code.name.monkey.retromusic.activities.bugreport.BugReportActivity
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.helper.MusicPlayerRemote.audioSessionId

object NavigationUtil {
    fun bugReport(activity: Activity) {
        activity.startActivity(Intent(activity, BugReportActivity::class.java), null)
    }

    fun goToOpenSource(activity: Activity) {
        activity.startActivity(Intent(activity, LicenseActivity::class.java), null)
    }

    fun goToSupportDevelopment(activity: Activity) {
        activity.startActivity(Intent(activity, SupportDevelopmentActivity::class.java), null)
    }

    fun gotoDriveMode(activity: Activity) {
        activity.startActivity(Intent(activity, DriveModeActivity::class.java), null)
    }

    fun openEqualizer(activity: FragmentActivity) {
        customEqualizer(activity)
    }

    private fun stockEqualizer(activity: Activity) {
        val sessionId = audioSessionId
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            activity.showToast(R.string.no_audio_ID, Toast.LENGTH_LONG)
        } else {
            try {
                val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                activity.startActivityForResult(effects, 0)
            } catch (notFound: ActivityNotFoundException) {
                activity.showToast(R.string.no_equalizer)
            }
        }
    }

    private fun customEqualizer(activity: FragmentActivity) {
        try {
            val navController = (activity as FragmentActivity).findNavController(R.id.fragment_container)
            navController.navigate(R.id.equalizerFragment)
        } catch (e: Exception) {
            activity.showToast("Equalizer not available")
        }
    }
}
