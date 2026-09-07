package bot.authentication

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(private val service: AuthenticationService) {

    @PostMapping("/sendAuthCode")
    fun sendVerificationCode(tgName: String, code: Int) = service.sendVerificationCode(tgName, code)
}
