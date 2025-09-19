package com.zecobranca.presentation.controllers

import com.fasterxml.jackson.databind.DeserializationFeature
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

  // ObjectMapper configurado para ser mais tolerante a falhas
  private val mapper = ObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
  }

  private val logger = LoggerFactory.getLogger(WebhookController::class.java)

  private fun identifyJsonFormat(jsonBody: String): String {
    return when {
      jsonBody.trim().startsWith("[") -> "array"
      jsonBody.contains("\"number\"") && jsonBody.contains("\"message\"") -> "simple"
      jsonBody.contains("\"Type\"") && jsonBody.contains("\"Body\"") -> "chatpro"
      else -> "unknown"
    }
  }

  private fun parseArrayFormat(jsonBody: String): WebhookMessage? {
    try {
      val jsonArray = mapper.readTree(jsonBody)
      if (jsonArray.isArray && jsonArray.size() > 1) {
        val webhookData = jsonArray[1]

        // Se é um ACK
        if (webhookData.has("cmd") && webhookData.get("cmd").asText() == "ack") {
          logger.debug("Parsing ACK from array format")
          return WebhookMessage(
            id = webhookData.get("id")?.asText(),
            cmd = "ack",
            ack = webhookData.get("ack")?.asInt(),
            type = "ack_update",
            t = webhookData.get("t")?.asLong(),
            isSync = webhookData.get("isSync")?.asBoolean(),
            from = null,
            body = null,
            timestamp = null,
            instanceId = null,
            status = null,
            chargeStatus = null,
            fromMe = null
          )
        }

        // Outros tipos de mensagem em array format
        return mapper.treeToValue(webhookData, WebhookMessage::class.java)
      }
    } catch (e: Exception) {
      logger.warn("Failed to parse array format: ${e.message}")
    }
    return null
  }

  private fun createBasicWebhookMessage(jsonBody: String): WebhookMessage {
    logger.debug("Creating basic webhook message from JSON structure")

    try {
      val format = identifyJsonFormat(jsonBody)
      logger.debug("Identified JSON format: $format")

      // Tratar formato de array primeiro
      if (format == "array") {
        val arrayResult = parseArrayFormat(jsonBody)
        if (arrayResult != null) {
          return arrayResult
        }
      }

      // Formato simples: {"number": "xxx", "message": "yyy", "quoted_message_id": "zzz"}
      if (format == "simple") {
        logger.debug("Detected simple format JSON (number/message structure)")

        val numberMatch = """"number":\s*"([^"]*)"""".toRegex().find(jsonBody)
        val messageMatch = """"message":\s*"([^"]*)"""".toRegex().find(jsonBody)
        val quotedMessageIdMatch = """"quoted_message_id":\s*"([^"]*)"""".toRegex().find(jsonBody)

        val from = numberMatch?.groupValues?.get(1)
        val body = messageMatch?.groupValues?.get(1)
        val quotedMessageId = quotedMessageIdMatch?.groupValues?.get(1)

        logger.debug("Simple format - From: $from, Body: $body, QuotedId: $quotedMessageId")

        return WebhookMessage(
          id = quotedMessageId ?: System.currentTimeMillis().toString(),
          from = from,
          body = body,
          type = "received",
          timestamp = System.currentTimeMillis(),
          instanceId = null,
          status = null,
          chargeStatus = null,
          fromMe = false,
          number = from,
          message = body,
          quotedMessageId = quotedMessageId
        )
      }

      // Parse do formato complexo (ChatPro) - mantém a lógica existente
      logger.debug("Detected complex format JSON (ChatPro structure)")

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

      // Extrair campos da estrutura aninhada Body.Info
      val bodyInfoIdMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"Id":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val bodyInfoRemoteJidMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"RemoteJid":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val bodyInfoSenderJidMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"SenderJid":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val bodyInfoTimestampMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"Timestamp":\s*(\d+)""".toRegex().find(jsonBody)
      val bodyInfoFromMeMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"FromMe":\s*(true|false)"""".toRegex().find(jsonBody)
      val bodyInfoPushNameMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"PushName":\s*"([^"]*)"""".toRegex().find(jsonBody)
      val bodyInfoStatusMatch = """"Body":\s*\{[^}]*"Info":\s*\{[^}]*"Status":\s*(\d+)""".toRegex().find(jsonBody)

      // Usar campos da estrutura aninhada se disponíveis, senão usar campos diretos
      val type = typeMatch?.groupValues?.get(1) ?: "received"
      val id = bodyInfoIdMatch?.groupValues?.get(1) ?: idMatch?.groupValues?.get(1)
      val from = bodyInfoRemoteJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
        ?: bodyInfoSenderJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
        ?: remoteJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
        ?: senderJidMatch?.groupValues?.get(1)?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
      val body = textMatch?.groupValues?.get(1)
      val timestamp = bodyInfoTimestampMatch?.groupValues?.get(1)?.toLong()
        ?: timestampMatch?.groupValues?.get(1)?.toLong()
      val pushName = bodyInfoPushNameMatch?.groupValues?.get(1) ?: pushNameMatch?.groupValues?.get(1)
      val fromMe = bodyInfoFromMeMatch?.groupValues?.get(1)?.toBoolean()
        ?: fromMeMatch?.groupValues?.get(1)?.toBoolean()
      val instanceId = instanceIdMatch?.groupValues?.get(1)
      val status = bodyInfoStatusMatch?.groupValues?.get(1)?.toInt()
        ?: statusMatch?.groupValues?.get(1)?.toInt()
      val statusText = statusTextMatch?.groupValues?.get(1)
      val chargeStatus = chargeStatusMatch?.groupValues?.get(1)

      logger.debug("Complex format - Type: $type, From: $from, Body: $body, Timestamp: $timestamp, InstanceId: $instanceId, Status: $status")

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
      logger.error("Failed to create basic webhook message: ${e.message}")
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
    logger.debug("Raw webhook body: ${request.body}")

    return try {
      logger.debug("Parsing webhook message from JSON")
      logger.debug("Full JSON body: ${request.body}")

      // *** NOVO: Primeiro verificar se é formato de array ***
      val webhookMessage = try {
        if (request.body.trim().startsWith("[")) {
          logger.debug("Detected array format webhook from ChatPro")
          val arrayResult = parseArrayFormat(request.body)
          arrayResult ?: throw IllegalArgumentException("Failed to parse array format")
        } else {
          // Parse normal para outros formatos
          mapper.readValue(request.body, WebhookMessage::class.java)
        }
      } catch (e: Exception) {
        logger.warn("⚠️ Standard parsing failed, creating basic webhook message: ${e.message}")
        createBasicWebhookMessage(request.body)
      }

      logger.info(
        "✅ Webhook parsed successfully - From: ${webhookMessage.getActualFrom()}, Body: ${webhookMessage.getActualBody()}, Type: ${webhookMessage.getActualType()}"
      )

      // *** MELHORADO: Verificar se é um evento de ACK ***
      if (webhookMessage.isAckEvent()) {
        logger.info(
          "📨 Received ACK webhook - Status: ${webhookMessage.getActualStatus()} (${webhookMessage.getAckDescription()})"
        )
        return HttpHelper.ok(
          mapOf(
            "message" to "ACK received",
            "ack" to webhookMessage.getActualStatus(),
            "description" to webhookMessage.getAckDescription()
          )
        )
      }

      // Verificar se é um webhook de status de cobrança
      if (webhookMessage.getActualType() == "charge_status") {
        logger.info("💰 Received charge status webhook - Status: ${webhookMessage.getActualStatus()}")
        return HttpHelper.ok(
          mapOf(
            "message" to "Charge status received",
            "status" to webhookMessage.getActualStatus()
          )
        )
      }

      // Verificar se é um webhook de status de mensagem
      if (webhookMessage.getActualType() == "message_status") {
        logger.info("📋 Received message status webhook - Status: ${webhookMessage.getActualStatus()}")
        return HttpHelper.ok(
          mapOf(
            "message" to "Message status received",
            "status" to webhookMessage.getActualStatus()
          )
        )
      }

      // Verificar se é um webhook de status de conexão
      if (webhookMessage.getActualType() == "connection_status") {
        logger.info("🔌 Received connection status webhook - Status: ${webhookMessage.getActualStatus()}")
        return HttpHelper.ok(
          mapOf(
            "message" to "Connection status received",
            "status" to webhookMessage.getActualStatus()
          )
        )
      }

      // Verificar se é um webhook de grupo
      if (webhookMessage.getActualType() == "group_message") {
        logger.info("👥 Received group message webhook - From: ${webhookMessage.getActualFrom()}")
        return HttpHelper.ok(
          mapOf(
            "message" to "Group message received",
            "from" to webhookMessage.getActualFrom()
          )
        )
      }

      // *** NOVO: Verificar se é mensagem do próprio bot para evitar loop ***
      val messageBody = webhookMessage.getActualBody()
      if (messageBody != null && (messageBody.contains("ZéCobrança 🤖") ||
          messageBody.contains("1️⃣") ||
          messageBody.contains("2️⃣") ||
          messageBody.contains("Consultar Débito") ||
          messageBody.contains("Pagamento"))) {
        logger.info("🤖 Ignoring bot's own message to prevent loop")
        return HttpHelper.ok(mapOf("message" to "Bot message ignored to prevent loop"))
      }

      // Verificar se é um webhook sem remetente
      if (webhookMessage.getActualFrom() == null) {
        logger.info("ℹ️ Received webhook without sender - Type: ${webhookMessage.getActualType()}")
        return HttpHelper.ok(mapOf("message" to "Webhook received", "type" to webhookMessage.getActualType()))
      }

      // *** MELHORADO: Processar apenas mensagens relevantes ***
      val actualType = webhookMessage.getActualType()
      if (actualType != "received" && actualType != "send_message") {
        logger.info("ℹ️ Received system webhook - Type: $actualType")
        return HttpHelper.ok(mapOf("message" to "System webhook received", "type" to actualType))
      }

      // *** NOVO: Verificar se é confirmação de envio para evitar loop ***
      if (webhookMessage.getActualFromMe() == true) {
        logger.info("🔄 Ignoring message confirmation webhook to prevent loop - FromMe: true")
        return HttpHelper.ok(
          mapOf("message" to "Message confirmation ignored", "type" to webhookMessage.getActualType())
        )
      }

      // Verificar se é mensagem de grupo
      val isGroupMessage = webhookMessage.getActualFrom()?.contains("@g.us") == true
      if (isGroupMessage) {
        logger.info("👥 Processing group message from: ${webhookMessage.getActualFrom()}")
      }

      // Validação
      val messageMap = mapOf(
        "id" to webhookMessage.getActualId(),
        "from" to webhookMessage.getActualFrom(),
        "body" to webhookMessage.getActualBody(),
        "type" to webhookMessage.getActualType(),
        "timestamp" to webhookMessage.getActualTimestamp(),
        "instanceId" to webhookMessage.instanceId,
      )
      logger.debug("🔍 Validation map created: $messageMap")

      logger.debug("Running validation")
      val validationResult = validation.validate(messageMap)
      if (!validationResult.isValid) {
        logger.warn("❌ Validation failed: ${validationResult.errors}")
        return HttpHelper.badRequest(validationResult.errors.joinToString(", "))
      }
      logger.info("✅ Validation passed")

      logger.info(
        "🔄 Processing ${webhookMessage.getActualType()} webhook message for user: ${webhookMessage.getActualFrom()}"
      )

      // Criar WebhookMessage limpo para processamento
      val cleanWebhookMessage = WebhookMessage(
        id = webhookMessage.getActualId(),
        from = webhookMessage.getActualFrom(),
        body = webhookMessage.getActualBody(),
        type = webhookMessage.getActualType(),
        timestamp = webhookMessage.getActualTimestamp(),
        instanceId = webhookMessage.instanceId,
        status = webhookMessage.getActualStatus(),
        chargeStatus = webhookMessage.chargeStatus,
        fromMe = webhookMessage.getActualFromMe()
      )

      val result = processWebhookMessage.process(cleanWebhookMessage)

      if (result.success) {
        logger.info(
          "✅ ${webhookMessage.getActualType()} webhook processed successfully for user: ${webhookMessage.getActualFrom()}"
        )
        HttpHelper.ok(mapOf("message" to "Processed successfully", "type" to webhookMessage.getActualType()))
      } else {
        logger.error(
          "❌ ${webhookMessage.getActualType()} webhook processing failed for user: ${webhookMessage.getActualFrom()}, error: ${result.error}"
        )
        HttpHelper.badRequest(result.error ?: "Processing failed")
      }
    } catch (e: Exception) {
      logger.error("💥 Critical error processing webhook: ${e.message}", e)
      logger.error("Request body that caused error: ${request.body}")
      HttpHelper.serverError("Failed to process webhook: ${e.message}")
    }
  }
}
