package bot.telegram.command.profile

import bot.isOpenProfile
import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.State
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class ProfileMenuView : BotView {

    override fun invoke(update: Update): SendMessage = when (update.message.isOpenProfile()) {
        true -> SendMessage(update.message.chatId.toString(), acceptMessage)
            .apply { replyMarkup = acceptMenu }
        false -> SendMessage(update.message.chatId.toString(), rejectMessage)
            .apply { replyMarkup = rejectMenu }
    }

    override fun getType(): State = State.ProfileMenu

    companion object {

        private val acceptMessage = """
            Для того, чтобы стать волонтёром и получать приглашения на волонтёрские мероприятия, нужно указать вашу локацию и расстояние, на котором вам будут доступны мероприятия.
        """.trimIndent()
        private val acceptMenu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.PROFILE_ENTER_ADDRESS.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val rejectMessage = """
            К сожалению мы не можем зарегистрировать вас в качестве волонтера. Для регистрации
            в качестве волонтера профиль телеграмм должен быть открытый. Вы можете создать отдельный
            профиль телеграмм для регистрации в качестве волонтера
        """.trimIndent()
        private val rejectMenu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }
    }
}
