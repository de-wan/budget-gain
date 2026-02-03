package co.ke.foxlysoft.budgetgain.calc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests on the Shunting-yard algorithm
 */
class GrammarTest {

    /**
     * Tests SYA
     */
    @Test
    fun grammarTest() {
        val operators = CalcBuilder.DEFAULT_RESOURCES
        assertEquals(8.0, "3 + 5 * (2-1)".toAST(operators).eval())
    }

    /**
     * Tests Calc
     */
    @Test
    fun calcTest() {
        assertEquals(50.0, "(2 + 3)(4 + 6)".calc())
        assertEquals(50.0, Calc.eval("(2 + 3)(4 + 6)"))
        assertFailsWith<CalcInvalidExpressionException> {
            "(3+1)) - 2".calc()
        }
        assertFailsWith<CalcZeroDivisionException> {
            "(3+1) / 0".calc()
        }
    }

    /**
     * Tests Calc on empty expression
     */
    @Test
    fun emptyFailTest() {
        assertFailsWith<CalcInvalidExpressionException> {
            "".calc()
        }
        assertFailsWith<CalcInvalidExpressionException> {
            "()".calc()
        }
    }
}