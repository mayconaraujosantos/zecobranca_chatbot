package com.zecobranca.infra.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zecobranca.data.protocols.http.HttpPostClient
import com.zecobranca.domain.entities.ChatMessage
import com.zecobranca.domain.usecases.SendMessage
import okhttp3.OkHttpClient

class ChatProHttpClient(
  private val httpClient: HttpPostClient,
  private val apiUrl: String,
  private val apiToken: String,
) : SendMessage {
  private val client = OkHttpClient()
  private val mapper = jacksonObjectMapper()

  data class TextMessage(
    val text: String,
  )

  data class ChatProRequest(
    val instanceId: String,
    val number: String,
    val textMessage: TextMessage,
  )

  override suspend fun send(message: ChatMessage): Boolean {
    val request = ChatProRequest(
      instanceId = message.instanceId,
      number = message.to,
      textMessage = TextMessage(message.text),
    )
    val headers = mapOf(
      "accept" to "application/json",
      "content-type" to "application/json",
      "Authorization" to apiToken,
    )
    val requestBody = mapper.writeValueAsString(request)

    val response = httpClient.post(apiUrl, headers, requestBody)
    return response.statusCode in 200..299
  }
}
