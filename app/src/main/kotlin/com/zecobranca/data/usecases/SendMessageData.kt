package com.zecobranca.data.usecases

import com.zecobranca.data.protocols.http.HttpPostClient
import com.zecobranca.domain.entities.ChatMessage
import com.zecobranca.domain.usecases.SendMessage
import com.zecobranca.main.config.Env
import org.slf4j.LoggerFactory

class SendMessageData(
        private val httpClient: HttpPostClient,
        private val chatProApiToken: String,
) : SendMessage {

  private val logger = LoggerFactory.getLogger(SendMessageData::class.java)

  // Estrutura da resposta da API do ChatPro
  data class ChatProResponse(
          val message: String,
          val resposeMessage:
                  ResponseMessage, // ChatPro retorna "resposeMessage" (com erro de digitação)
          val status: Boolean
  )

  data class ResponseMessage(val id: String, val timestamp: Long)

  override suspend fun send(message: ChatMessage): Boolean {
    val url = "${Env.chatProApiUrl}${Env.chatProInstanceId}/api/v1/send_message"
    logger.info("📤 Sending message to ChatPro API: $url")

    val headers =
            mapOf(
                    "accept" to "application/json",
                    "content-type" to "application/json",
                    "Authorization" to chatProApiToken,
            )

    // Formato exato que o ChatPro espera
    val body =
            mapOf(
                    "number" to message.to,
                    "message" to message.text,
                    // quoted_message_id é opcional, só incluir se necessário
                    )

    logger.debug("📋 Request body: $body")

    val resp = httpClient.post(url, headers, body)
    logger.info("📡 ChatPro API response - Status: ${resp.statusCode}")

    val success = resp.statusCode in 200..299
    if (success) {
      try {
        // Parse manual simples da resposta JSON
        val responseBody = resp.body.toString()

        // Extrair campos usando regex simples
        val messageMatch = """"message":\s*"([^"]*)"""".toRegex().find(responseBody)
        val statusMatch = """"status":\s*(true|false)""".toRegex().find(responseBody)
        val idMatch = """"id":\s*"([^"]*)"""".toRegex().find(responseBody)
        val timestampMatch = """"timestamp":\s*(\d+)""".toRegex().find(responseBody)

        val message = messageMatch?.groupValues?.get(1) ?: "N/A"
        val status = statusMatch?.groupValues?.get(1)?.toBoolean() ?: false
        val messageId = idMatch?.groupValues?.get(1) ?: "N/A"
        val timestamp = timestampMatch?.groupValues?.get(1)?.toLong() ?: 0L

        logger.info("✅ Message sent successfully via ChatPro")
        logger.info("📨 Message ID: $messageId")
        logger.info("⏰ Timestamp: $timestamp")
        logger.info("💬 Response message: $message")
        logger.info("✅ Status: $status")
      } catch (e: Exception) {
        logger.warn("⚠️ Could not parse ChatPro response: ${e.message}")
        logger.debug("📄 Raw response body: ${resp.body}")
      }
    } else {
      logger.error("❌ Failed to send message via ChatPro - Status: ${resp.statusCode}")
      logger.error("📄 Error response body: ${resp.body}")
    }

    return success
  }
}
