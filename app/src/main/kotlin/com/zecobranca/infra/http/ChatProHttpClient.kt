package com.zecobranca.infra.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zecobranca.data.protocols.http.HttpPostClient
import com.zecobranca.domain.entities.ChatMessage
import com.zecobranca.domain.usecases.SendMessage
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class ChatProHttpClient(
        private val httpClient: HttpPostClient,
        private val apiUrl: String,
        private val apiToken: String,
) : SendMessage {
  private val client = OkHttpClient()
  private val mapper = jacksonObjectMapper()
  private val logger = LoggerFactory.getLogger(ChatProHttpClient::class.java)

  data class TextMessage(
          val text: String,
  )

  data class ChatProRequest(
          val instanceId: String,
          val number: String,
          val textMessage: TextMessage,
  )

  override suspend fun send(message: ChatMessage): Boolean {
    logger.info("📤 Preparing ChatPro request for user: ${message.to}")
    logger.debug("🔗 ChatPro API URL: $apiUrl")

    val request =
            ChatProRequest(
                    instanceId = message.instanceId,
                    number = message.to,
                    textMessage = TextMessage(message.text),
            )

    logger.debug("📋 ChatPro request payload: $request")

    val headers =
            mapOf(
                    "accept" to "application/json",
                    "content-type" to "application/json",
                    "Authorization" to apiToken,
            )

    logger.debug(
            "🔑 Headers prepared (token masked): ${headers.mapValues { if (it.key == "Authorization") "***" else it.value }}"
    )

    val requestBody = mapper.writeValueAsString(request)
    logger.debug("📝 Request body serialized: $requestBody")

    logger.info("🚀 Sending HTTP POST to ChatPro API")
    val response = httpClient.post(apiUrl, headers, requestBody)

    logger.info("📡 ChatPro API response - Status: ${response.statusCode}")
    logger.debug("📄 Response body: ${response.body}")

    val success = response.statusCode in 200..299
    if (success) {
      logger.info("✅ ChatPro API call successful")
    } else {
      logger.error("❌ ChatPro API call failed with status: ${response.statusCode}")
    }

    return success
  }
}
