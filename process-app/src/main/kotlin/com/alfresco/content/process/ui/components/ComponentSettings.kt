package com.alfresco.content.process.ui.components

data class ComponentSettings(
    val id: String,
    val placeHolder: String,
    val isRequired: Boolean,
    val isReadOnly: Boolean,
    val value: String,
    val minLength: Int,
    val maxLength: Int,
    val minValue: String,
    val maxValue: String,
    val options: ComponentOptions,
)

data class ComponentOptions(
    val id: String,
    val name: String,
)
