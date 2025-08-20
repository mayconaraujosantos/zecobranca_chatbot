package com.zecobranca.main.factories.repositories

import com.zecobranca.infra.db.memory.MemoryConversationRepository

object ConversationRepositoryFactory {
  private val instance = MemoryConversationRepository()

  fun make(): MemoryConversationRepository = instance
}
