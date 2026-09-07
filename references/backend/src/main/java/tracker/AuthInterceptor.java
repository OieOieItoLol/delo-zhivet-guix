package tracker;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


import static tracker.api.AuthApi.*;
import static tracker.api.AuthApi.Role.Admin;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTH_ATTR = "TRACKER_APP_AUTH";

    @Value("${credentials.bot.auth.token}")
    private String botSecret;

    public record Authentication(
        String username, Role role
    ) { }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        var path = req.getServletPath();

        if (
            path.equals("/error") ||
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/docs") ||
            path.startsWith("/api/auth")
        ) return true;

        var botAuth = req.getHeader("Authorization");

        if (path.startsWith("/api/bot") && botSecret.equals(botAuth))
            return true;

        var siteAuth = (Authentication) req.getSession().getAttribute(AUTH_ATTR);
        if (
            (path.startsWith("/api/admin") && siteAuth.role == Admin) ||
            (path.startsWith("/api/manager") && siteAuth != null)
        ) return true;

        if (path.startsWith("/images/") &&
            (botSecret.equals(botAuth) || siteAuth != null)
        ) return true;

        res.setStatus(401);
        return false;
    }
}
