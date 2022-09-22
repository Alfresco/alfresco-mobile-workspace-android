package com.alfresco.content

/**
 * Marked as TaskPriority enum class
 */
enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    RESET("reset"),
    NONE("none")
}

enum class DefaultPriority(val value: Int) {
    LOW(3),
    MEDIUM(7),
    HIGH(10)
}

/**
 * returns the Task priority on the basis of given integer value
 */
fun getTaskPriority(priority: Int): TaskPriority {
    return when (priority) {
        -1 -> TaskPriority.RESET
        0, 1, 2, 3 -> TaskPriority.LOW
        4, 5, 6, 7 -> TaskPriority.MEDIUM
        8, 9, 10 -> TaskPriority.HIGH
        else -> TaskPriority.NONE
    }
}
