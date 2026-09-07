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
class ProfileActiveRadiusSelectView : BotView {

    override fun invoke(update: Update): SendMessage = SendMessage(update.message.chatId.toString(), message)
        .apply { replyMarkup = menu }

    override fun getType(): State = State.ProfileActiveRadiusSelect

    companion object {
        private val message = """
            Выбери расстояние от вашей локации, на котором вы можете оказывать волонтерскую помощь.
            Вам будут предлагать мероприятия в вашем радиусе активности. Мы рекомендуем радиус 25 км для больших
            городов и 50-100 км для городов поменьше и сельской местности.
            Если вам предлагают слишком много мероприятий рекомендуем увеличить радиус активности,
            если слишком мало - увеличить.
        """.trimIndent()
        private val menu = ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply {
                    add(KeyboardButton(ActiveRadius.KILOMETRES_5.text))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(ActiveRadius.KILOMETRES_10.text))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(ActiveRadius.KILOMETRES_25.text))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(ActiveRadius.KILOMETRES_50.text))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(ActiveRadius.KILOMETRES_100.text))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_BACK.commandText))
                },
                KeyboardRow().apply {
                    add(KeyboardButton(CommandType.RETURN_MAIN_MENU.commandText))
                }
            )
            resizeKeyboard = true
        }
    }
}

enum class ActiveRadius(val text: String, val value: Int) {
    KILOMETRES_5("5 км", 5000),
    KILOMETRES_10("10 км", 10000),
    KILOMETRES_25("25 км", 25000),
    KILOMETRES_50("50 км", 50000),
    KILOMETRES_100("100 км", 100000);

    companion object {
        fun findByText(text: String): ActiveRadius = ActiveRadius.entries
            .first { it.text == text }
    }
}
