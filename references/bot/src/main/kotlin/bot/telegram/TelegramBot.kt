package bot.telegram

import bot.telegram.command.BotView
import bot.telegram.state.machine.CommandType
import bot.telegram.state.machine.Event
import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import com.tinder.StateMachine
import mu.KotlinLogging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class TelegramBot(
    botToken: String,
    private val botUsername: String,
    private val botViewMap: Map<State, BotView>,
    private val botContext: BotContext
) : TelegramLongPollingBot(botToken) {

    override fun getBotUsername(): String = botUsername

    override fun onUpdateReceived(update: Update) {
        log.info { "Receive message ${update.message}" }
        val context = botContext.getOrCreate(update.message.chatId)

        val stateMachine = context.state
        val command = update.message.text?.let { text -> CommandType.findByCommandTextOrNull(text) }
        val event = command?.eventFactory?.invoke(update) ?: Event.OnCommonMessage(update)

        val transaction = stateMachine.transition(event)
        if (transaction is StateMachine.Transition.Valid) {
            val state = stateMachine.state
            val sendMessage = botViewMap[state]!!.invoke(update)
            execute(sendMessage)
        }
    }

    fun sendMessage(message: SendMessage) = execute(message)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
