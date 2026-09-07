package bot.telegram.command.profile

import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class SocialProblemAddressInputView : AddressInputView(message) {

    override fun getType(): State = State.SocialProblemAddressInput

    companion object {
        private val message = """
            Отправьте и подтвердите адрес соц. проблемы (адрес по которому нужно оказать помощь).
        """.trimIndent()
    }
}
