package bot.telegram.command.profile

import bot.telegram.state.machine.State
import bot.telegram.state.machine.context.BotContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ProfileAddressValidationView(private val botContext: BotContext) : AddressValidationView() {

    override fun getType(): State = State.ProfileAddressValidation

    override fun getAddress(update: Update): String? = botContext.getOrCreate(update.message.chatId).profile.address
}
