package com.zecobranca.main.config

import com.zecobranca.main.routes.HealthRoutes
import com.zecobranca.main.routes.WebHookRoutes
import io.javalin.Javalin

object Application {
  suspend fun setupApp(): Javalin {
    val app = Javalin.create { config ->
      config.showJavalinBanner = false
      config.http.defaultContentType = "application/json"
    }
    WebHookRoutes.setup(app)
    HealthRoutes.setup(app)
    return app
  }
}
