package tracker.api.manager;


import jakarta.validation.ValidationException;
import net.truej.sql.TrueSql;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tracker.api.manager.VolunteerApiG.*;


import java.time.LocalDateTime;
import java.util.List;

import static tracker.TrackerApplication.*;

@RequestMapping("/api/manager/volunteer")
@TrueSql @RestController public class VolunteerApi {

    @Autowired MainDb ds;

    record EditNoteRequest(int id, String note) { }

    @PostMapping("/editNote") @ResponseBody void editNote(@RequestBody EditNoteRequest e) {
        ds.q("""
            update volunteer
            set note = ?
            where id = ?
            """, e.note, e.id
        ).fetchNone();
    }

    record ChangeVolunteerStatus (
        int volunteerId,
        boolean isBanned
    ) {}

    @PostMapping("/ban") @ResponseBody void changeStatus(@RequestBody ChangeVolunteerStatus c) {
        ds.q("""
            update volunteer
            set is_banned = ?
            where id = ?
        """, c.isBanned, c.volunteerId).fetchNone();
    }

    @PostMapping("/get") @ResponseBody Volunteer get(@RequestBody int volunteerId) {
        return ds.q("""
            select
            	v.id,
            	v.tg_name,
            	v.register_date,
            	v.status,
            	v.is_banned,
            	v.note,
            	m.id is not null is_manager,
            	m.id as ":t? id_manager",
            	st_x(v.coordinates) longitude,
                st_y(v.coordinates) latitude,
            	v.radius,
            	tsk.id                "Task tasks.",
            	tsk.name              "     tasks.",
            	st_x(tsk.coordinates) "     tasks.longitude",
            	st_y(tsk.coordinates) "     tasks.latitude"
            from volunteer v
            	left join task tsk on st_dwithin(v.coordinates, tsk.coordinates, v.radius, true) and tsk.status != 'Archive'
            	left join manager m on v.tg_name = m.tg_name
            where v.id = ?
            """, volunteerId
        ).g.fetchOne(Volunteer.class);
    }

    @PostMapping("/volunteersShort") @ResponseBody List<VolunteerShort> volunteersShort() {
        return ds.q("""
            select
                id,
                tg_name,
                register_date,
                status,
                is_banned,
                (select count(*) from task tsk where st_dwithin(v.coordinates, tsk.coordinates, v.radius, true) and tsk.status != 'Archive')::int active_tasks_count,
                (select count(*) from task_volunteer_done tvd where tvd.volunteer_id = v.id)::int finished_tasks_count
            from volunteer v"""
        ).g.fetchList(VolunteerShort.class);
    }
}
