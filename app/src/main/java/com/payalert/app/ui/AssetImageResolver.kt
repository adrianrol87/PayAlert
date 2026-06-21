package com.payalert.app.ui

import android.content.Context
import java.io.IOException

object AssetImageResolver {
    private const val AssetsRoot = "cards"

    fun assetPathFor(
        context: Context,
        preferredDirectory: String,
        fallbackDirectory: String,
    ): String? {
        return firstFileInMatchingDirectory(context, preferredDirectory)
            ?: firstFileInMatchingDirectory(context, fallbackDirectory)
            ?: firstFileInMatchingDirectory(context, "generic")
    }

    private fun firstFileInMatchingDirectory(context: Context, directoryName: String): String? {
        val candidateDirectories = listOf(
            directoryName,
            "$directoryName.imageset",
        )

        return candidateDirectories.firstNotNullOfOrNull { candidate ->
            try {
                val files = context.assets.list("$AssetsRoot/$candidate").orEmpty()
                val chosen = files.firstOrNull { file ->
                    val lower = file.lowercase()
                    lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp")
                }
                chosen?.let { "$AssetsRoot/$candidate/$it" }
            } catch (_: IOException) {
                null
            }
        }
    }
}
