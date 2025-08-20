package com.zecobranca.validation.validators

import ValidationResult
import com.zecobranca.validation.protocols.Validation

class ValidationComposite(
  private val validations: List<Validation>,
) : Validation {

  override fun validate(input: Map<String, Any?>): ValidationResult {
    val errors = mutableListOf<String>()

    validations.forEach { validation ->
      val result = validation.validate(input)
      if (!result.isValid) {
        errors.addAll(result.errors)
      }
    }

    return ValidationResult(
      isValid = errors.isEmpty(),
      errors = errors,
    )
  }
}
