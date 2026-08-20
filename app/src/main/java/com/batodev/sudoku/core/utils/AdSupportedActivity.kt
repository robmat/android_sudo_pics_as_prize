package com.batodev.sudoku.core.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that periodically shows an interstitial ad while the activity is visible.
 *
 * This extracts the ad-scheduling boilerplate (visibility tracking + a self-rescheduling
 * [Handler] loop) that previously existed as separately copy-pasted code in MainActivity and
 * GalleryActivity.
 */
abstract class AdSupportedActivity : AppCompatActivity() {
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var isActivityVisible = true

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    fun isActivityVisible(): Boolean = isActivityVisible

    /**
     * Starts (or continues) the recurring ad-check loop: shows an ad via [AdHelper] whenever the
     * activity is visible, then reschedules itself after [adCheckIntervalMs].
     */
    protected fun handlerAdPosting(adCheckIntervalMs: Long) {
        handler.postDelayed({
            Log.d(this::class.java.simpleName, "Showing ad.")
            if (isActivityVisible()) {
                AdHelper.showAd(this)
            }
            handlerAdPosting(adCheckIntervalMs)
        }, adCheckIntervalMs)
    }
}
