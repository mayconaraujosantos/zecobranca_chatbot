package com.zecobranca

import com.zecobranca.main.config.Application.setupApp
import com.zecobranca.main.config.Env
import org.slf4j.LoggerFactory

suspend fun main() {
  val logger = LoggerFactory.getLogger("Main")

  logger.info("🚀 ZéCobrança Bot starting up...")
  logger.info("⚙️ Environment configuration - Port: ${Env.port}")
  logger.info("🔗 ChatPro API URL: ${Env.chatProApiUrl}")
  logger.info("🔑 ChatPro Instance ID: ${Env.chatProInstanceId}")

  val app = setupApp()

  logger.info("🌐 Starting Javalin server on port ${Env.port}")
  app.start(Env.port)

  logger.info("✅ ZéCobrança Bot started successfully!")

  // Detecta se está rodando no Railway ou localmente
  val isRailway = System.getenv("RAILWAY_ENVIRONMENT") != null
  val baseUrl =
          if (isRailway) {
            val railwayUrl = System.getenv("RAILWAY_PUBLIC_DOMAIN")
            if (railwayUrl != null) "https://$railwayUrl"
            else "https://zecobranca-production.up.railway.app"
          } else {
            "http://localhost:${Env.port}"
          }

  logger.info("📡 Webhook endpoint: $baseUrl/webhook")
  logger.info("❤️ Health check: $baseUrl/health")
  logger.info("🌍 Base URL: $baseUrl")
}
