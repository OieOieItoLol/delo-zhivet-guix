package tracker;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest @AutoConfigureMockMvc @Testcontainers
public class Runner {
    @Container static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16.3");

    @DynamicPropertySource
    static void connectionProperties(DynamicPropertyRegistry registry) {
        new Flyway(
            new ClassicConfiguration() {{
                setUrl(postgres.getJdbcUrl());
                setUser(postgres.getUsername());
                setPassword(postgres.getPassword());
            }}
        ).migrate();

        registry.add("datasource.url", postgres::getJdbcUrl);
        registry.add("datasource.username", postgres::getUsername);
        registry.add("datasource.password", postgres::getPassword);
    }
}
