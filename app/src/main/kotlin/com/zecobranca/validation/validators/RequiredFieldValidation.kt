package com.zecobranca.validation.validators

import ValidationResult
import com.zecobranca.validation.protocols.Validation

class RequiredFieldValidation(
  private val fieldName: String,
) : Validation {

  override fun validate(input: Map<String, Any?>): ValidationResult {
    val value = input[fieldName]
    return if (value == null || (value is String && value.isBlank())) {
      ValidationResult(
        isValid = false,
        errors = listOf("$fieldName is required"),
      )
    } else {
      ValidationResult(
        isValid = true,
      )
    }
  }
}
