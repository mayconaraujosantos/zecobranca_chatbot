package com.zecobranca.validation.protocols

import ValidationResult

interface Validation {
  fun validate(input: Map<String, Any?>): ValidationResult
}
