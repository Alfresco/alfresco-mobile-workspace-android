package com.alfresco.content.data

/**
 * Marked as TaskPriority enum class
 */
enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    RESET("reset"),
    NONE("none"),
}

/**
 * Marked as DefaultPriority enum class
 */
enum class DefaultPriority(val value: Int) {
    LOW(3),
    MEDIUM(7),
    HIGH(10),
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

/**
 * returns the priority integer value using low, medium and high priority.
 */
fun getPriorityNumValue(priority: String): Int {
    return when (priority.lowercase()) {
        DefaultPriority.LOW.name.lowercase() -> DefaultPriority.LOW.value
        DefaultPriority.MEDIUM.name.lowercase() -> DefaultPriority.MEDIUM.value
        DefaultPriority.HIGH.name.lowercase() -> DefaultPriority.HIGH.value
        else -> -1
    }
}
