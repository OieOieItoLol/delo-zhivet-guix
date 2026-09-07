package bot.domain.tag

import bot.domain.task.TaskType
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class TagRowMapper : RowMapper<Tag> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Tag = Tag(
        id = rs.getInt("id").takeIf { !rs.wasNull() },
        name = rs.getString("name"),
        task = TaskType.fromDbValue(rs.getString("task")),
        isHiddenInBot = rs.getBoolean("is_hidden_in_bot"),
        isArchived = rs.getBoolean("is_archived")
    )
}
