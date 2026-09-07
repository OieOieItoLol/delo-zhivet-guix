package tracker.api.manager;

import net.truej.sql.TrueSql;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tracker.TrackerApplication.MainDb;
import tracker.api.manager.TagApiG.*;

import java.util.List;

import static tracker.TrackerApplication.*;

@RequestMapping("/api/manager/tag")
@TrueSql @RestController public class TagApi {
    @Autowired MainDb ds;

    record TagFields(
        @NotNull String name,
        @NotNull TaskType taskType,
        @NotNull Boolean isHiddenInBot
    ) { }

    @PostMapping("/add") @ResponseBody Integer add(@RequestBody TagFields v) {
        return ds.q("""
            insert into users_tag
            values(default, ?, ?, ?, false)
            returning id
            """, v.name, v.taskType, v.isHiddenInBot
        ).fetchOne(Integer.class);
    }

    record BotAvailabilityFields (
        int tagId,
        boolean isHidden
    ) {}

    @PostMapping("/changeTagBotAvailability") @ResponseBody void changeTagBotAvailability(@RequestBody BotAvailabilityFields f) {
        ds.q("""
            update users_tag
            set is_hidden_in_bot = ?
            where id = ?
            """, f.isHidden, f.tagId).fetchNone();
    }

    record ArchiveStatusFields (
        int tagId,
        boolean isArchived
    ) {}

    @PostMapping("/changeTagArchiveStatus") @ResponseBody void changeTagArchiveStatus(@RequestBody ArchiveStatusFields f) {
        ds.q("""
            update users_tag
            set is_archived = ?
            where id = ?
            """, f.isArchived, f.tagId).fetchNone();
    }

    @PostMapping("/usersTags") @ResponseBody List<Tag> usersTags(@RequestBody Object notArchivedTags) {
        return ds.q("""
            select
                id,
                name,
                task as task_type,
                is_hidden_in_bot,
                is_archived
            from users_tag
            where (? or not is_archived = ?)
        """, (Boolean) notArchivedTags==null, (Boolean) notArchivedTags).g.fetchList(Tag.class);
    }

    @PostMapping("/allowedTagsByTaskType")
    @ResponseBody List<AllowedTag> allowedTagsByTaskType(@RequestBody TaskType taskType) {
        return ds.q("""
            select
                id,
                name
            from users_tag
            where task = ? and not is_archived
        """, taskType).g.fetchList(AllowedTag.class);
    }

}
