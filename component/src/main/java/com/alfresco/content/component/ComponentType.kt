package com.alfresco.content.component

/**
 * Marked as ComponentType class
 */
enum class ComponentType(val value: String) {

    TEXT("text"),
    VIEW_TEXT("view-text"),
    CHECK_LIST("check-list"),
    SLIDER("slider"),
    NUMBER_RANGE("number-range"),
    DATE_RANGE("date-range"),
    DATE_RANGE_FUTURE("date-range-future"),
    RADIO("radio"),
    FACETS("facets"),
    None("none")
}
