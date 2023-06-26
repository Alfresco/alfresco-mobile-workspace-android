package com.alfresco.list

fun <T> merge(
    left: List<T>,
    right: List<T>,
    includeRemainingLeft: Boolean = true,
    includeRemainingRight: Boolean = true,
    comparator: Comparator<T>,
): List<T> {
    var indexLeft = 0
    var indexRight = 0
    val newList = mutableListOf<T>()

    while (indexLeft < left.count() && indexRight < right.count()) {
        if (comparator.compare(left[indexLeft], right[indexRight]) <= 0) {
            newList.add(left[indexLeft])
            indexLeft++
        } else {
            newList.add(right[indexRight])
            indexRight++
        }
    }

    while (includeRemainingLeft && indexLeft < left.size) {
        newList.add(left[indexLeft])
        indexLeft++
    }

    while (includeRemainingRight && indexRight < right.size) {
        newList.add(right[indexRight])
        indexRight++
    }
    return newList
}
