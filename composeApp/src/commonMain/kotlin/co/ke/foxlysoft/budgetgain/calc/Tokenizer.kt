package co.ke.foxlysoft.budgetgain.calc

private enum class TokenType {
    FIRST, OPERAND, OPERATOR, LPAREN, RPAREN, COMMA,
}

private fun shouldAssumeMul(tokenType: TokenType): Boolean =
    tokenType == TokenType.OPERAND || tokenType == TokenType.RPAREN

// normalize tokens to be of specific form (add product symbols where they should be assumed)
private fun Sequence<String>.normalizeTokens(symbols: Map<String, CalcOperator>): List<String> {
    var currentPos = 0
    var prevToken = TokenType.FIRST
    var parenthesesCount = 0
    val functionAtCount = mutableListOf(-1)
    val ret = mutableListOf<String>()
    this.forEach { token ->
        prevToken = when {
            token.isNumeric() || symbols[token] is CalcConstant -> TokenType.OPERAND.also {
                if (shouldAssumeMul(prevToken)) ret.add("*")
                ret.add(token)
            }

            token.isCalcOperator(symbols.keys) -> TokenType.OPERATOR.also {
                if (shouldAssumeMul(prevToken) && (symbols[token] is CalcConstant || symbols[token] is CalcFunction)) {
                    ret.add("*")
                }
                ret.add(token)
                if (symbols[token] is CalcFunction) {
                    functionAtCount.add(parenthesesCount + 1)
                }
            }

            token == "(" -> TokenType.LPAREN.also {
                parenthesesCount += 1
                if (shouldAssumeMul(prevToken)) ret.add("*")
                ret.add(token)
            }

            token == ")" -> TokenType.RPAREN.also {
                parenthesesCount -= 1
                ret.add(token)
            }

            token == "," -> TokenType.COMMA.also {
                if (functionAtCount.last() == parenthesesCount) {
                    ret.add(token)
                } else {
                    throw CalcInvalidSymbolException(
                        token, joinToString(""), currentPos, "comma can only be used in the context of a function"
                    )
                }
            }

            else -> throw CalcInvalidSymbolException(token, joinToString(""), currentPos)
        }
        currentPos += token.length
    }
    return ret
}

/**
 * Checks if a string is numeric or not
 *
 * @receiver is the string to check
 * @return true if the string is numeric, false otherwise
 */
internal fun String.isNumeric(): Boolean {
    toDoubleOrNull() ?: return false
    return true
}

/**
 * Checks if a string is a Calc Operator or not
 *
 * @receiver is the string to check
 * @return true if the string is a valid operator, false otherwise
 */
internal fun String.isCalcOperator(symbolsSet: Set<String>): Boolean = this in symbolsSet

/**
 * Tokenizes a mathematical expression
 *
 * @receiver is the string to tokenize
 * @return the list of tokens
 * @throws CalcInvalidSymbolException if the expression contains an invalid symbol
 */
internal fun String.tokenize(symbolsSet: Map<String, CalcOperator>): List<String> =
    TOKENIZER_REGEX.findAll(this)
        .map(MatchResult::value)
        .filter(String::isNotBlank)
        .map { SANITIZE_REGEX.replace(it, "") }
        .normalizeTokens(symbolsSet)

private val SANITIZE_REGEX = """\s+""".toRegex()

private val TOKENIZER_REGEX = """(\d+\.\d+|\d+|[a-zA-Z_]\w*|[^\w\s])""".toRegex()