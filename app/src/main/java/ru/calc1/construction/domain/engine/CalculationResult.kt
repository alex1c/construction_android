package ru.calc1.construction.domain.engine

/**
 * Result of a calculation operation.
 * Can be either a successful calculation with results, or an error with a message.
 *
 * @param T Type of the result data (typically Map<String, Double>)
 */
sealed class CalculationResult<out T> {
	
	/**
	 * Successful calculation result.
	 * @param data Map of result field IDs to their calculated values
	 */
	data class Success<T>(val data: T) : CalculationResult<T>()
	
	/**
	 * Calculation error.
	 * @param message Human-readable error message in Russian
	 * @param fieldId Optional ID of the input field that caused the error
	 * @param errorType Type of validation error
	 */
	data class Error(
		val message: String,
		val fieldId: String? = null,
		val errorType: ErrorType = ErrorType.VALIDATION
	) : CalculationResult<Nothing>()
	
	/**
	 * Type of calculation error.
	 */
	enum class ErrorType {
		/** Input validation error (negative, zero, out of range, etc.) */
		VALIDATION,
		
		/** Internal calculation error (division by zero, overflow, etc.) */
		INTERNAL,
		
		/** Missing required input field */
		MISSING_INPUT
	}
	
	/**
	 * Returns true if the result is successful.
	 */
	val isSuccess: Boolean
		get() = this is Success
	
	/**
	 * Returns true if the result is an error.
	 */
	val isError: Boolean
		get() = this is Error
	
	/**
	 * Gets the data if successful, or null otherwise.
	 */
	fun getOrNull(): T? = when (this) {
		is Success -> data
		is Error -> null
	}
	
	/**
	 * Gets the data if successful, or throws an exception if error.
	 */
	fun getOrThrow(): T = when (this) {
		is Success -> data
		is Error -> throw IllegalStateException(message)
	}
}

/**
 * Common error messages in Russian.
 */
object ErrorMessages {
	const val NEGATIVE_VALUE = "Введите положительное значение"
	const val ZERO_NOT_ALLOWED = "Значение должно быть больше нуля"
	const val TOO_LARGE = "Слишком большое значение"
	const val TOO_SMALL = "Слишком маленькое значение"
	const val INVALID_PERCENT = "Процент должен быть от 0 до 100"
	const val MISSING_REQUIRED = "Не заполнено обязательное поле"
	const val INVALID_RANGE = "Значение вне допустимого диапазона"
	const val DIVISION_BY_ZERO = "Деление на ноль невозможно"
	const val INTERNAL_ERROR = "Ошибка при расчёте"
}



