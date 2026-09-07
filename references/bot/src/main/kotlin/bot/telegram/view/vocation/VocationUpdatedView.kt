package bot.telegram.command.vocation

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class VocationUpdatedView(private val botContext: BotContext) : BotView {

    override fun invoke(update: Update): SendMessage {
        val userContext = botContext.getOrCreate(update.message.chatId)
        val isVocation = userContext.isVocation!!
        val message = if (isVocation) enableMessage else disableMessage

        return SendMessage(update.message.chatId.toString(), message)
            .apply { replyMarkup = menu }
    }

    override fun getType(): State = State.VocationUpdated

    companion object {
        private val enableMessage = """
            Режим отпуска активирован, вам не будут приходить приглашения на новые мероприятия.
            Чтобы выйти из режима отпуска воспользуйтесь кнопкой Выйти из режима отпуска в меню Режим отпуска.
        """.trimIndent()
        private val disableMessage = """
            Вы вышли из отпуска, теперь вам вновь будут приходить приглашения на новые мероприятия.
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
