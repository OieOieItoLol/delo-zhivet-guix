package tracker.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import net.truej.sql.TrueSql;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tracker.AuthInterceptor;
import tracker.AuthInterceptor.Authentication;
import tracker.api.AuthApiG.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static tracker.AuthInterceptor.AUTH_ATTR;
import static tracker.TrackerApplication.*;

@RequestMapping("/api/auth")
@TrueSql @RestController public class AuthApi {
    public AuthApi() throws URISyntaxException { }

    public record TgCode(
        int code, @NotNull LocalDateTime expireTime
    ) { }

    public record UserCodeRequest(
        @NotNull String tgName, int code
    ) { }

    record User(int id, String tgName) {}

    private static final String USER_ATTR = "TRACKER_APP_USER";

    @Value("${credentials.user.auth.code.lifetime}")
    private int codeLifetimeSeconds;

    @Value("${credentials.backend.auth.token}")
    private String backendSecret;

    @Value("${bot.uri}")
    private String botUrl;

    private final ConcurrentHashMap<Integer, TgCode> userTgCode = new ConcurrentHashMap<>();

    private final HttpClient botHttpClient = HttpClient.newHttpClient();
    private final ObjectMapper objMapper = new ObjectMapper();

    public enum Role {Manager, Admin}

    @Autowired MainDb ds;

    @PostMapping("/requestCode") void requestCode(
        HttpServletRequest req, @RequestBody @NotNull Object tgName
    ) throws IOException, InterruptedException {

        var identity = ds.q("""
            select
                id as user_id,
                tg_name
            from manager
            where tg_name = ?""", (String) tgName
        ).g.fetchOneOrZero(ManagerIdentifiers.class);

        if (identity == null) throw new ValidationException("Пользователь не найден");

        req.getSession(true).setAttribute(USER_ATTR, new User(
            identity.userId, identity.tgName
        ));

        if (
            userTgCode.get(identity.userId) != null &&
            userTgCode.get(identity.userId).expireTime.isAfter(LocalDateTime.now())
        ) return;

        var code = (new Random()).nextInt(999999);

        String form =
            "?tgName=" + URLEncoder.encode((String) tgName, StandardCharsets.UTF_8)
            + "&code=" + URLEncoder.encode(String.valueOf(code), StandardCharsets.UTF_8);

        botHttpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(botUrl + form))
                .setHeader("Authorization", backendSecret)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.noBody()).build(),
            HttpResponse.BodyHandlers.discarding()
        );

        userTgCode.put(
            identity.userId, new TgCode(
                code, LocalDateTime.now().plusSeconds(codeLifetimeSeconds)
            )
        );
    }

    @PostMapping("/login") void login(
        HttpServletRequest req, @RequestBody int code
    ) {

        var user = (User) req.getSession().getAttribute(USER_ATTR);
        if (user == null)
            throw new ValidationException("Нужно получить код");

        var expectedCode = userTgCode.get(user.id);

        if (expectedCode == null)
            throw new ValidationException("Нужно получить код");

        if (expectedCode.code != code)
            throw new ValidationException("Код не совпадает");

        if (expectedCode.expireTime.isBefore(LocalDateTime.now()))
            throw new ValidationException("Время действия кода истекло");

        var isAdmin = ds.q("select is_admin from manager where id = ?", user.id)
            .fetchOne(boolean.class);

        req.getSession().setAttribute(AUTH_ATTR, new Authentication(
            user.tgName, isAdmin ? Role.Admin : Role.Manager
        ));
    }

    @PostMapping("/logout") void logout(HttpServletRequest req) {
        req.getSession().removeAttribute(AUTH_ATTR);
    }

    @PostMapping("/isAdmin") @ResponseBody boolean isAdmin(HttpServletRequest req) {
        return Role.Admin == ((Authentication) req.getSession(false).getAttribute(AUTH_ATTR)).role();
    }

}

