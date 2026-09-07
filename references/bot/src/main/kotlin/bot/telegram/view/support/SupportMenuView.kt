package bot.telegram.command.support

import bot.telegram.command.BotView
import bot.telegram.command.featureNotReadyMessage
import bot.telegram.command.mainMenu
import bot.telegram.state.machine.State
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SupportMenuView : BotView {
    override fun invoke(update: Update): SendMessage = SendMessage(
        update.message.chatId.toString(),
        featureNotReadyMessage
    ).apply { replyMarkup = mainMenu }

    override fun getType(): State = State.SupportMenu
}
