package tracker;

import com.zaxxer.hikari.HikariDataSource;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.DataSourceW;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@SpringBootConfiguration
@SpringBootApplication public class TrackerApplication implements WebMvcConfigurer {

    @Autowired AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**");
    }

    public enum VolunteerStatus {Active, InVacation}
    public enum TaskType {Social, Ecological}
    public enum TaskStatus {New, InProcessing, InPlan, Done, Inactive, Archive}

    public static class TaskStatusRW extends PgEnumRW<TaskStatus> {
        public TaskStatusRW() { super(TaskStatus.class); }
    }
    public static class TaskTypeRW extends PgEnumRW<TaskType> {
        public TaskTypeRW() { super(TaskType.class); }
    }
    public static class VolunteerStatusRW extends PgEnumRW<VolunteerStatus> {
        public VolunteerStatusRW() { super(VolunteerStatus.class); }
    }

    @Configuration(
        checks = @CompileTimeChecks(
            url = "jdbc:postgresql://host.docker.internal:5432/tracker",
            username = "site",
            password = "f_chc87Y#C&-Hc_Etu-32gr87N-nyfynhmisgg"
        ),
        typeBindings = {
            @TypeBinding(
                compatibleSqlTypeName = "task_status",
                rw = TaskStatusRW.class
            ),
            @TypeBinding(
                compatibleSqlTypeName = "task_type",
                rw = TaskTypeRW.class
            ),
            @TypeBinding(
                compatibleSqlTypeName = "volunteer_status",
                rw = VolunteerStatusRW.class
            )
        }
    ) public static class MainDb extends DataSourceW {
        public MainDb(DataSource w) { super(w); }
    }

    @Bean MainDb mainDb(
        @Value("${datasource.url}") String url,
        @Value("${datasource.username}") String username,
        @Value("${datasource.password}") String password
    ) {
        return new MainDb(new HikariDataSource() {{
            setJdbcUrl(url);
            setUsername(username);
            setPassword(password);
            setMaximumPoolSize(10);
        }});
    }

    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }
}
