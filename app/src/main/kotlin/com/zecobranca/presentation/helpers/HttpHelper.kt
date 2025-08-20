package com.zecobranca.presentation.helpers

import com.zecobranca.presentation.protocols.HttpResponse

object HttpHelper {
  fun ok(data: Any): HttpResponse = HttpResponse(
    statusCode = 200,
    body = data,
    headers = mapOf("Content-Type" to "application/json"),
  )
  fun badRequest(error: String): HttpResponse = HttpResponse(
    statusCode = 400,
    body = mapOf("error" to error),
    headers = mapOf("Content-Type" to "application/json"),
  )

  fun serverError(error: String): HttpResponse = HttpResponse(
    statusCode = 500,
    body = mapOf("error" to error),
    headers = mapOf("Content-Type" to "application/json"),
  )
}
