package co.ke.foxlysoft.budgetgain.calc

/**
 * Represents an operator, may be either a binary operator, a unary operator, a function, or a constant
 */
sealed interface CalcOperator

/**
 * Represents a binary operator
 *
 * @property precedence is the precedence of the operator
 * @property isLeftAssociative is true if the operator is left associative, false otherwise
 * @property implementation is the actual implementation of the operator
 */
internal data class CalcBinaryOperator(
    val precedence: Int,
    val isLeftAssociative: Boolean,
    val implementation: (Double, Double) -> Double
) : CalcOperator

internal data class CalcUnaryOperator(
    val isPrefix: Boolean,
    val implementation: (Double) -> Double,
) : CalcOperator

internal data class CalcBothOperator(
    val binary: CalcBinaryOperator,
    val unary: CalcUnaryOperator,
) : CalcOperator

/**
 * Represents a function
 *
 * @property arity is the arity of the function (how many arguments it takes). If null, the function is variadic
 * @property implementation is the actual implementation of the function
 */
internal data class CalcFunction(
    val arity: Int?,
    val implementation: (DoubleArray) -> Double
) : CalcOperator

/**
 * Represents a constant
 *
 * @property value is the value of the constant
 */
internal data class CalcConstant(
    val value: Double
) : CalcOperator

/**
 * Represents a node in an AST and can evaluate its value
 *
 * Can either be an operator, or a leaf (a value)
 */
internal interface Node {
    /**
     * Evaluates the value of this node
     *
     * @return the value of the node
     * @throws CalcZeroDivisionException in case of a zero division
     */
    fun eval(): Double
}

/**
 * An binary operator node
 *
 * @property left is its left child
 * @property op is the actual operator
 * @property right is its right child
 * @constructor Creates an operator node
 */
internal data class BinaryOperatorNode(
    private val left: Node,
    private val op: (Double, Double) -> Double,
    private val right: Node
) : Node {
    override fun eval(): Double = op(left.eval(), right.eval())
}

internal data class UnaryOperatorNode(
    private val op: (Double) -> Double,
    private val child: Node
) : Node {
    override fun eval(): Double = op(child.eval())
}

internal data class FunctionNode(
    private val func: (DoubleArray) -> Double,
    private val children: List<Node>
) : Node {
    override fun eval(): Double = func(children.map(Node::eval).toDoubleArray())
}

/**
 * A value node (leaf)
 *
 * @property value is its value
 * @constructor Creates a value node
 */
internal data class ValueNode(
    private val value: Double
) : Node {
    override fun eval(): Double = value
}