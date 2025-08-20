package com.zecobranca

import com.zecobranca.main.config.Application.setupApp
import com.zecobranca.main.config.Env

suspend fun main() {
  val app = setupApp()

  app.start(Env.port)

  println("🚀 ZéCobrança Bot iniciado na porta ${Env.port}!")
  println("📡 Webhook: http://localhost:${Env.port}/webhook")
  println("❤️ Health Check: http://localhost:${Env.port}/health")
}
