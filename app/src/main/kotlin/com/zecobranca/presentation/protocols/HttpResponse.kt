package com.zecobranca.presentation.protocols

data class HttpResponse(
  val statusCode: Int,
  val body: Any,
  val headers: Map<String, String> = emptyMap(),
)