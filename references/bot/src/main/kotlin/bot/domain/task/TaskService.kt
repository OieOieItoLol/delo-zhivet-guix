package bot.domain.task

import org.springframework.stereotype.Service

@Service
class TaskService(private val repository: TaskRepository) {

    fun save(task: Task): Task = repository.insert(task)

    fun getLastId(): Int? = repository.selectMaxId()
}
