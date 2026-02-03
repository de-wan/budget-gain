package co.ke.foxlysoft.budgetgain.calc

/**
 * Generic Calc Exception
 *
 * @param message is the message to display in the stacktrace
 */
sealed class CalcException(message: String) : RuntimeException(message)

/**
 * Invalid Expression Exception, is thrown when the expression is considered invalid (i.e. Mismatched parenthesis or missing operands)
 *
 * @property expression is the invalid expression
 * @property position is the estimated position of the error
 */
open class CalcInvalidExpressionException internal constructor(
    val expression: String,
    val position: Int,
    extraMessage: String = ""
) : CalcException(
    "Invalid expression at position $position in $expression${
        if (extraMessage.isNotBlank()) ", $extraMessage" else ""
    }"
)

/**
 * Invalid Operator Exception, is thrown when an invalid/unknown operator is found
 *
 * @property invalidSymbol is the given invalid operator
 * @param expression is the invalid expression
 * @param position is the estimated position of the error
 */
class CalcInvalidSymbolException internal constructor(
    val invalidSymbol: String,
    expression: String,
    position: Int,
    message: String = ""
) : CalcInvalidExpressionException(expression, position, message)

/**
 * Zero Division Exception, is thrown when a zero division occurs (i.e. x/0, x%0)
 */
class CalcZeroDivisionException : CalcException("Division by zero")

/**
 * Invalid Argument Exception, is thrown when a given argument to an operator or a function is invalid.
 * For example, when a negative number or a non-integer number is given to the factorial function
 *
 * @param message is the message to display in the stacktrace
 */
class CalcInvalidArgumentException(message: String) : CalcException(message)

/**
 * DSL Exception, is thrown when a required field isn't defined
 *
 * @param what is the name of the undefined field
 */
class CalcDSLException internal constructor(what: String) :
    CalcException("All required fields must be properly defined: $what")