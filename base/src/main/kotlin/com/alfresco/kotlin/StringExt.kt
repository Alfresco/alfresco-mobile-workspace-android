package com.alfresco.kotlin

import android.net.Uri
import java.io.File
import java.security.MessageDigest

fun String.ellipsize(length: Int) =
    if (this.length <= length) {
        this
    } else this.subSequence(0, length).toString().plus("\u2026")

fun String.isLocalPath() =
    with(Uri.parse(this).scheme) {
        this == null || this == "file"
    }

fun String.filename() =
    Uri.parse(this).lastPathSegment

fun String.parentFile(): File? =
    File(Uri.parse(this).path ?: "").parentFile

fun String.md5(): String =
    hashString("MD5", this)

fun String.sha1(): String =
    hashString("SHA-1", this)

fun String.sha256(): String =
    hashString("SHA-256", this)

fun String.sha512(): String =
    hashString("SHA-512", this)

/**
 * Supported algorithms on Android:
 *
 * Algorithm	Supported API Levels
 * MD5          1+
 * SHA-1	    1+
 * SHA-224	    1-8,22+
 * SHA-256	    1+
 * SHA-384	    1+
 * SHA-512	    1+
 */
private fun hashString(type: String, input: String): String {
    val hexChars = "0123456789ABCDEF"
    val bytes = MessageDigest
        .getInstance(type)
        .digest(input.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(hexChars[i shr 4 and 0x0f])
        result.append(hexChars[i and 0x0f])
    }

    return result.toString()
}
