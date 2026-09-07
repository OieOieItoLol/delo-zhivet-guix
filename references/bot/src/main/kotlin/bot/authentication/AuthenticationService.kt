package bot.authentication

import bot.domain.volunteer.VolunteerService
import bot.telegram.TelegramBot
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class AuthenticationService(private val volunteerService: VolunteerService, private val telegramBot: TelegramBot) {

    fun sendVerificationCode(tgUserName: String, verificationCode: Int) {
        volunteerService.findByTgName(tgUserName)?.let {
            val message = "Код для авторизации: $verificationCode"
            telegramBot.sendMessage(
                SendMessage(it.tgChannelId.toString(), message)
            )
        } ?: throw IllegalArgumentException("Volunteer with tgUserName $tgUserName not found")
    }
}
