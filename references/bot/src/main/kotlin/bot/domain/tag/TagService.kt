package bot.domain.tag

import bot.domain.task.Task
import bot.domain.task.TaskType
import org.springframework.stereotype.Service

@Service
class TagService(private val repository: TagRepository) {

    fun findAllByTaskType(taskType: TaskType): List<Tag> = repository.findAllByTaskType(taskType)

    fun saveTaskTags(task: Task, tags: List<Tag>) = repository.saveTaskTags(task, tags)
}
