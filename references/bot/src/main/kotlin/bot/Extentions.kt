package bot

import org.telegram.telegrambots.meta.api.objects.Message

fun Message.isOpenProfile(): Boolean = this.from.userName != null
