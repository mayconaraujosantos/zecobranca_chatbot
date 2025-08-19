package com.zecobranca.presentation.protocols

interface Controller {
  suspend fun handle(request: HttpRequest): HttpResponse
}