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
              logger.info("⚙️ Javalin configuration applied")
            }

    logger.info("🔧 Setting up WebHook routes")
    WebHookRoutes.setup(app)

    logger.info("❤️ Setting up Health routes")
    HealthRoutes.setup(app)

    logger.info("✅ Application setup completed successfully")
    return app
  }
}
