package com.zecobranca.main.factories.usescases
import com.zecobranca.domain.usecases.ProcessWebhookMessageData
import com.zecobranca.main.factories.repositories.ConversationRepositoryFactory

object ProcessWebhookMessageFactory {
  fun make(): ProcessWebhookMessageData {
    val conversationRepository = ConversationRepositoryFactory.make()

    return ProcessWebhookMessageData(

      sendMessage = SendMessageFactory.make(),
      conversationRepository,
    )
  }
}
