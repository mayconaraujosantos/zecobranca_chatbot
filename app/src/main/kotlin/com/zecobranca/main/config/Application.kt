package com.zecobranca.main.config

import com.zecobranca.main.routes.HealthRoutes
import io.javalin.Javalin

object Application {
    suspend fun setupApp(): Javalin {
        val app = Javalin.create { config ->
            config.showJavalinBanner = false
            config.http.defaultContentType = "application/json"
        }

        HealthRoutes.setup(app)

        return app
    }
}