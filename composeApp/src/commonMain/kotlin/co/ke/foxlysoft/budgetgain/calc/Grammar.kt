package co.ke.foxlysoft.budgetgain.calc

private fun String.isDouble(): Boolean = this.toDoubleOrNull() != null
private fun String.pluralize(count: Int): String = if (count == 1) this else "${this}s"

internal class Parser(
    private val tokens: Iterator<String>,
    private val tokensToString: String,
    private val operators: Map<String, CalcOperator>
) {
    private var currentTokenOrNull: String? = tokens.next()
    private val currentToken: String
        get() = currentTokenOrNull ?: throw CalcInvalidExpressionException(tokensToString, -1)

    private var currentPos = 0
    private var openParenthesesCount = 0

    private fun consume(expected: String) {
        if (currentTokenOrNull != expected) {
            throw CalcInvalidExpressionException(
                tokensToString,
                currentPos,
                "expected $expected but found ${currentTokenOrNull ?: "end of expression"}",
            )
        }

        if (currentToken == "(") {
            openParenthesesCount++
        } else if (currentToken == ")") {
            if (openParenthesesCount == 0) {
                throw CalcInvalidExpressionException(
                    tokensToString,
                    currentPos,
                    "unexpected closing parenthesis"
                )
            }
            openParenthesesCount--
        }
        currentPos += currentTokenOrNull?.length ?: 0 // Update the current position
        currentTokenOrNull = if (tokens.hasNext()) tokens.next() else null
    }

    private fun isBinaryOrBoth(token: String): Boolean = operators[token].let {
        it is CalcBinaryOperator || it is CalcBothOperator
    }

    private fun isUnaryOrBothPrefix(token: String): Boolean = operators[token].let {
        (it is CalcUnaryOperator && it.isPrefix) || (it is CalcBothOperator && it.unary.isPrefix)
    }

    private fun isUnaryOrBothPostfix(token: String): Boolean = operators[token].let {
        (it is CalcUnaryOperator && !it.isPrefix) || (it is CalcBothOperator && !it.unary.isPrefix)
    }

    private fun getBinaryOperator(token: String): CalcBinaryOperator = operators[token].let {
        if (it is CalcBothOperator) {
            it.binary
        } else {
            it as CalcBinaryOperator
        }
    }

    private fun getUnaryOperator(token: String): CalcUnaryOperator = operators[token].let {
        if (it is CalcBothOperator) {
            it.unary
        } else {
            it as CalcUnaryOperator
        }
    }

    private fun handleBinaryOperator(node: Node, minPrecedence: Int): Node {
        var result = node
        while (currentTokenOrNull != null && isBinaryOrBoth(currentToken)) {
            val op = getBinaryOperator(currentToken)
            if (op.precedence < minPrecedence) break
            consume(currentToken)
            val rightAssociativity = if (op.isLeftAssociative) 1 else 0
            result = BinaryOperatorNode(result, op.implementation, expression(op.precedence + rightAssociativity))
        }
        return result
    }

    private fun handleUnaryOperator(node: Node? = null): Node {
        val op = getUnaryOperator(currentToken)
        consume(currentToken)
        return UnaryOperatorNode(op.implementation, node ?: primary())
    }

    private fun handleFunction(): Node {
        val functionName = currentToken
        consume(functionName)
        val op = operators[functionName] as CalcFunction
        consume("(")
        val args = mutableListOf<Node>()
        while (currentTokenOrNull != ")") {
            args.add(expression())
            if (op.arity != null && args.size > op.arity) {
                throw CalcInvalidExpressionException(
                    tokensToString,
                    currentPos,
                    "expected ${op.arity} ${"argument".pluralize(op.arity)} but found ${args.size}",
                )
            }
            if (currentTokenOrNull == ",") {
                consume(",")
            }
        }
        consume(")")
        if (op.arity != null && args.size < op.arity) {
            throw CalcInvalidExpressionException(
                tokensToString,
                currentPos,
                "expected ${op.arity} ${"argument".pluralize(op.arity)} but found ${args.size}",
            )
        }
        return FunctionNode(op.implementation, args)
    }

    private fun handleConstant(): Node {
        val op = operators[currentToken] as CalcConstant
        consume(currentToken)
        return ValueNode(op.value)
    }

    private fun expression(minPrecedence: Int = 0): Node {
        var node = primary()
        while (currentTokenOrNull != null && isUnaryOrBothPostfix(currentToken)) {
            node = handleUnaryOperator(node)
        }
        node = handleBinaryOperator(node, minPrecedence)
        return node
    }

    private fun primary(): Node {
        if (currentTokenOrNull != null && isUnaryOrBothPrefix(currentToken)) {
            return handleUnaryOperator()
        } else if (currentTokenOrNull == "(") {
            consume("(")
            val node = expression()
            consume(")")
            return node
        } else if (operators.containsKey(currentTokenOrNull)) {
            val op = operators[currentToken]
            if (op is CalcFunction) {
                return handleFunction()
            } else if (op is CalcConstant) {
                return handleConstant()
            }
        }
        val token = currentToken
        if (!token.isDouble()) {
            throw CalcInvalidExpressionException(
                tokensToString,
                currentPos,
                "expected number or symbol but found $token",
            )
        }
        consume(currentToken)
        val node = ValueNode(token.toDouble())
        return node
    }

    fun parse(): Node {
        val node = expression()
        if (currentTokenOrNull != null) {
            throw CalcInvalidExpressionException(
                tokensToString,
                currentPos,
                "unexpected token $currentTokenOrNull"
            )
        }
        return node
    }
}

/**
 * Converts an infix mathematical expression into an abstract syntax tree,
 * The operators that are supported are defined in the operators map, which each have a precedence and associativity.
 *
 * @receiver the string to convert
 * @return the abstract syntax tree
 * @throws CalcInvalidSymbolException if the expression contains an invalid symbol
 * @throws CalcInvalidExpressionException if the expression is invalid (i.e. mismatched parenthesis, missing operand, or empty expression)
 */
internal fun String.toAST(operators: Map<String, CalcOperator>): Node {
    if (this.replace("""[()]""".toRegex(), "").isBlank())
        throw CalcInvalidExpressionException("", -1)

    val tokens = this.tokenize(operators)
    val tokensToString = tokens.joinToString("")

    val parser = Parser(tokens.iterator(), tokensToString, operators)
    return parser.parse()
}