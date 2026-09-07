package bot.telegram.command.ecological

import bot.telegram.command.common.ProfileClosedView
import bot.telegram.state.machine.State
import org.springframework.stereotype.Component

@Component
class EcologicalProblemProfileClosedView : ProfileClosedView() {

    override fun getType(): State = State.EcologicalProblemProfileClosed
}
