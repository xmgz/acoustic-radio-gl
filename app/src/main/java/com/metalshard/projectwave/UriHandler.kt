package com.metalshard.projectwave

import android.content.Intent
import android.net.Uri

object UriHandler {
    fun handleIncomingIntent(intent: Intent?): RadioStation? {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return null

        val data: Uri = intent.data ?: return null

        if (data.scheme == "acoustic" && data.host == "register") {
            val name = data.getQueryParameter("name").orEmpty()
            val url = data.getQueryParameter("url").orEmpty()
            val image = data.getQueryParameter("image").orEmpty()

            if (url.isNotBlank()) {
                return RadioStation(
                    id = -99,
                    name = name,
                    streamUrl = url,
                    imageUrl = image
                )
            }
        }
        return null
    }
}