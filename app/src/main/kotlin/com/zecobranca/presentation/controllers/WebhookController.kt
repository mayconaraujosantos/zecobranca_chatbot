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

  private fun createBasicWebhookMessage(jsonBody: String): WebhookMessage {
    logger.debug("🔧 Creating basic webhook message from JSON structure")

    try {
      // Extrair campos básicos usando regex para a estrutura real do ChatPro
      val typeMatch = """"Type":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val idMatch = """"Id":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val remoteJidMatch = """"RemoteJid":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val senderJidMatch = """"SenderJid":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val timestampMatch = """"Timestamp":\s*(\d+)""".toRegex().find(jsonBody)
      val textMatch = """"Text":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val pushNameMatch = """"PushName":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val fromMeMatch = """"FromMe":\s*(true|false)""".toRegex().find(jsonBody)

      // Novos campos para diferentes tipos de eventos
      val instanceIdMatch = """"InstanceId":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val statusMatch = """"Status":\s*(\d+)""".toRegex().find(jsonBody)
      val statusTextMatch = """"StatusText":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val chargeStatusMatch = """"ChargeStatus":\s*"([^"]*)"""".toRegex().find(jsonBody)

      val type = typeMatch?.groupValues?.get(1) ?: "unknown"
      val id = idMatch?.groupValues?.get(1)
      val from =
              remoteJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")
                      ?: senderJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")
      val body = textMatch?.groupValues?.get(1)
      val timestamp = timestampMatch?.groupValues?.get(1)?.toLong()
      val pushName = pushNameMatch?.groupValues?.get(1)
      val fromMe = fromMeMatch?.groupValues?.get(1)?.toBoolean()
      val instanceId = instanceIdMatch?.groupValues?.get(1)
      val status = statusMatch?.groupValues?.get(1)?.toInt()
      val statusText = statusTextMatch?.groupValues?.get(1)
      val chargeStatus = chargeStatusMatch?.groupValues?.get(1)

      logger.debug(
              "🔍 Extracted fields - Type: $type, From: $from, Body: $body, Timestamp: $timestamp, InstanceId: $instanceId, Status: $status"
      )

      return WebhookMessage(
              id = id,
              from = from,
              body = body,
              type = type.lowercase(),
              timestamp = timestamp,
              instanceId = instanceId,
              status = status,
              chargeStatus = chargeStatus,
              fromMe = fromMe
      )
    } catch (e: Exception) {
      logger.error("💥 Failed to create basic webhook message: ${e.message}")
      // Retornar objeto vazio em caso de falha
      return WebhookMessage(
              id = null,
              from = null,
              body = null,
              type = "unknown",
              timestamp = null,
              instanceId = null,
              status = null,
              chargeStatus = null
      )
    }
  }

  override suspend fun handle(request: HttpRequest): HttpResponse {
    logger.info("🔄 Webhook request received - Body length: ${request.body.length}")
    logger.debug("📄 Raw webhook body: ${request.body}")

    return try {
      logger.debug("📝 Parsing webhook message from JSON")
      logger.debug("📄 Full JSON body: ${request.body}")

      // Primeiro vamos tentar fazer o parse com a estrutura atual
      val webhookMessage =
              try {
                mapper.readValue(request.body, WebhookMessage::class.java)
              } catch (e: Exception) {
                logger.warn(
                        "⚠️ Standard parsing failed, creating basic webhook message: ${e.message}"
                )
                // Criar um webhook básico com informações extraídas manualmente
                createBasicWebhookMessage(request.body)
              }

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

      // Verificar se é um webhook de status de mensagem (não precisa de processamento)
      if (webhookMessage.type == "message_status") {
        logger.info("📨 Received message status webhook - Status: ${webhookMessage.status}")
        return HttpHelper.ok(
                mapOf("message" to "Message status received", "status" to webhookMessage.status)
        )
      }

      // Verificar se é um webhook de status de conexão (não precisa de processamento)
      if (webhookMessage.type == "connection_status") {
        logger.info("🔌 Received connection status webhook - Status: ${webhookMessage.status}")
        return HttpHelper.ok(
                mapOf("message" to "Connection status received", "status" to webhookMessage.status)
        )
      }

      // Verificar se é um webhook de grupo (não precisa de processamento por enquanto)
      if (webhookMessage.type == "group_message") {
        logger.info("👥 Received group message webhook - From: ${webhookMessage.from}")
        return HttpHelper.ok(
                mapOf("message" to "Group message received", "from" to webhookMessage.from)
        )
      }

      // Verificar se é um webhook de mensagem válida (recebida ou enviada pelo usuário)
      // Ignorar webhooks de confirmação de envio para evitar loops
      if (webhookMessage.from == null) {
        logger.info("ℹ️ Received webhook without sender - Type: ${webhookMessage.type}")
        return HttpHelper.ok(mapOf("message" to "Webhook received", "type" to webhookMessage.type))
      }

      // Processar apenas mensagens do usuário, não confirmações do sistema
      if (webhookMessage.type != "received" && webhookMessage.type != "send_message") {
        logger.info("ℹ️ Received system webhook - Type: ${webhookMessage.type}")
        return HttpHelper.ok(
                mapOf("message" to "System webhook received", "type" to webhookMessage.type)
        )
      }

      // Verificar se é uma confirmação de envio (FromMe: true) - ignorar para evitar loop
      if (webhookMessage.type == "send_message" && webhookMessage.fromMe == true) {
        logger.info("ℹ️ Ignoring message confirmation webhook to prevent loop - FromMe: true")
        return HttpHelper.ok(
                mapOf("message" to "Message confirmation ignored", "type" to webhookMessage.type)
        )
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

      logger.info(
              "🚀 Processing ${webhookMessage.type} webhook message for user: ${webhookMessage.from}"
      )
      val result = processWebhookMessage.process(webhookMessage)

      if (result.success) {
        logger.info(
                "🎉 ${webhookMessage.type} webhook processed successfully for user: ${webhookMessage.from}"
        )
        HttpHelper.ok(mapOf("message" to "Processed successfully", "type" to webhookMessage.type))
      } else {
        logger.error(
                "💥 ${webhookMessage.type} webhook processing failed for user: ${webhookMessage.from}, error: ${result.error}"
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
