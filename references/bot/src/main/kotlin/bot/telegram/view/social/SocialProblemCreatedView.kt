package bot.telegram.command.ecological

import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class SocialProblemCreatedView : ProblemCreatedView() {

    override fun getType(): State = State.SocialProblemCreated
}
