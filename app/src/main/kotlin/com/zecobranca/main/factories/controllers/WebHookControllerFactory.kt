package com.zecobranca.main.factories.controllers

import com.zecobranca.main.factories.usescases.ProcessWebhookMessageFactory
import com.zecobranca.main.factories.validators.WebHookValidationFactory
import com.zecobranca.presentation.controllers.WebhookController

object WebHookControllerFactory {
  fun make(): WebhookController = WebhookController(
    processWebhookMessage = ProcessWebhookMessageFactory.make(),
    validation = WebHookValidationFactory.make(),
  )
}
