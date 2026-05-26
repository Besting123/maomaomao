package com.example.myapplication.ui.utils

import android.content.Context
import android.content.Intent

fun sharePlainText(
    context: Context,
    chooserTitle: String,
    subject: String,
    body: String
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
}
