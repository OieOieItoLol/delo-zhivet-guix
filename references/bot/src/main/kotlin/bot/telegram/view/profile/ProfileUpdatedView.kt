package bot.telegram.command.profile

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
class ProfileUpdatedView : BotView {

    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    override fun getType(): State = State.ProfileUpdated

    companion object {
        private val message = """
            Профиль волонтера успешно создан/обновлен.

            Ждите приглашения на мероприятия в вашем телеграмм.
        """.trimIndent()

        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }
    }
}
