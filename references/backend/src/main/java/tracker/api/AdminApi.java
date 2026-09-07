package tracker.api;

import jakarta.validation.ValidationException;
import net.truej.sql.TrueSql;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tracker.api.AdminApiG.*;
import java.util.List;

import static tracker.TrackerApplication.*;

@RequestMapping("/api/admin")
@TrueSql @RestController public class AdminApi {
    @Autowired MainDb ds;

    @PostMapping("/add") Integer add(@RequestBody @NotNull Object tgName) {

        var id = ds.q("""
            select id from volunteer where tg_name = ?""", (String) tgName).fetchOneOrZero(int.class);

        if (id == null)
            throw new ValidationException("Нет волонтёра с таким tgName");

        return ds.q("""
            insert into manager
            values(default, ?, false)
            """, (String) tgName).asGeneratedKeys("id").fetchOne(int.class);
    }

    @PostMapping("/delete") @ResponseBody void delete(@RequestBody int managerId) {
        ds.q("""
            delete from manager
            where id = ?
            """, managerId).fetchNone();
    }

    @GetMapping("/managers") List<Manager> managers() {
        return ds.q("""
            select
                id,
                tg_name,
                is_admin
            from manager
        """).g.fetchList(Manager.class);
    }
}
