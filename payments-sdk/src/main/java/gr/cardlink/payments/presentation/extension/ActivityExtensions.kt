package gr.cardlink.payments.presentation.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

internal fun Activity.hideKeyboard() {
    this.currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

@SuppressLint("UnsupportedChromeOsCameraSystemFeature")
internal fun Activity.hasRearCamera(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
}

internal fun Activity.hasNfc(): Boolean {
    val manager = getSystemService(Context.NFC_SERVICE) as? NfcManager
    val adapter = manager?.defaultAdapter
    return adapter != null
}

internal fun Activity.isNfcEnabled(): Boolean {
    val manager = getSystemService(Context.NFC_SERVICE) as? NfcManager
    val adapter = manager?.defaultAdapter
    return adapter?.isEnabled == true
}

internal fun Activity.showSnackMessage(@StringRes messageRes: Int) {
    val view: View? = findViewById(android.R.id.content)
    view?.let {
        Snackbar.make(it, messageRes, Snackbar.LENGTH_SHORT).show()
    }
}