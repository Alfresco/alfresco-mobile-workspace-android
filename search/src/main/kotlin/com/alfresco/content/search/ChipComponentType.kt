package com.alfresco.content.search

enum class ChipComponentType(val component: String) {

    TEXT("text"),
    CHECK_LIST("check-list"),
    SLIDER("slider"),
    NUMBER_RANGE("number-range"),
    DATE_RANGE("date-range"),
    RADIO("radio"),
    None("none")
}
