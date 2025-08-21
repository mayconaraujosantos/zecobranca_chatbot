package com.zecobranca.presentation.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.zecobranca.domain.entities.WebhookMessage
import com.zecobranca.domain.usecases.ProcessWebhookMessageData
import com.zecobranca.presentation.helpers.HttpHelper
import com.zecobranca.presentation.protocols.Controller
import com.zecobranca.presentation.protocols.HttpRequest
import com.zecobranca.presentation.protocols.HttpResponse
import com.zecobranca.validation.protocols.Validation
import org.slf4j.LoggerFactory

class WebhookController(
        private val processWebhookMessage: ProcessWebhookMessageData,
        private val validation: Validation,
) : Controller {

  private val mapper = ObjectMapper()
  private val logger = LoggerFactory.getLogger(WebhookController::class.java)

  override suspend fun handle(request: HttpRequest): HttpResponse {
    logger.info("🔄 Webhook request received - Body length: ${request.body.length}")
    logger.debug("📄 Raw webhook body: ${request.body}")

    return try {
      logger.debug("📝 Parsing webhook message from JSON")
      val webhookMessage = mapper.readValue(request.body, WebhookMessage::class.java)

      logger.info(
              "✅ Webhook parsed successfully - From: ${webhookMessage.from}, Body: ${webhookMessage.body}, Type: ${webhookMessage.type}"
      )

      // Verificar se é um webhook de status de cobrança (não precisa de processamento)
      if (webhookMessage.type == "charge_status") {
        logger.info("💰 Received charge status webhook - Status: ${webhookMessage.status}")
        return HttpHelper.ok(
                mapOf("message" to "Charge status received", "status" to webhookMessage.status)
        )
      }

      // Verificar se é um webhook de mensagem recebida
      if (webhookMessage.type != "received" || webhookMessage.from == null) {
        logger.info("ℹ️ Received non-message webhook - Type: ${webhookMessage.type}")
        return HttpHelper.ok(mapOf("message" to "Webhook received", "type" to webhookMessage.type))
      }

      // Convert to map for validation
      val messageMap =
              mapOf(
                      "id" to webhookMessage.id,
                      "from" to webhookMessage.from,
                      "body" to webhookMessage.body,
                      "type" to webhookMessage.type,
                      "timestamp" to webhookMessage.timestamp,
                      "instanceId" to webhookMessage.instanceId,
              )
      logger.debug("🔍 Validation map created: $messageMap")

      logger.debug("✅ Running validation")
      val validationResult = validation.validate(messageMap)
      if (!validationResult.isValid) {
        logger.warn("❌ Validation failed: ${validationResult.errors}")
        return HttpHelper.badRequest(validationResult.errors.joinToString(", "))
      }
      logger.info("✅ Validation passed")

      logger.info("🚀 Processing webhook message for user: ${webhookMessage.from}")
      val result = processWebhookMessage.process(webhookMessage)

      if (result.success) {
        logger.info("🎉 Webhook processed successfully for user: ${webhookMessage.from}")
        HttpHelper.ok(mapOf("message" to "Processed successfully"))
      } else {
        logger.error(
                "💥 Webhook processing failed for user: ${webhookMessage.from}, error: ${result.error}"
        )
        HttpHelper.badRequest(result.error ?: "Processing failed")
      }
    } catch (e: Exception) {
      logger.error("💥 Critical error processing webhook: ${e.message}", e)
      logger.error("📋 Request body that caused error: ${request.body}")
      HttpHelper.serverError("Failed to process webhook: ${e.message}")
    }
  }
}
