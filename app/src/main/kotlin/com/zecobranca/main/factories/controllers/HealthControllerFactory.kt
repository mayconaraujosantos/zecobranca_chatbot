package com.zecobranca.main.factories.controllers

import com.zecobranca.presentation.controllers.HealthController

object HealthControllerFactory {
  fun make(): HealthController {
    return HealthController()
  }
}