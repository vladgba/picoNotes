package x.vladgba.notes

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets

/**
 * targetSdk 35 forces edge-to-edge on Android 15. Draw edge-to-edge on API 30+
 * everywhere and pad [root] by the system bars (and keyboard).
 */
fun Activity.applySystemInsets(root: View) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
    @Suppress("DEPRECATION") // no-op on 35+, needed on 30–34
    window.setDecorFitsSystemWindows(false)
    val l = root.paddingLeft
    val t = root.paddingTop
    val r = root.paddingRight
    val b = root.paddingBottom
    root.setOnApplyWindowInsetsListener { v, insets ->
        val i = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
        v.setPadding(l + i.left, t + i.top, r + i.right, b + i.bottom)
        WindowInsets.CONSUMED
    }
    root.requestApplyInsets()
}
