package bot.domain.tag

import bot.domain.task.Task
import bot.domain.task.TaskType
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class TagRepository(private val jdbcTemplate: NamedParameterJdbcTemplate, private val rowMapper: TagRowMapper) {

    fun findAllByTaskType(taskType: TaskType): List<Tag> {
        val params = MapSqlParameterSource()
        params.addValue("taskType", taskType.dbValue)

        return jdbcTemplate.query(findByTaskTypeQuery, params, rowMapper)
    }

    @Transactional
    fun saveTaskTags(task: Task, tags: List<Tag>) {
        val batchArgs = tags.map { tag ->
            MapSqlParameterSource()
                .addValue("taskId", task.id)
                .addValue("tagId", tag.id)
        }.toList().toTypedArray()

        jdbcTemplate.batchUpdate(insertTaskTags, batchArgs)
    }

    companion object {
        val findByTaskTypeQuery = """
            SELECT id,
                   name,
                   task,
                   is_hidden_in_bot,
                   is_archived
            FROM users_tag
            WHERE not is_hidden_in_bot AND
                not is_archived AND
                task = :taskType::task_type
        """.trimIndent()

        val insertTaskTags = """
            INSERT INTO task_users_tag
            VALUES (:taskId, :tagId)
        """.trimIndent()
    }
}
