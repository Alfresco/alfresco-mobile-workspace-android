package com.alfresco.content.browse.tasks

/**
 * This is chip component of various types
 */
enum class ChipFilterType(val component: String) {

    TEXT("text"),
    DATE_RANGE("date-range"),
    RADIO("radio"),
    None("none")
}
