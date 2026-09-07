package bot.telegram.command.profile

import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class ProfileAddressInputView : AddressInputView(message) {

    override fun getType(): State = State.ProfileAddressInput

    companion object {
        private val message = """
            Отправьте и подтвердите свой адрес. Для конфиденциальности, точный адрес указывать нет
            необходимости, мы рекомендуем указывать адрес с точностью до улицы
            для больших городов или название города/населенного пункта для меньших
            поселений.
        """.trimIndent()
    }
}
