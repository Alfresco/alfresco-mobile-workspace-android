package com.alfresco.content

import android.content.SharedPreferences

fun SharedPreferences.Editor.putStringList(key: String, list: List<String>) {
    this.putString(key, list.joinToString("\n"))
}

fun SharedPreferences.getStringList(key: String): List<String> {
    return this.getString(key, null)?.split("\n") ?: listOf()
}
