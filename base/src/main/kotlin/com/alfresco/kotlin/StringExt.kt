package com.alfresco.kotlin

fun String.ellipsize(length: Int) =
    if (this.length <= length) this
    else this.subSequence(0, length).toString().plus("\u2026")
