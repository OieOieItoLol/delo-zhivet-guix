package bot.domain.tag

import bot.domain.task.TaskType

data class Tag(val id: Int?, val name: String, val task: TaskType, val isHiddenInBot: Boolean, val isArchived: Boolean)
