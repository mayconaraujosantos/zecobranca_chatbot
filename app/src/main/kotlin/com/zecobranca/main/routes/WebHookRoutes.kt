package com.zecobranca.main.routes

import com.zecobranca.main.adapters.JavalinRouteAdapter
import com.zecobranca.main.factories.controllers.WebHookControllerFactory
import io.javalin.Javalin

object WebHookRoutes {
  suspend fun setup(app: Javalin) {
    app.post("/webhook", JavalinRouteAdapter.adapt(WebHookControllerFactory.make()))
  }
}
