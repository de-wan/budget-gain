package co.ke.foxlysoft.budgetgain.calc

/**
 * Main class for evaluating mathematical expressions.
 * It can be customized with additional operators, functions, and constants.
 */
class Calc internal constructor(private val resources: Map<String, CalcOperator>) {

    /**
     * Creates a new instance which contains a binary operator.
     *
     * @param symbol The symbol representing the operator.
     * @param precedence The precedence of the operator.
     * @param isLeftAssociative Whether the operator is left associative.
     * @param implementation The implementation of the operator.
     * @return This Calc instance.
     * @throws CalcDSLException If one of the fields isn't set properly.
     */
    fun withBinaryOperator(
        symbol: Char,
        precedence: Int,
        isLeftAssociative: Boolean,
        implementation: (Double, Double) -> Double
    ): Calc = CalcBuilder(resources)
        .binaryOperator {
            this.symbol = symbol
            this.precedence = precedence
            this.isLeftAssociative = isLeftAssociative
            this.implementation = implementation
        }
        .build()


    /**
     * Adds a unary operator to this Calc instance.
     *
     * @param symbol The symbol representing the operator.
     * @param isPrefix Whether the operator is prefix.
     * @param implementation The implementation of the operator.
     * @return This Calc instance.
     * @throws CalcDSLException If one of the fields isn't set properly.
     */
    fun withUnaryOperator(
        symbol: Char,
        isPrefix: Boolean,
        implementation: (Double) -> Double
    ): Calc = CalcBuilder(resources)
        .unaryOperator {
            this.symbol = symbol
            this.isPrefix = isPrefix
            this.implementation = implementation
        }
        .build()

    /**
     * Adds a function to this Calc instance.
     *
     * @param name The name of the function.
     * @param arity The number of arguments the function takes. `null` if the function should be variadic.
     * @param implementation The implementation of the function.
     * @return This Calc instance.
     * @throws CalcDSLException If one of the fields isn't set properly.
     */
    fun withFunction(
        name: String,
        arity: Int? = null,
        implementation: (DoubleArray) -> Double
    ): Calc = CalcBuilder(resources)
        .function {
            this.name = name
            this.arity = arity
            this.implementation = implementation
        }
        .build()

    /**
     * Adds a constant to this Calc instance.
     *
     * @param name The name of the constant.
     * @param value The value of the constant.
     * @return This Calc instance.
     * @throws CalcDSLException If one of the fields isn't set properly.
     */
    fun withConstant(
        name: String,
        value: Double
    ): Calc = CalcBuilder(resources)
        .constant {
            this.name = name
            this.value = value
        }
        .build()

    /**
     * Adds the default resources to this Calc instance.
     *
     * @return This Calc instance.
     */
    fun withDefault(): Calc = CalcBuilder(resources).includeDefault().build()

    /**
     * Evaluates a mathematical expression.
     *
     * @param mathExpression The mathematical expression to evaluate.
     * @return The result of the evaluation.
     * @throws CalcInvalidSymbolException If there's an invalid operator in the expression.
     * @throws CalcInvalidExpressionException If the expression is invalid (i.e., mismatched parentheses).
     * @throws CalcZeroDivisionException If a division by zero occurs.
     */
    fun eval(
        mathExpression: String,
    ): Double {
        val operators = resourcesView()
        return mathExpression.toAST(operators).eval()
    }

    /**
     * Returns the resources of this [Calc] instance.
     * The tokenizer assumes multiplication, hence disallowing overriding `*` operator
     */
    fun resourcesView(): Map<String, CalcOperator> =
        resources + ("*" to CalcBinaryOperator(3, true) { a, b -> a * b })

    companion object {

        /**
         * Creates a new instance of [Calc] with the provided resources.
         *
         * @param generator A lambda function that configures a CalcBuilder instance.
         * @return The new instance of Calc.
         * @throws CalcDSLException If one of the fields isn't set properly.
         */
        fun create(generator: CalcBuilder.() -> Unit = { includeDefault() }): Calc =
            CalcBuilder().apply(generator).build()

        /**
         * Evaluates a mathematical expression using the default resources.
         *
         * @param mathExpression The mathematical expression to evaluate.
         * @return The result of the evaluation.
         * @throws CalcInvalidSymbolException If there's an invalid operator in the expression.
         * @throws CalcInvalidExpressionException If the expression is invalid (i.e., mismatched parentheses).
         * @throws CalcZeroDivisionException If a division by zero occurs.
         */
        fun eval(
            mathExpression: String,
        ): Double = mathExpression.toAST(CalcBuilder.DEFAULT_RESOURCES).eval()
    }
}

/**
 * Evaluates a mathematical expression using the provided resources.
 *
 * @receiver The mathematical expression to evaluate.
 * @param generator A lambda function that configures a CalcBuilder instance.
 * @return The result of the evaluation.
 * @throws CalcInvalidSymbolException If there's an invalid operator in the expression.
 * @throws CalcInvalidExpressionException If the expression is invalid (i.e., mismatched parentheses).
 * @throws CalcZeroDivisionException If a division by zero occurs.
 * @throws CalcDSLException If one of the fields isn't set properly.
 */
fun String.calc(
    generator: CalcBuilder.() -> Unit
): Double = CalcBuilder().apply(generator).build().eval(this)

/**
 * Evaluates a mathematical expression using the default resources.
 *
 * @receiver The mathematical expression to evaluate.
 * @return The result of the evaluation.
 * @throws CalcInvalidSymbolException If there's an invalid operator in the expression.
 * @throws CalcInvalidExpressionException If the expression is invalid (i.e., mismatched parentheses).
 * @throws CalcZeroDivisionException If a division by zero occurs.
 */
fun String.calc(): Double = Calc.eval(this)