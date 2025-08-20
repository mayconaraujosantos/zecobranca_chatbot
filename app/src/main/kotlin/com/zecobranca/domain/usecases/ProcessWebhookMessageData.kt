package com.zecobranca.domain.usecases

import com.zecobranca.domain.entities.ConversationState
import com.zecobranca.domain.entities.ConversationStepEnum
import com.zecobranca.domain.entities.WebhookMessage
import com.zecobranca.infra.db.memory.MemoryConversationRepository
import org.slf4j.LoggerFactory

data class ProcessResult(val success: Boolean, val error: String? = null)

class ProcessWebhookMessageData(
        private val sendMessage: SendMessage,
        private val conversationRepo: MemoryConversationRepository,
) {

  private val logger = LoggerFactory.getLogger(ProcessWebhookMessageData::class.java)

  suspend fun process(webhookMessage: WebhookMessage): ProcessResult {
    logger.info("🔄 Starting webhook message processing")

    val from =
            webhookMessage.from
                    ?: run {
                      logger.error("❌ No sender found in webhook message")
                      return ProcessResult(false, "No sender")
                    }

    val text = webhookMessage.body?.trim() ?: ""
    val instanceId = webhookMessage.instanceId ?: ""

    logger.info("📱 Processing message from: $from, text: '$text', instance: $instanceId")

    // Carrega ou cria estado da conversa
    val state = conversationRepo.loadByUserId(from) ?: ConversationState(userId = from)
    logger.info("💾 Conversation state loaded - User: $from, Current step: ${state.step}")

    val (reply, newStep) =
            when (state.step) {
              ConversationStepEnum.MENU_INICIAL ->
                      when (text) {
                        "1" -> {
                          logger.info("🔍 User $from selected: Consultar Débito")
                          "Você escolheu Consultar Débito. Digite o número do seu CPF." to
                                  ConversationStepEnum.CONSULTA_DEBITO
                        }
                        "2" -> {
                          logger.info("💳 User $from selected: Pagamento")
                          "Você escolheu Pagamento. Digite o código de pagamento." to
                                  ConversationStepEnum.PAGAMENTO
                        }
                        else -> {
                          logger.info("🏠 User $from at menu inicial, showing options")
                          """
                    Olá! Eu sou o ZéCobrança 🤖
                    Digite:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento
          """.trimIndent() to
                                  ConversationStepEnum.MENU_INICIAL
                        }
                      }
              ConversationStepEnum.CONSULTA_DEBITO -> {
                logger.info("🔍 Processing CPF consultation for user $from with CPF: $text")
                // Aqui você poderia integrar com um serviço real
                "✅ Consulta realizada com sucesso para CPF $text.\nDeseja voltar ao menu? (sim/nao)" to
                        ConversationStepEnum.FIM
              }
              ConversationStepEnum.PAGAMENTO -> {
                logger.info("💳 Processing payment for user $from with code: $text")
                // Aqui você poderia integrar com um serviço real
                "✅ Pagamento realizado com sucesso para código $text.\nDeseja voltar ao menu? (sim/nao)" to
                        ConversationStepEnum.FIM
              }
              ConversationStepEnum.FIM ->
                      when (text.lowercase()) {
                        "sim" -> {
                          logger.info("🔄 User $from returning to main menu")
                          """
                    Menu inicial:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento
          """.trimIndent() to
                                  ConversationStepEnum.MENU_INICIAL
                        }
                        "nao" -> {
                          logger.info("👋 User $from ending conversation")
                          "Obrigado! Até logo 👋" to ConversationStepEnum.FIM
                        }
                        else -> {
                          logger.info("❓ User $from gave unclear response: '$text'")
                          "Deseja voltar ao menu? (sim/nao)" to ConversationStepEnum.FIM
                        }
                      }
            }

    logger.info("📝 Generated reply: '$reply'")
    logger.info("🔄 Updating conversation step from ${state.step} to $newStep")

    // Atualiza estado
    conversationRepo.save(state.copy(step = newStep))
    logger.info("💾 Conversation state saved successfully")

    // Envia resposta via ChatPro
    return try {
      logger.info("📤 Sending message via ChatPro to user: $from")
      sendMessage.send(
              com.zecobranca.domain.entities.ChatMessage(
                      to = from,
                      text = reply,
                      instanceId = instanceId,
              ),
      )
      logger.info("✅ Message sent successfully via ChatPro")
      ProcessResult(true)
    } catch (e: Exception) {
      logger.error("💥 Failed to send message via ChatPro: ${e.message}", e)
      ProcessResult(false, e.message)
    }
  }
}
