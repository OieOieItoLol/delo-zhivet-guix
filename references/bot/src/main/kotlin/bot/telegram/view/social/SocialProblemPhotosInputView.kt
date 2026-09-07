package bot.telegram.command.ecological

import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class SocialProblemPhotosInputView : PhotosInputView() {

    override fun getType(): State = State.SocialProblemPhotosInput
}
