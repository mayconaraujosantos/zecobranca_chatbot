package com.zecobranca.domain.usecases

import com.zecobranca.domain.entities.ConversationState
import com.zecobranca.domain.entities.ConversationStepEnum
import com.zecobranca.domain.entities.WebhookMessage
import com.zecobranca.infra.db.memory.MemoryConversationRepository

data class ProcessResult(val success: Boolean, val error: String? = null)

class ProcessWebhookMessageData(
  private val sendMessage: SendMessage,
  private val conversationRepo: MemoryConversationRepository,
) {

  suspend fun process(webhookMessage: WebhookMessage): ProcessResult {
    val from = webhookMessage.from ?: return ProcessResult(false, "No sender")
    val text = webhookMessage.body?.trim() ?: ""
    val instanceId = webhookMessage.instanceId ?: ""

    // Carrega ou cria estado da conversa
    val state = conversationRepo.loadByUserId(from) ?: ConversationState(userId = from)

    val (reply, newStep) = when (state.step) {
      ConversationStepEnum.MENU_INICIAL -> when (text) {
        "1" -> "Você escolheu Consultar Débito. Digite o número do seu CPF." to ConversationStepEnum.CONSULTA_DEBITO
        "2" -> "Você escolheu Pagamento. Digite o código de pagamento." to ConversationStepEnum.PAGAMENTO
        else -> """
                    Olá! Eu sou o ZéCobrança 🤖
                    Digite:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento
        """.trimIndent() to ConversationStepEnum.MENU_INICIAL
      }

      ConversationStepEnum.CONSULTA_DEBITO -> {
        // Aqui você poderia integrar com um serviço real
        "✅ Consulta realizada com sucesso para CPF $text.\nDeseja voltar ao menu? (sim/nao)" to ConversationStepEnum.FIM
      }

      ConversationStepEnum.PAGAMENTO -> {
        // Aqui você poderia integrar com um serviço real
        "✅ Pagamento realizado com sucesso para código $text.\nDeseja voltar ao menu? (sim/nao)" to ConversationStepEnum.FIM
      }

      ConversationStepEnum.FIM -> when (text.lowercase()) {
        "sim" -> """
                    Menu inicial:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento
        """.trimIndent() to ConversationStepEnum.MENU_INICIAL
        "nao" -> "Obrigado! Até logo 👋" to ConversationStepEnum.FIM
        else -> "Deseja voltar ao menu? (sim/nao)" to ConversationStepEnum.FIM
      }
    }

    // Atualiza estado
    conversationRepo.save(state.copy(step = newStep))

    // Envia resposta via ChatPro
    return try {
      sendMessage.send(
        com.zecobranca.domain.entities.ChatMessage(
          to = from,
          text = reply,
          instanceId = instanceId,
        ),
      )
      ProcessResult(true)
    } catch (e: Exception) {
      ProcessResult(false, e.message)
    }
  }
}
