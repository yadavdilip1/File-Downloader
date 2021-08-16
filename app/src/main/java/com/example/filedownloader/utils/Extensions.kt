package com.example.filedownloader.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.filedownloader.R

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showPermissionsAlert(fontPath: String) {
    val title = getString(R.string.permission_required)
    val message = getString(R.string.permission_msg)
    val positiveText = getString(R.string.permission_goto)
    val negativeText = getString(android.R.string.cancel)
    showAlert(title, message, positiveText, negativeText, fontPath, object : OnPositive {
        override fun onYes() {
            openSettings()
        }
    })
}

private fun Context.openSettings() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.data = Uri.fromParts("package", packageName, null)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}


fun Context.showAlert(title: String? = null, msg: String? = null, positiveText: String? = null, negativeText: String? = null, fontPath: String? = null, positive: OnPositive? = null) {

    val dialog = AlertDialog.Builder(this)
    var alert: AlertDialog? = null


    dialog.setCancelable(false)
    if (title != null) {
        // Initialize a new foreground color span instance
        val foregroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.purple_500))
        val ssBuilder = SpannableStringBuilder(title)
        ssBuilder.setSpan(foregroundColorSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        dialog.setTitle(ssBuilder)
    }
    if (msg != null) {
        dialog.setMessage(msg)
    }
    if (positiveText != null) {
        dialog.setPositiveButton(positiveText) { _, _ ->
            alert?.dismiss()
            positive?.onYes()
        }
    }
    if (negativeText != null) {
        dialog.setNegativeButton(negativeText) { _, _ ->
            alert?.dismiss()
            positive?.onNo()
        }
    }

    alert = dialog.create()
    alert.show()


    if (fontPath != null) {
        val textView = alert.findViewById<TextView>(android.R.id.message)
        try {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            val face = Typeface.createFromAsset(assets, fontPath)
            textView.typeface = face
        } catch (e: Exception) {
            Log.e("showAlert", e.toString())
        }
    }
}


interface OnPositive {
    fun onYes()
    fun onNo() {}
}


