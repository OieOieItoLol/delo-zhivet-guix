package bot.telegram.state.machine.context

import bot.telegram.state.machine.StateMachineFabric
import org.springframework.stereotype.Component

@Component
class BotContext(private val context: MutableMap<Long, UserContext> = mutableMapOf(), private val stateMachineFabric: StateMachineFabric) {
    fun getOrCreate(chatId: Long): UserContext = context.computeIfAbsent(chatId) {
        UserContext(stateMachineFabric.create())
    }
}
