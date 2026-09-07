package bot.telegram.processor.terminating

import bot.telegram.state.machine.State
import org.telegram.telegrambots.meta.api.objects.Update

interface TerminatingProcessor {
    fun invoke(update: Update)
    fun getType(): State
}
