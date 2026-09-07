package bot.telegram.command.ecological

import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class EcologicalProblemPhotosInputView : PhotosInputView() {

    override fun getType(): State = State.EcologicalProblemPhotosInput
}
