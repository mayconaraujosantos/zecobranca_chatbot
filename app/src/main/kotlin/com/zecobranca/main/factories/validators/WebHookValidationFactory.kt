package com.zecobranca.main.factories.validators

import com.zecobranca.validation.validators.RequiredFieldValidation
import com.zecobranca.validation.validators.ValidationComposite

object WebHookValidationFactory {
  fun make(): ValidationComposite = ValidationComposite(
    listOf(
      RequiredFieldValidation("type"),
    ),
  )
}
