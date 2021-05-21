package com.alfresco.kotlin

object FilenameComparator : java.util.Comparator<String> {
    override fun compare(s1: String, s2: String): Int {
        val r = base(s1).compareTo(base(s2), ignoreCase = true)
        return if (r != 0) r
        else ext(s1).compareTo(ext(s2), ignoreCase = true)
    }

    private fun base(s: String) =
        s.substringBeforeLast('.')

    private fun ext(s: String) =
        s.substringAfterLast('.', "")
}
