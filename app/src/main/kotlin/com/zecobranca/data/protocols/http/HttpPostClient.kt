package com.zecobranca.data.protocols.http

import com.zecobranca.presentation.protocols.HttpResponse

interface HttpPostClient {
  fun post(url: String, headers: Map<String, String>, body: Any): HttpResponse
}
