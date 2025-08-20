package com.zecobranca.main.config

import com.zecobranca.main.routes.HealthRoutes
import com.zecobranca.main.routes.WebHookRoutes
import io.javalin.Javalin
import org.slf4j.LoggerFactory

object Application {
  private val logger = LoggerFactory.getLogger(Application::class.java)

  suspend fun setupApp(): Javalin {
    logger.info("🚀 Starting ZéCobrança Bot application setup")

    val app =
            Javalin.create { config ->
              config.showJavalinBanner = false
              config.http.defaultContentType = "application/json"

              // Configuração para aceitar conexões externas (necessário para Railway)
              config.server {
                it.host = "0.0.0.0" // Aceita conexões de qualquer IP
                it.port = Env.port
              }

              logger.info("⚙️ Javalin configuration applied - Host: 0.0.0.0, Port: ${Env.port}")
            }

    logger.info("🔧 Setting up WebHook routes")
    WebHookRoutes.setup(app)

    logger.info("❤️ Setting up Health routes")
    HealthRoutes.setup(app)

    logger.info("✅ Application setup completed successfully")
    return app
  }
}
