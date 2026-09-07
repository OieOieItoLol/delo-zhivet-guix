package bot.domain.task

import bot.support.toPgObject
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class TaskRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    @Transactional
    fun insert(task: Task): Task {
        val insertTaskParams = MapSqlParameterSource()
        insertTaskParams.addValue("type", task.type.dbValue)
        insertTaskParams.addValue("name", task.name)
        insertTaskParams.addValue("createDate", task.createDate)
        insertTaskParams.addValue("status", task.status.dbValue)
        insertTaskParams.addValue("statusDate", task.statusDate)
        insertTaskParams.addValue("eventDatetimeUtc", task.eventDatetimeUtc)
        insertTaskParams.addValue("leader", task.leader)
        insertTaskParams.addValue("chatLink", task.chatLink)
        insertTaskParams.addValue("note", task.note)
        insertTaskParams.addValue("coordinates", task.coordinates?.toPgObject())
        insertTaskParams.addValue("contact", task.contact)
        val taskId = jdbcTemplate.queryForObject(insertTaskQuery, insertTaskParams, Int::class.java)

        if (TaskType.SOCIAL == task.type) {
            val insertSocialTaskParams = MapSqlParameterSource()
            insertSocialTaskParams.addValue("taskId", taskId)
            insertSocialTaskParams.addValue("address", task.address)
            jdbcTemplate.update(insertSocialTaskQuery, insertSocialTaskParams)
        }

        return task.copy(id = taskId)
    }

    fun selectMaxId(): Int? {
        val params = MapSqlParameterSource()
        return jdbcTemplate.queryForObject(selectMaxId, params, Int::class.java)
    }

    companion object {
        val insertTaskQuery = """
            INSERT INTO task (type, name, create_date, status, status_date, event_datetime_utc, leader, chat_link, note, coordinates, contact)
            VALUES (:type::task_type, :name, :createDate, :status::task_status, :statusDate, :eventDatetimeUtc, :leader, :chatLink, :note, :coordinates::geometry, :contact)
            RETURNING id
        """.trimIndent()

        val insertSocialTaskQuery = """
            INSERT INTO soctask_info (task_id, address)
            VALUES (:taskId, :address)
        """.trimIndent()

        val selectMaxId = """
            SELECT MAX(id) FROM task;
        """.trimIndent()
    }
}
