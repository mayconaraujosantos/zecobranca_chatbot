package com.zecobranca.presentation.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zecobranca.domain.entities.WebhookMessage
import com.zecobranca.domain.usecases.ProcessWebhookMessageData
import com.zecobranca.presentation.helpers.HttpHelper
import com.zecobranca.presentation.protocols.Controller
import com.zecobranca.presentation.protocols.HttpRequest
import com.zecobranca.presentation.protocols.HttpResponse
import com.zecobranca.validation.protocols.Validation

class WebhookController(
  private val processWebhookMessage: ProcessWebhookMessageData,
  private val validation: Validation,
) : Controller {

  private val mapper = jacksonObjectMapper()

  override suspend fun handle(request: HttpRequest): HttpResponse {
    return try {
      val webhookMessage = mapper.readValue<WebhookMessage>(request.body)

      // Convert to map for validation
      val messageMap = mapOf(
        "id" to webhookMessage.id,
        "from" to webhookMessage.from,
        "body" to webhookMessage.body,
        "type" to webhookMessage.type,
        "timestamp" to webhookMessage.timestamp,
        "instanceId" to webhookMessage.instanceId,
      )

      val validationResult = validation.validate(messageMap)
      if (!validationResult.isValid) {
        return HttpHelper.badRequest(validationResult.errors.joinToString(", "))
      }

      val result = processWebhookMessage.process(webhookMessage)

      if (result.success) {
        HttpHelper.ok(mapOf("message" to "Processed successfully"))
      } else {
        HttpHelper.badRequest(result.error ?: "Processing failed")
      }
    } catch (e: Exception) {
      HttpHelper.serverError("Failed to process webhook: ${e.message}")
    }
  }
}
