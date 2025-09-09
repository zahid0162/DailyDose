package com.zahid.dailydose.utils

import android.net.Uri

fun getFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    it.getString(nameIndex)
                } else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}