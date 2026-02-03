package co.ke.foxlysoft.budgetgain.calc

import kotlin.math.*

/**
 * This class is used to build a Calc instance with custom operators, functions, and constants.
 */
class CalcBuilder internal constructor(
    baseResources: Map<String, CalcOperator> = mapOf()
) {
    private val resources: MutableMap<String, CalcOperator> = baseResources.toMutableMap()

    /**
     * Includes the default resources (operators, functions, constants) to the current Calc instance.
     */
    fun includeDefault(): CalcBuilder = apply {
        resources += DEFAULT_RESOURCES
    }

    /**
     * Defines a binary operator for the current Calc instance.
     *
     * @param definition A lambda function that configures a BinaryOperatorBuilder instance.
     */
    fun binaryOperator(definition: BinaryOperatorBuilder.() -> Unit): CalcBuilder = apply {
        val op = BinaryOperatorBuilder().apply(definition)
        validateOperator(op.symbol, op.precedence, op.implementation)
        addOperator(op.symbol!!, CalcBinaryOperator(op.precedence!!, op.isLeftAssociative!!, op.implementation!!), isUnary = false)
    }

    /**
     * Defines a unary operator for the current Keval instance.
     *
     * @param definition A lambda function that configures a UnaryOperatorBuilder instance.
     */
    fun unaryOperator(definition: UnaryOperatorBuilder.() -> Unit): CalcBuilder = apply {
        val op = UnaryOperatorBuilder().apply(definition)
        validateUnaryOperator(op.symbol, op.isPrefix, op.implementation)
        addOperator(op.symbol!!, CalcUnaryOperator(op.isPrefix!!, op.implementation!!), isUnary = true)
    }

    /**
     * Defines a function for the current Keval instance.
     *
     * @param definition A lambda function that configures a FunctionBuilder instance.
     */
    fun function(definition: FunctionBuilder.() -> Unit): CalcBuilder = apply {
        val fn = FunctionBuilder().apply(definition)
        validateFunction(fn.name, fn.arity, fn.implementation)
        resources[fn.name!!] = CalcFunction(fn.arity, fn.implementation!!)
    }

    /**
     * Defines a constant for the current Keval instance.
     *
     * @param definition A lambda function that configures a ConstantBuilder instance.
     */
    fun constant(definition: ConstantBuilder.() -> Unit): CalcBuilder = apply {
        val const = ConstantBuilder().apply(definition)
        validateConstant(const.name, const.value)
        resources[const.name!!] = CalcConstant(const.value!!)
    }

    /**
     * Builds the Calc instance with the defined resources.
     *
     * @return A Calc instance.
     */
    fun build(): Calc = Calc(resources)

    private fun validateOperator(symbol: Char?, precedence: Int?, implementation: ((Double, Double) -> Double)?) {
        requireNotNull(symbol) { "symbol is not set" }
        requireNotNull(implementation) { "implementation is not set" }
        requireNotNull(precedence) { "precedence is not set" }
        require(precedence >= 0) { "operator's precedence must always be positive or 0" }
        require(symbol.isOperatorSymbol()) { "a symbol must NOT be a letter, a digit, an underscore, parentheses nor a comma but was: $symbol" }
        require(symbol != '*') { "* cannot be overwritten" }
    }

    private fun validateUnaryOperator(symbol: Char?, isPrefix: Boolean?, implementation: ((Double) -> Double)?) {
        requireNotNull(symbol) { "symbol is not set" }
        requireNotNull(isPrefix) { "isPrefix is not set" }
        requireNotNull(implementation) { "implementation is not set" }
        require(symbol.isOperatorSymbol()) { "a symbol must NOT be a letter, a digit, an underscore, parentheses nor a comma but was: $symbol" }
    }

    private fun validateFunction(name: String?, arity: Int?, implementation: Any?) {
        requireNotNull(name) { "name is not set" }
        requireNotNull(implementation) { "implementation is not set" }
        require(arity == null || arity >= 0) { "function's arity must always be positive or 0" }
        require(name.isFunctionOrConstantName()) { "a function's name cannot start with a digit and must contain only letters, digits or underscores: $name" }
    }

    private fun validateConstant(name: String?, value: Double?) {
        requireNotNull(name) { "name is not set" }
        requireNotNull(value) { "value is not set" }
        require(name.isFunctionOrConstantName()) { "a constant's name cannot start with a digit and must contain only letters, digits or underscores: $name" }
    }

    private fun Char.isOperatorSymbol() = !isLetterOrDigit() && this !in listOf('_', '(', ')', ',')
    private fun String.isFunctionOrConstantName() =
        isNotEmpty() && this[0] !in '0'..'9' && !contains("[^a-zA-Z0-9_]".toRegex())

    private fun addOperator(symbol: Char, operator: CalcOperator, isUnary: Boolean) {
        when (val resource = resources[symbol.toString()]) {
            is CalcUnaryOperator -> resources[symbol.toString()] =
                if (isUnary) operator as CalcUnaryOperator
                else CalcBothOperator(operator as CalcBinaryOperator, resource)

            is CalcBinaryOperator -> resources[symbol.toString()] =
                if (isUnary) CalcBothOperator(resource, operator as CalcUnaryOperator)
                else operator as CalcBinaryOperator

            is CalcBothOperator -> resources[symbol.toString()] =
                if (isUnary) CalcBothOperator(resource.binary, operator as CalcUnaryOperator)
                else CalcBothOperator(operator as CalcBinaryOperator, resource.unary)

            else -> resources[symbol.toString()] = operator
        }
    }

    companion object {

        val DEFAULT_RESOURCES: Map<String, CalcOperator> = mapOf(
            // binary operators
            "+" to CalcBothOperator(
                CalcBinaryOperator(2, true) { a, b -> a + b },
                CalcUnaryOperator(true) { it }
            ),
            "-" to CalcBothOperator(
                CalcBinaryOperator(2, true) { a, b -> a - b },
                CalcUnaryOperator(true) { -it }
            ),

            "/" to CalcBinaryOperator(3, true) { a, b ->
                if (b == 0.0) throw CalcZeroDivisionException()
                a / b
            },
            "%" to CalcBinaryOperator(3, true) { a, b ->
                if (b == 0.0) throw CalcZeroDivisionException()
                a % b
            },
            "^" to CalcBinaryOperator(4, false) { a, b -> a.pow(b) },
            "*" to CalcBinaryOperator(3, true) { a, b -> a * b },

            // unary operators
            "!" to CalcUnaryOperator(false) {
                if (it < 0) throw CalcInvalidArgumentException("factorial of a negative number")
                if (floor(it) != it) throw CalcInvalidArgumentException("factorial of a non-integer")
                var result = 1.0
                for (i in 2..it.toInt()) {
                    result *= i
                }
                result
            },

            // functions
            "neg" to CalcFunction(1) { -it[0] },
            "abs" to CalcFunction(1) { it[0].absoluteValue },
            "sqrt" to CalcFunction(1) { sqrt(it[0]) },
            "cbrt" to CalcFunction(1) { cbrt(it[0]) },
            "exp" to CalcFunction(1) { exp(it[0]) },
            "ln" to CalcFunction(1) { ln(it[0]) },
            "log10" to CalcFunction(1) { log10(it[0]) },
            "log2" to CalcFunction(1) { log2(it[0]) },
            "sin" to CalcFunction(1) { sin(it[0]) },
            "cos" to CalcFunction(1) { cos(it[0]) },
            "tan" to CalcFunction(1) { tan(it[0]) },
            "asin" to CalcFunction(1) { asin(it[0]) },
            "acos" to CalcFunction(1) { acos(it[0]) },
            "atan" to CalcFunction(1) { atan(it[0]) },
            "ceil" to CalcFunction(1) { ceil(it[0]) },
            "floor" to CalcFunction(1) { floor(it[0]) },
            "round" to CalcFunction(1) { round(it[0]) },

            // constants
            "PI" to CalcConstant(PI),
            "e" to CalcConstant(E)
        )

        /**
         * Builder representation of a binary operator.
         *
         * @property symbol The symbol which represents the operator.
         * @property precedence The precedence of the operator.
         * @property isLeftAssociative True when the operator is left associative, false otherwise.
         * @property implementation The actual implementation of the operator.
         */
        data class BinaryOperatorBuilder(
            var symbol: Char? = null,
            var precedence: Int? = null,
            var isLeftAssociative: Boolean? = null,
            var implementation: ((Double, Double) -> Double)? = null
        )

        /**
         * Builder representation of a unary operator.
         *
         * @property symbol The symbol which represents the operator.
         * @property isPrefix True when the operator is prefix, false otherwise.
         * @property implementation The actual implementation of the operator.
         */
        data class UnaryOperatorBuilder(
            var symbol: Char? = null,
            var isPrefix: Boolean? = null,
            var implementation: ((Double) -> Double)? = null
        )

        /**
         * Builder representation of a function.
         *
         * @property name The identifier which represents the function.
         * @property arity The arity of the function (how many arguments it takes). If null, the function is variadic
         * @property implementation The actual implementation of the function.
         */
        data class FunctionBuilder(
            var name: String? = null,
            var arity: Int? = null,
            var implementation: ((DoubleArray) -> Double)? = null,
        )

        /**
         * Builder representation of a constant.
         *
         * @property name The identifier which represents the constant.
         * @property value The value of the constant.
         */
        data class ConstantBuilder(
            var name: String? = null,
            var value: Double? = null
        )
    }
}