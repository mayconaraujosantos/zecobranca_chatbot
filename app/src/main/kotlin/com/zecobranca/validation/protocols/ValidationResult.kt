
data class ValidationResult(
  val isValid: Boolean,
  val errors: List<String> = emptyList(),
)
