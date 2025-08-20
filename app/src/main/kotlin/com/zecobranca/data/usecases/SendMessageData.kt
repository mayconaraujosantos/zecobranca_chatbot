package com.zecobranca.data.usecases

import com.zecobranca.data.protocols.http.HttpPostClient
import com.zecobranca.domain.entities.ChatMessage
import com.zecobranca.domain.usecases.SendMessage
import com.zecobranca.main.config.Env

class SendMessageData(
  private val httpClient: HttpPostClient,
  private val chatProApiToken: String,
) : SendMessage {
  override suspend fun send(message: ChatMessage): Boolean {
    val url = "${Env.chatProApiUrl}${Env.chatProInstanceId}/api/v1/send_message"
    val headers = mapOf(
      "accept" to "application/json",
      "content-type" to "application/json",
      "Authorization" to Env.chatProApiToken,
    )
    val body = mapOf(
      "number" to message.to,
      "message" to message.text,
    )
    val resp = httpClient.post(url, headers, body)
    return resp.statusCode in 200..299
  }
}
