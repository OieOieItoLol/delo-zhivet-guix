package bot.telegram.command.vocation

import bot.domain.volunteer.VolunteerService
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
class VocationMenuView(private val volunteerService: VolunteerService) : BotView {
    override fun invoke(update: Update): SendMessage {
        val volunteer = volunteerService.findByTgName(update.message.from.userName)
        return if (volunteer != null) {
            SendMessage(update.message.chatId.toString(), message)
                .apply { replyMarkup = menu }
        } else {
            SendMessage(update.message.chatId.toString(), rejectedMessage)
                .apply { replyMarkup = rejectedMenu }
        }
    }

    override fun getType(): State = State.VocationMenu

    companion object {
        private val message = """
            Чтобы перестать получать приглашения на мероприятия, включите режим отпуска.
            Чтобы вновь начать получать приглашения на мероприятия, выключите режим отпуска.
        """.trimIndent()

        private val rejectedMessage = """
            Вы ещё не зарегестрированы в качестве волонтера
        """.trimIndent()

        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.VOCATION_ENABLE.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.VOCATION_DISABLE.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }

        private val rejectedMenu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }
    }
}
