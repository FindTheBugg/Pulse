package com.example.pulse.UI.settings

import android.content.Context
import com.example.pulse.R
import org.json.JSONObject

data class ChangelogVersion(
    val version: String,
    val items: List<String>
)

fun readChangelog(context: Context): List<ChangelogVersion> {
    val text = context.resources.openRawResource(R.raw.changelog)
        .bufferedReader()
        .use { it.readText() }

    val versions = JSONObject(text).getJSONArray("versions")
    return List(versions.length()) { i ->
        val v = versions.getJSONObject(i)
        val items = v.getJSONArray("items")
        ChangelogVersion(
            version = v.getString("version"),
            items = List(items.length()) { items.getString(it) }
        )
    }
}