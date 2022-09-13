package com.alfresco.content.data

fun convertTaskEntryToEntry(taskEntry: TaskEntry): Entry {

    return Entry(
        id = taskEntry.id,
        name = taskEntry.name
    )
}
