package com.zecobranca.presentation.protocols

data class HttpRequest(
  val body: String,
  val headers: Map<String, String> = emptyMap(),
  val params: Map<String, String> = emptyMap(),
  val query: Map<String, String> = emptyMap()
)