package com.alfresco.content.browse.tasks

data class TaskSortData(
    val title: String = "",
    val selectedValue: String = "",
    val isSelected: Boolean = false,
    val values: List<String> = emptyList()
) {
    companion object {
        fun with(title: String, values: List<String> = emptyList()): TaskSortData {
            return TaskSortData(
                title = title,
                values = values
            )
        }

        fun reset(obj: TaskSortData): TaskSortData {
            return TaskSortData(
                title = obj.title,
                values = obj.values
            )
        }

        fun updateData(obj: TaskSortData): TaskSortData {

            return TaskSortData(
                title = obj.title,
                isSelected = true,
                selectedValue = if (obj.values.isNotEmpty()) getSelectedValue(obj) else obj.title,
                values = obj.values
            )
        }

        private fun getSelectedValue(obj: TaskSortData): String {
            val index = obj.values.indexOf(obj.selectedValue)
            return if (index == obj.values.size - 1) obj.values[0] else obj.values[index.plus(1)]
        }
    }
}
