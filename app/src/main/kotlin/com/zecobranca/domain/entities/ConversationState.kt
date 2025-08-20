package com.zecobranca.domain.entities

data class ConversationState(
  val userId: String,
  val step: ConversationStepEnum = ConversationStepEnum.MENU_INICIAL,
)
