package com.wald.apps.emailclient

import java.text.DecimalFormat


fun sizeLabel(size: Long): String {
    val sizeFormat = DecimalFormat("#.#")
    return when {
        size < 2 shl 9 -> "$size bytes"
        size < 2 shl 19 -> sizeFormat.format(size / (2 shl 9)) + " KB"
        else -> sizeFormat.format(size / (2 shl 19)) + " MB"
    }
}