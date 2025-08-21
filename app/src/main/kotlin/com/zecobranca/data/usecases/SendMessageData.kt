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

  override suspend fun send(message: ChatMessage): Boolean {
    val url = "${Env.chatProApiUrl}${Env.chatProInstanceId}/api/v1/send_message"
    logger.info("📤 Sending message to ChatPro API: $url")

    val headers =
            mapOf(
                    "accept" to "application/json",
                    "content-type" to "application/json",
                    "Authorization" to chatProApiToken,
            )

    val body =
            mapOf(
                    "number" to message.to,
                    "message" to message.text,
            )

    logger.debug("📋 Request body: $body")

    val resp = httpClient.post(url, headers, body)
    logger.info("📡 ChatPro API response - Status: ${resp.statusCode}")

    val success = resp.statusCode in 200..299
    if (success) {
      logger.info("✅ Message sent successfully via ChatPro")
    } else {
      logger.error("❌ Failed to send message via ChatPro - Status: ${resp.statusCode}")
    }

    return success
  }
}
