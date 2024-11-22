package com.rwa.tellme.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun showAlertDialog(
    context: Context,
    title: String,
    message: String,
    onPositiveAction: (() -> Unit)? = null
) {
    AlertDialog.Builder(context).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton("OK") { _, _ -> onPositiveAction?.invoke() }
        create()
        show()
    }
}

fun showToastMessage(
    context: Context,
    message: String,
) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}