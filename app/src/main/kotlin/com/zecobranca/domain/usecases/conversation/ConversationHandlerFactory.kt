package com.zecobranca.domain.usecases.conversation

import com.zecobranca.domain.entities.ConversationState
import com.zecobranca.domain.entities.ConversationStepEnum
import com.zecobranca.domain.entities.WebhookMessage
import org.slf4j.LoggerFactory

object ConversationHandlerFactory {
    fun createHandler(step: ConversationStepEnum): ConversationHandler {
        return when (step) {
            ConversationStepEnum.MENU_INICIAL -> MenuInicialHandler()
            ConversationStepEnum.CONSULTA_DEBITO -> ConsultaDebitoHandler()
            ConversationStepEnum.PAGAMENTO -> PagamentoHandler()
            ConversationStepEnum.FIM -> FimHandler()
        }
    }
}

interface ConversationHandler {
    fun handle(state: ConversationState, message: WebhookMessage): ConversationResponse
}

data class ConversationResponse(
    val reply: String,
    val newStep: ConversationStepEnum
)

class MenuInicialHandler : ConversationHandler {
    private val logger = LoggerFactory.getLogger(MenuInicialHandler::class.java)

    override fun handle(state: ConversationState, message: WebhookMessage): ConversationResponse {
        val text = message.body?.trim() ?: ""
        val from = message.from ?: ""

        return when (text) {
            "1" -> {
                logger.info("🔍 User $from selected: Consultar Débito")
                ConversationResponse(
                    "Você escolheu Consultar Débito. Digite o número do seu CPF.",
                    ConversationStepEnum.CONSULTA_DEBITO
                )
            }
            "2" -> {
                logger.info("💳 User $from selected: Pagamento")
                ConversationResponse(
                    "Você escolheu Pagamento. Digite o código de pagamento.",
                    ConversationStepEnum.PAGAMENTO
                )
            }
            else -> {
                logger.info("🏠 User $from at menu inicial, showing options")
                ConversationResponse(
                    """
                    Olá! Eu sou o ZéCobrança 🤖
                    Digite:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento
                    """.trimIndent(),
                    ConversationStepEnum.MENU_INICIAL
                )
            }
        }
    }
}

class ConsultaDebitoHandler : ConversationHandler {
    private val logger = LoggerFactory.getLogger(ConsultaDebitoHandler::class.java)

    override fun handle(state: ConversationState, message: WebhookMessage): ConversationResponse {
        val text = message.body?.trim() ?: ""
        val from = message.from ?: ""

        logger.info("🔍 Processing CPF consultation for user $from with CPF: $text")
        // Aqui você poderia integrar com um serviço real
        return ConversationResponse(
            "✅ Consulta realizada com sucesso para CPF $text.\nDeseja voltar ao menu? (sim/nao)",
            ConversationStepEnum.FIM
        )
    }
}

class PagamentoHandler : ConversationHandler {
    private val logger = LoggerFactory.getLogger(PagamentoHandler::class.java)

    override fun handle(state: ConversationState, message: WebhookMessage): ConversationResponse {
        val text = message.body?.trim() ?: ""
        val from = message.from ?: ""

        logger.info("💳 Processing payment for user $from with code: $text")
        // Aqui você poderia integrar com um serviço real
        return ConversationResponse(
            "✅ Pagamento realizado com sucesso para código $text.\nDeseja voltar ao menu? (sim/nao)",
            ConversationStepEnum.FIM
        )
    }
}

class FimHandler : ConversationHandler {
    private val logger = LoggerFactory.getLogger(FimHandler::class.java)

    override fun handle(state: ConversationState, message: WebhookMessage): ConversationResponse {
        val text = message.body?.trim() ?: ""
        val from = message.from ?: ""

        return when (text.lowercase()) {
            "sim" -> {
                logger.info("🔄 User $from returning to main menu")
                ConversationResponse(
                    """Menu inicial:
                    1️⃣ Consultar Débito
                    2️⃣ Pagamento""".trimIndent(),
                    ConversationStepEnum.MENU_INICIAL
                )
            }
            "nao" -> {
                logger.info("👋 User $from ending conversation")
                ConversationResponse(
                    "Obrigado! Até logo 👋",
                    ConversationStepEnum.FIM
                )
            }
            else -> {
                logger.info("❓ User $from gave unclear response: '$text'")
                ConversationResponse(
                    "Deseja voltar ao menu? (sim/nao)",
                    ConversationStepEnum.FIM
                )
            }
        }
    }
}
