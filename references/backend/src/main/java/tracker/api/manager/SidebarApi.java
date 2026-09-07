package tracker.api.manager;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import tracker.AuthInterceptor.Authentication;

import static tracker.AuthInterceptor.AUTH_ATTR;

@RequestMapping("/api/manager/sidebar")
@RestController public class SidebarApi {

    @PostMapping("/currentUser") @ResponseBody
    @Nullable Authentication currentUser(HttpServletRequest req) {
        return (Authentication) req.getSession().getAttribute(AUTH_ATTR);
    }
}
