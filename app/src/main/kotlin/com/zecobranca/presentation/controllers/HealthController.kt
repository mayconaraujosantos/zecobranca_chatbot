package com.zecobranca.presentation.controllers

import com.zecobranca.presentation.helpers.HttpHelper
import com.zecobranca.presentation.protocols.Controller
import com.zecobranca.presentation.protocols.HttpRequest
import com.zecobranca.presentation.protocols.HttpResponse

class HealthController : Controller {
  override suspend fun handle(request: HttpRequest): HttpResponse {
    return HttpHelper.ok(
      mapOf(
        "status" to "OK",
        "service" to "ZeCobranca Bot",
        "timestamp" to System.currentTimeMillis()
      )
    )
  }

}