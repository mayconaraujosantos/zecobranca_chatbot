package com.zecobranca.main.routes

import com.zecobranca.main.adapters.JavalinRouteAdapter
import com.zecobranca.main.factories.controllers.HealthControllerFactory
import io.javalin.Javalin

object HealthRoutes {
  suspend fun setup(app: Javalin) {
    app.get("/health", JavalinRouteAdapter.adapt(HealthControllerFactory.make()))
  }
}
