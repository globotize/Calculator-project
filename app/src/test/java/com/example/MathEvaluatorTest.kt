package com.example

import com.example.engine.MathEvaluator
import com.example.model.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun basicArithmetic_precedence() {
        val result = MathEvaluator.evaluate("2 + 3 * 4", AngleMode.DEG).getOrThrow()
        assertEquals(14.0, result.value, 1e-9)
        assertEquals("14", result.formattedResult)
    }

    @Test
    fun parentheses_andImplicitMultiplication() {
        val res1 = MathEvaluator.evaluate("2(3 + 4)", AngleMode.DEG).getOrThrow()
        assertEquals(14.0, res1.value, 1e-9)

        val res2 = MathEvaluator.evaluate("3(2+1)(2)", AngleMode.DEG).getOrThrow()
        assertEquals(18.0, res2.value, 1e-9)
    }

    @Test
    fun trigonometry_degreeMode() {
        val sin30 = MathEvaluator.evaluate("sin(30)", AngleMode.DEG).getOrThrow()
        assertEquals(0.5, sin30.value, 1e-9)

        val cos60 = MathEvaluator.evaluate("cos(60)", AngleMode.DEG).getOrThrow()
        assertEquals(0.5, cos60.value, 1e-9)

        val tan45 = MathEvaluator.evaluate("tan(45)", AngleMode.DEG).getOrThrow()
        assertEquals(1.0, tan45.value, 1e-9)
    }

    @Test
    fun trigonometry_radianMode() {
        val sinHalfPi = MathEvaluator.evaluate("sin(π / 2)", AngleMode.RAD).getOrThrow()
        assertEquals(1.0, sinHalfPi.value, 1e-9)
    }

    @Test
    fun powers_andRoots() {
        val pow = MathEvaluator.evaluate("2^3", AngleMode.DEG).getOrThrow()
        assertEquals(8.0, pow.value, 1e-9)

        val sqrtVal = MathEvaluator.evaluate("sqrt(16)", AngleMode.DEG).getOrThrow()
        assertEquals(4.0, sqrtVal.value, 1e-9)

        val cbrtVal = MathEvaluator.evaluate("cbrt(27)", AngleMode.DEG).getOrThrow()
        assertEquals(3.0, cbrtVal.value, 1e-9)
    }

    @Test
    fun logarithms() {
        val log100 = MathEvaluator.evaluate("log(100)", AngleMode.DEG).getOrThrow()
        assertEquals(2.0, log100.value, 1e-9)

        val lnE = MathEvaluator.evaluate("ln(e)", AngleMode.DEG).getOrThrow()
        assertEquals(1.0, lnE.value, 1e-9)
    }

    @Test
    fun factorial_andCombinatorics() {
        val fact5 = MathEvaluator.evaluate("5!", AngleMode.DEG).getOrThrow()
        assertEquals(120.0, fact5.value, 1e-9)

        val npr = MathEvaluator.evaluate("nPr(5, 2)", AngleMode.DEG).getOrThrow()
        assertEquals(20.0, npr.value, 1e-9)

        val ncr = MathEvaluator.evaluate("nCr(5, 2)", AngleMode.DEG).getOrThrow()
        assertEquals(10.0, ncr.value, 1e-9)
    }

    @Test
    fun unclosedParentheses_autoBalanced() {
        val unclosed = MathEvaluator.evaluate("sin(30", AngleMode.DEG).getOrThrow()
        assertEquals(0.5, unclosed.value, 1e-9)
    }

    @Test
    fun alternateRepresentations_fractionAndHex() {
        val fracRes = MathEvaluator.evaluate("3 / 4", AngleMode.DEG).getOrThrow()
        assertEquals("3/4", fracRes.fractionApprox)

        val hexRes = MathEvaluator.evaluate("255", AngleMode.DEG).getOrThrow()
        assertEquals("0xFF", hexRes.hex)
    }
}
