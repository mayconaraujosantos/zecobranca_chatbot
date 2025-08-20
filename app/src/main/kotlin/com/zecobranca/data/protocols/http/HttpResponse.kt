package com.zecobranca.data.protocols.http

data class HttpResponse(
  val statusCode: Int,
  val body: Any,
  val headers: Map<String, String> = emptyMap(),
)
