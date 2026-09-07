package bot.telegram.state.machine

import bot.isOpenProfile
import bot.telegram.processor.intermediate.IntermediateProcessor
import bot.telegram.processor.terminating.TerminatingProcessor
import com.tinder.StateMachine
import mu.KotlinLogging
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

sealed class State {
    object MainMenu : State()
    object ProfileMenu : State()
    object ProfileAddressInput : State()
    object ProfileAddressValidation : State()
    object ProfileActiveRadiusSelect : State()
    object ProfileUpdated : State()
    object EcologicalProblemMenu : State()
    object EcologicalProblemProfileClosed : State()
    object EcologicalProblemLocationInput : State()
    object EcologicalProblemPhotosInput : State()
    object EcologicalProblemTagsInput : State()
    object EcologicalProblemCommentaryInput : State()
    object EcologicalProblemCreated : State()
    object SocialProblemMenu : State()
    object SocialProblemProfileClosed : State()
    object SocialProblemAddressInput : State()
    object SocialProblemAddressValidation : State()
    object SocialProblemPhotosInput : State()
    object SocialProblemTagsInput : State()
    object SocialProblemCommentaryInput : State()
    object SocialProblemCreated : State()
    object SupportMenu : State()
    object VocationMenu : State()
    object VocationUpdated : State()
}

sealed class Event(open val update: Update) {
    data class OnStart(override val update: Update) : Event(update)
    data class OnProfileMenuSelected(override val update: Update) : Event(update)
    data class OnProfileEnterAddress(override val update: Update) : Event(update)
    data class OnEcologicalProblemMenuSelected(override val update: Update) : Event(update)
    data class OnEcologicalProblemSendSelected(override val update: Update) : Event(update)
    data class OnSocialProblemMenuSelected(override val update: Update) : Event(update)
    data class OnSocialProblemSendSelected(override val update: Update) : Event(update)
    data class OnAcceptWithoutCommentary(override val update: Update) : Event(update)
    data class OnSupportMenuSelected(override val update: Update) : Event(update)
    data class OnVocationMenuSelected(override val update: Update) : Event(update)
    data class OnVocationChanged(override val update: Update) : Event(update)
    data class OnReturnMainMenu(override val update: Update) : Event(update)
    data class OnReturnBack(override val update: Update) : Event(update)
    data class OnAccept(override val update: Update) : Event(update)
    data class OnReject(override val update: Update) : Event(update)
    data class OnNext(override val update: Update) : Event(update)
    data class OnCommonMessage(override val update: Update) : Event(update)
}

sealed class SideEffect {
    object ApplyIntermediateProcessor : SideEffect()
    object ApplyTerminatingProcessor : SideEffect()
    object ApplyAllProcessor : SideEffect()
}

@Component
class StateMachineFabric(
    private val intermediateProcessorsProvider: ObjectProvider<Map<State, IntermediateProcessor>>,
    private val terminatingProcessorsProvider: ObjectProvider<Map<State, TerminatingProcessor>>
) {

    fun create(): StateMachine<State, Event, SideEffect> {
        return StateMachine.create {
            initialState(State.MainMenu)

            state<State.MainMenu> {
                on<Event.OnProfileMenuSelected> { event ->
                    transitionTo(State.ProfileMenu)
                }
                on<Event.OnEcologicalProblemMenuSelected> { event ->
                    transitionTo(State.EcologicalProblemMenu)
                }
                on<Event.OnSocialProblemMenuSelected> { event ->
                    transitionTo(State.SocialProblemMenu)
                }
                on<Event.OnVocationMenuSelected> { event ->
                    transitionTo(State.VocationMenu)
                }
                on<Event.OnSupportMenuSelected> { event ->
                    transitionTo(State.SupportMenu)
                }
                on<Event> { event ->
                    transitionTo(State.MainMenu)
                }
            }

            // Логика изменения профиля
            state<State.ProfileMenu> {
                on<Event.OnProfileEnterAddress> { event ->
                    transitionTo(State.ProfileAddressInput, SideEffect.ApplyIntermediateProcessor)
                }
                on<Event.OnStart> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.MainMenu)
                }
            }
            state<State.ProfileAddressInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.ProfileMenu)
                }
                on<Event> { event ->
                    transitionTo(State.ProfileAddressValidation, SideEffect.ApplyIntermediateProcessor)
                }
            }
            state<State.ProfileAddressValidation> {
                on<Event.OnAccept> { event ->
                    transitionTo(State.ProfileActiveRadiusSelect)
                }
                on<Event.OnReject> { event ->
                    transitionTo(State.ProfileAddressInput)
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.ProfileAddressInput)
                }
                on<Event> { event ->
                    transitionTo(State.ProfileAddressValidation)
                }
            }
            state<State.ProfileActiveRadiusSelect> {
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.ProfileAddressInput)
                }
                on<Event> { event ->
                    transitionTo(State.ProfileUpdated, SideEffect.ApplyAllProcessor)
                }
            }
            state<State.ProfileUpdated> {
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.ProfileActiveRadiusSelect)
                }
                on<Event> { event ->
                    transitionTo(State.ProfileAddressValidation)
                }
            }

            // Экологические проблемы
            state<State.EcologicalProblemMenu> {
                on<Event.OnEcologicalProblemSendSelected> { event ->
                    if (event.update.message.isOpenProfile()) {
                        transitionTo(State.EcologicalProblemLocationInput)
                    } else {
                        transitionTo(State.EcologicalProblemProfileClosed)
                    }
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.MainMenu)
                }
            }
            state<State.EcologicalProblemProfileClosed> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.EcologicalProblemMenu)
                }
                on<Event> { event ->
                    transitionTo(State.EcologicalProblemLocationInput, SideEffect.ApplyIntermediateProcessor)
                }
            }
            state<State.EcologicalProblemLocationInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.EcologicalProblemMenu)
                }
                on<Event> { event ->
                    if (event.update.message.location == null) {
                        transitionTo(State.EcologicalProblemLocationInput)
                    } else {
                        transitionTo(State.EcologicalProblemPhotosInput, SideEffect.ApplyIntermediateProcessor)
                    }
                }
            }
            state<State.EcologicalProblemPhotosInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.EcologicalProblemLocationInput)
                }
                on<Event> { event ->
                    if (event.update.message.photo.isNullOrEmpty()) {
                        transitionTo(State.EcologicalProblemPhotosInput)
                    } else {
                        transitionTo(State.EcologicalProblemTagsInput, SideEffect.ApplyIntermediateProcessor)
                    }
                }
            }
            state<State.EcologicalProblemTagsInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.EcologicalProblemPhotosInput)
                }
                on<Event.OnNext> { event ->
                    transitionTo(State.EcologicalProblemCommentaryInput, SideEffect.ApplyIntermediateProcessor)
                }
                on<Event> { event ->
                    transitionTo(State.EcologicalProblemTagsInput, SideEffect.ApplyIntermediateProcessor)
                }
            }
            state<State.EcologicalProblemCommentaryInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.EcologicalProblemPhotosInput)
                }
                on<Event.OnAcceptWithoutCommentary> { event ->
                    transitionTo(State.EcologicalProblemCreated, SideEffect.ApplyTerminatingProcessor)
                }
                on<Event> { event ->
                    transitionTo(State.EcologicalProblemCreated, SideEffect.ApplyAllProcessor)
                }
            }
            state<State.EcologicalProblemCreated> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
            }

            // Социальные проблемы
            state<State.SocialProblemMenu> {
                on<Event.OnSocialProblemSendSelected> { event ->
                    if (event.update.message.isOpenProfile()) {
                        transitionTo(State.SocialProblemAddressInput)
                    } else {
                        transitionTo(State.SocialProblemProfileClosed)
                    }
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.MainMenu)
                }
            }
            state<State.SocialProblemProfileClosed> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemMenu)
                }
                on<Event> { event ->
                    transitionTo(State.SocialProblemAddressInput, SideEffect.ApplyIntermediateProcessor)
                }
            }
            state<State.SocialProblemAddressInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemMenu)
                }
                on<Event> { event ->
                    if (event.update.message.text.isNullOrEmpty()) {
                        transitionTo(State.SocialProblemAddressInput)
                    } else {
                        transitionTo(State.SocialProblemAddressValidation, SideEffect.ApplyIntermediateProcessor)
                    }
                }
            }
            state<State.SocialProblemAddressValidation> {
                on<Event.OnAccept> { event ->
                    transitionTo(State.SocialProblemPhotosInput)
                }
                on<Event.OnReject> { event ->
                    transitionTo(State.SocialProblemAddressInput)
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemAddressInput)
                }
                on<Event> { event ->
                    transitionTo(State.SocialProblemAddressValidation)
                }
            }
            state<State.SocialProblemPhotosInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemAddressInput)
                }
                on<Event> { event ->
                    if (event.update.message.photo.isNullOrEmpty()) {
                        transitionTo(State.SocialProblemPhotosInput)
                    } else {
                        transitionTo(State.SocialProblemTagsInput, SideEffect.ApplyIntermediateProcessor)
                    }
                }
            }
            state<State.SocialProblemTagsInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemPhotosInput)
                }
                on<Event.OnNext> { event ->
                    transitionTo(State.SocialProblemCommentaryInput, SideEffect.ApplyIntermediateProcessor)
                }
                on<Event> { event ->
                    transitionTo(State.SocialProblemTagsInput, SideEffect.ApplyIntermediateProcessor)
                }
            }
            state<State.SocialProblemCommentaryInput> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
                on<Event.OnReturnBack> { event ->
                    transitionTo(State.SocialProblemPhotosInput)
                }
                on<Event.OnAcceptWithoutCommentary> { event ->
                    transitionTo(State.SocialProblemCreated, SideEffect.ApplyTerminatingProcessor)
                }
                on<Event> { event ->
                    transitionTo(State.SocialProblemCreated, SideEffect.ApplyAllProcessor)
                }
            }
            state<State.SocialProblemCreated> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
            }

            state<State.SupportMenu> {
                on<Event> { event ->
                    transitionTo(State.MainMenu)
                }
            }

            state<State.VocationMenu> {
                on<Event.OnVocationChanged> { event ->
                    transitionTo(State.VocationUpdated, SideEffect.ApplyAllProcessor)
                }
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
            }
            state<State.VocationUpdated> {
                on<Event.OnReturnMainMenu> { event ->
                    transitionTo(State.MainMenu)
                }
            }

            onTransition {
                val validTransition = it as? StateMachine.Transition.Valid ?: return@onTransition
                val intermediateProcessors = intermediateProcessorsProvider.getObject()
                val terminatingProcessors = terminatingProcessorsProvider.getObject()

                val applyIntermediateProcessor = { transition: StateMachine.Transition.Valid<State, Event, SideEffect> ->
                    intermediateProcessors[transition.fromState]?.invoke(validTransition.event.update)
                        ?: throw IllegalStateException("Intermediate processor for state ${validTransition.fromState} not implemented")
                }
                val applyTerminatingProcessor = { transition: StateMachine.Transition.Valid<State, Event, SideEffect> ->
                    terminatingProcessors[transition.toState]?.invoke(validTransition.event.update)
                        ?: throw IllegalStateException("Terminating processor for state ${validTransition.fromState} not implemented")
                }

                when (validTransition.sideEffect) {
                    SideEffect.ApplyIntermediateProcessor -> applyIntermediateProcessor(validTransition)
                    SideEffect.ApplyTerminatingProcessor -> applyTerminatingProcessor(validTransition)
                    SideEffect.ApplyAllProcessor -> applyIntermediateProcessor(validTransition)
                        .also { applyTerminatingProcessor(validTransition) }
                    null -> Unit
                }
                log.info { "Transaction from state ${validTransition.fromState} to state ${validTransition.toState}" }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
