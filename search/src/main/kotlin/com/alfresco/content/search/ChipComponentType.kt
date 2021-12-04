package com.alfresco.content.search

/**
 * This is chip component of various types
 */
enum class ChipComponentType(val component: String) {

    TEXT("text"),
    CHECK_LIST("check-list"),
    SLIDER("slider"),
    NUMBER_RANGE("number-range"),
    DATE_RANGE("date-range"),
    RADIO("radio"),
    FACET_QUERIES("facet-queries"),
    FACET_FIELDS("facet-fields"),
    FACET_INTERVALS("facet-intervals"),
    None("none")
}
