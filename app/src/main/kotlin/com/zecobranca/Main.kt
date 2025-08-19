package com.zecobranca

import com.zecobranca.main.config.Application.setupApp
import com.zecobranca.main.config.Env

suspend fun main() {
  val app = setupApp()

  app.start(Env.port)

  println("🚀 ZéCobrança Bot iniciado na porta ${Env.port}!")
  println("📡 Webhook: http://localhost:${Env.port}/webhook")
  println("❤️ Health Check: http://localhost:${Env.port}/health")
  println("🔧 Environment:")
  println("   - ChatPro API URL: ${Env.chatProApiUrl}")
  println("   - Instance ID: ${Env.chatProInstanceId}")
  println(
          "   - API Token: ${if (Env.chatProApiToken.length > 10) "${Env.chatProApiToken.take(10)}..." else "NOT_SET"}"
  )
}
