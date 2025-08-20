package com.zecobranca.domain.usecases

import com.zecobranca.domain.entities.ChatMessage

interface SendMessage {
  suspend fun send(message: ChatMessage): Boolean
}
