package bot.telegram.state.machine.context

import bot.telegram.state.machine.Event
import bot.telegram.state.machine.SideEffect
import bot.telegram.state.machine.State
import com.tinder.StateMachine

data class UserContext(
    val state: StateMachine<State, Event, SideEffect>,
    var profile: Profile = Profile(),
    var isVocation: Boolean? = null,
    var ecologicalProblem: EcologicalProblem = EcologicalProblem(),
    var socialProblem: SocialProblem = SocialProblem()
) {
    fun clear() {
        this.profile = Profile()
        this.isVocation = null
        this.ecologicalProblem = EcologicalProblem()
        this.socialProblem = SocialProblem()
    }
}
