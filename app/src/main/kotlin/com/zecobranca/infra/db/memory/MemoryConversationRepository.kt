package com.zecobranca.infra.db.memory

import com.zecobranca.domain.entities.ConversationState
import java.util.concurrent.ConcurrentHashMap

class MemoryConversationRepository {
  private val states = ConcurrentHashMap<String, ConversationState>()

  fun loadByUserId(userId: String): ConversationState? = states[userId]

  fun save(state: ConversationState): ConversationState {
    states[state.userId] = state
    return state
  }
}
