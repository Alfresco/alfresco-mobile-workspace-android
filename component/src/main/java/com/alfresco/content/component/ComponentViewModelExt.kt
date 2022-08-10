package com.alfresco.content.component

/**
 * return true if the component is selected,otherwise false
 */
fun ComponentViewModel.isOptionSelected(state: ComponentState, options: ComponentOptions): Boolean {

    if (state.parent?.selectedQuery?.isEmpty() == true)
        return options.default

    val selectedQuery = state.parent?.selectedQuery
    if (selectedQuery?.contains(delimiters) == true) {
        selectedQuery.split(delimiters).forEach { query ->
            if (query == options.query)
                return true
        }
    } else {
        return selectedQuery == options.query
    }
    return false
}

/**
 * return true if To value valid otherwise false
 */
fun ComponentViewModel.isToValueValid(to: String): Boolean {
    if (to.isEmpty())
        return true

    return if (fromValue.isEmpty())
        true
    else
        to.toLong() > fromValue.toLong()
}

/**
 * return true if from value valid otherwise false
 */
fun ComponentViewModel.isFromValueValid(from: String): Boolean {
    if (from.isEmpty())
        return true

    return if (toValue.isEmpty())
        true
    else
        from.toLong() < toValue.toLong()
}
