package tracker.api.manager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import net.truej.sql.TrueSql;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tracker.AuthInterceptor;
import tracker.api.manager.TaskApiG.*;
import tracker.images.ImagesService;
import tracker.images.PhotoCredentials;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static tracker.AuthInterceptor.AUTH_ATTR;
import static tracker.TrackerApplication.*;

@RequestMapping("/api/manager/task")
@TrueSql @RestController class TaskApi {

    @Autowired MainDb ds;

    record EditStatusRequest(
        int id, @NotNull TaskStatus status
    ) { }

    @PostMapping("/editStatus")
    void editStatus(@RequestBody EditStatusRequest req) {
        ds.inTransaction(cn -> {
            if (req.status == TaskStatus.Done) {
                cn.q("""
                    delete from task_volunteer_done where task_id = ?;
                    insert into task_volunteer_done
                    select ?, v.id from volunteer v where st_dwithin(
                        v.coordinates, (select coordinates from task where id = ?), v.radius, true
                    )
                    """, req.id, req.id, req.id
                ).fetchNone();
            }
            return cn.q("""
                update task set
                    status = ?,
                    status_date = now()::timestamp without time zone
                where id = ?
                """, req.status, req.id
            ).withUpdateCount.fetchNone();
        });
    }

    @PostMapping("/allLeaders")
    @ResponseBody List<Leader> allLeaders() {
        return ds.q("select id, tg_name from manager")
            .g.fetchList(Leader.class);
    }

    record EditLeaderRequest(int id, @Nullable Integer leaderId) { }

    @PostMapping("/editLeader")
    void editLeader(@RequestBody EditLeaderRequest req) {
        ds.q("""
            update task
            set leader = ?
            where id = ?
            """, req.leaderId, req.id
        ).fetchNone();
    }

    record EditNameRequest(int id, String name) { }

    @PostMapping("/editName") void editName(@RequestBody EditNameRequest req) {
        ds.q("""
            update task
            set name = ?
            where id = ?
            """, req.name, req.id
        ).fetchNone();
    }

    record EditCreateDateRequest(int id, LocalDate date) { }

    @PostMapping("/editCreateDate")
    void editCreateDate(@RequestBody EditCreateDateRequest req) {
        ds.q("""
            update task
            set create_date = ?
            where id = ?
            """, req.date.atStartOfDay(), req.id
        ).fetchNone();
    }

    record EditChatRequest(int id, String chat) { }

    @PostMapping("/editChat")
    @ResponseBody void editChat(@RequestBody EditChatRequest req) {
        ds.q("""
            update task
            set chat_link = ?
            where id = ?
            """, req.chat, req.id
        ).fetchNone();
    }

    record AddTagsRequest(int id, List<Integer> tagsId) { }

    @PostMapping("/addTags")
    void addTags(@RequestBody AddTagsRequest req) {
        ds.q(req.tagsId, """
            insert into task_users_tag (task_id, tag_id)
            select ?, ut.id
            from users_tag ut
            where ut.id = ? and not is_archived
            """, tagId -> new Object[]{req.id, tagId}
        ).fetchNone();
    }

    @PostMapping("/allAllowedTags") @ResponseBody
    List<TagForAdd> allAllowedTags(@RequestBody TaskType taskType) {
        return ds.q("""
             select
                id,
                name 
             from users_tag
             where not is_archived and task = ? 
             """, taskType
        ).g.fetchList(TagForAdd.class);
    }

    record DeleteTagRequest(int id, int tagId) { }

    @PostMapping("/deleteTag")
    void deleteTag(@RequestBody DeleteTagRequest req) {
        ds.q(
            "delete from task_users_tag where task_id = ? and tag_id = ?",
            req.id, req.tagId
        ).fetchNone();
    }

    record EditNoteRequest(int id, String note) { }

    @PostMapping("/editNote")
    void editNote(@RequestBody EditNoteRequest req) {
        ds.q("""
            update task
            set note = ?
            where id = ?
            """, req.note, req.id
        ).fetchNone();
    }

    @PostMapping("/setTaskEventDate")
    void setTaskEventDate(int taskId, @Nullable LocalDateTime eventDateTimeUTC) {
        ds.q("""
            update task
            set event_datetime_utc = ?
            where id = ?
            """, eventDateTimeUTC, taskId
        ).fetchNone();
    }

    @PostMapping("/get") @ResponseBody Task get(@RequestBody int taskId) {
        var t = ds.q("""
            select
                tsk.id,
                tsk.name,
                tsk.status,
                tsk.type task_type,
                extract(day from (now()::timestamp without time zone) - tsk.status_date)::int status_days_count,
                date(tsk.create_date) as create_date,
                tsk.event_datetime_utc,
                m.id      ":t? leader_id",
                m.tg_name ":t? leader_tg_name",
                tsk.chat_link,
                tsk.note,
                st_x(tsk.coordinates) longitude,
                st_y(tsk.coordinates) latitude,
                v.id      "VolunteerShort volunteers.",
                v.tg_name "               volunteers.",
                tg.id         "TagShort tags.",
                tg.name       "         tags.",
                tst.name      "systemTags.   ",
                tp.photo_id             "Photo  photos.",
                tp.photo_path           "       photos.",
                tp.optimized_photo_path "       photos."
            from task tsk
                left join manager m on tsk.leader = m.id
                left join volunteer v on st_dwithin(v.coordinates, tsk.coordinates, v.radius, true) and not v.is_banned and v.status = 'Active'
                left join v_task_users_tags tg on tsk.id = tg.task_id
                left join v_task_system_tags tst on tsk.id = tst.task_id
                left join task_photo tp on tsk.id = tp.task_id
            where tsk.id = ?
            """, taskId
        ).g.fetchOne(Task.class);

        if (t == null)
            throw new ValidationException("Задачи с таким id нет");
        return t;
    }

    @PostMapping("/tasksShort") @ResponseBody List<TaskShort> tasksShort(
        @RequestBody TaskType taskType, HttpServletRequest req
    ) {
        return ds.q("""
            select
                tsk.id, tsk.name, tsk.status,
                (select tg_name from manager m where m.id = tsk.leader ) ":t? tg_name",
                tsk.create_date                ,
                extract(
                    day from (now()::timestamp without time zone) - tsk.status_date)::int  status_days_count,
                (select count(*) from volunteer v
                    where st_dwithin(v.coordinates, tsk.coordinates, v.radius, true) and not v.is_banned and v.status = 'Active'
                    )::int volunteer_count  ,
                st_distance(tsk.coordinates, (select mv.coordinates from volunteer mv where tg_name = ?), true)::int manager_distance,
                (select count(*) from task_photo tp
                    where tsk.id = tp.task_id)                       photo_count      ,
                tsk.chat_link is not null has_chat_link,
                tg.id      "Tag tags.",
                tg.name     "   tags.",
                tst.name      "systemTags.   "
            from task tsk
                left join v_task_users_tags tg on tsk.id = tg.task_id
                left join v_task_system_tags tst on tsk.id = tst.task_id
            where tsk.type = ? and tsk.status != 'Archive'""", ((AuthInterceptor.Authentication) req.getSession(false).getAttribute(AUTH_ATTR)).username(), taskType
        ).g.fetchList(TaskShort.class);
    }

    @PostMapping("/getArchiveTaskYears") List<Integer> getArchiveTaskYears() {
        return ds.q("""
            select distinct extract(year from create_date)::int as year
            from task
            order by extract(year from create_date)::int desc
            """).fetchList(Integer.class);
    }

    @PostMapping("/getArchiveTasksShort") @ResponseBody
    List<ArchiveTask> getArchiveTasksShort(@RequestBody int year) {

        return ds.q("""
            select
                tsk.id,
                tsk.name,
                tsk.status_date as archive_date,
                tsk.status,
                (select count(*) from task_volunteer_done tvd where tvd.task_id = tsk.id) as volonteer_count,
                (select tg_name from manager m where m.id = tsk.leader) ":t? leader_tg_name"
            from task tsk
            where tsk.status = 'Archive'
                and extract(year from tsk.create_date)::int = ?
            """, year).g.fetchList(ArchiveTask.class);
    }

    @Autowired
    ImagesService imagesService;

    @PostMapping(value = "/photoUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PhotoCredentials upload(
        @RequestParam("file") MultipartFile photo, @RequestParam int taskId
    ) throws IOException {
        try (var input = photo.getInputStream()) {
            return imagesService.upload(taskId, input, photo.getOriginalFilename());
        }
    }

    @PostMapping("/photoDelete") void delete(@RequestBody  int id) throws IOException {
        imagesService.delete(id);
    }
}
