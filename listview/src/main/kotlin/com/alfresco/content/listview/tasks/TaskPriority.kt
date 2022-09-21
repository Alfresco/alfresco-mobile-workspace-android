package com.alfresco.content.listview.tasks

/**
 * Marked as TaskPriority enum class
 */
enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    NONE("none")
}

/**
 * returns the Task priority on the basis of given integer value
 */
fun getTaskPriority(priority: Int): TaskPriority {
    return when (priority) {
        0, 1, 2, 3 -> TaskPriority.LOW
        4, 5, 6, 7 -> TaskPriority.MEDIUM
        8, 9, 10 -> TaskPriority.HIGH
        else -> TaskPriority.NONE
    }
}

fun getTaskPriority(priority: TaskPriority): Int {
    return when (priority) {
        TaskPriority.LOW -> 3
        TaskPriority.MEDIUM -> 7
        TaskPriority.HIGH -> 10
        else -> -1
    }
}
