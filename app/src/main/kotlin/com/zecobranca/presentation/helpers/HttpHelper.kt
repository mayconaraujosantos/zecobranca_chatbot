package com.zecobranca.presentation.helpers

import com.zecobranca.presentation.protocols.HttpResponse

object HttpHelper {
  fun ok(data: Any): HttpResponse {
    return HttpResponse(
      statusCode = 200,
      body = data,
      headers = mapOf("Content-Type" to "application/json")
    )
  }
}