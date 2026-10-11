package com.example.engine

import com.example.model.AngleMode
import com.example.model.EvaluationResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object MathEvaluator {

    private const val EPSILON = 1e-14

    /**
     * Evaluates a mathematical expression string and returns formatted result details.
     */
    fun evaluate(expression: String, angleMode: AngleMode): Result<EvaluationResult> {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) {
            return Result.failure(IllegalArgumentException("Expression is empty"))
        }

        return try {
            val balanced = balanceParentheses(sanitized)
            val tokens = tokenize(balanced)
            val parser = Parser(tokens, angleMode)
            val value = parser.parseExpression()

            if (value.isNaN()) {
                Result.failure(ArithmeticException("Undefined / Not a Number"))
            } else if (value.isInfinite()) {
                Result.failure(ArithmeticException("Overflow / Division by Zero"))
            } else {
                val formatted = formatNumber(value)
                val fraction = approximateFraction(value)
                val (hex, bin, oct) = getIntegerRepresentations(value)
                Result.success(
                    EvaluationResult(
                        value = value,
                        formattedResult = formatted,
                        fractionApprox = fraction,
                        hex = hex,
                        bin = bin,
                        oct = oct
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Evaluates live expression for real-time preview (safe, non-throwing).
     */
    fun evaluatePreview(expression: String, angleMode: AngleMode): String? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        // If ends with open operator, don't show preview or trim last operator
        val sanitized = sanitize(trimmed)
        val cleaned = if (sanitized.endsWith("+") || sanitized.endsWith("-") ||
            sanitized.endsWith("*") || sanitized.endsWith("/") ||
            sanitized.endsWith("^") || sanitized.endsWith("(")
        ) {
            sanitized.dropLast(1).trim()
        } else {
            sanitized
        }

        if (cleaned.isEmpty()) return null

        return try {
            val balanced = balanceParentheses(cleaned)
            val tokens = tokenize(balanced)
            val parser = Parser(tokens, angleMode)
            val value = parser.parseExpression()
            if (!value.isNaN() && !value.isInfinite()) {
                formatNumber(value)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitize(input: String): String {
        return input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("π", "PI")
            .replace("pi", "PI", ignoreCase = true)
            .replace("φ", "PHI")
            .replace("phi", "PHI", ignoreCase = true)
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("arcsin", "asin")
            .replace("arccos", "acos")
            .replace("arctan", "atan")
            .replace("EXP", "*10^")
            .replace("EE", "*10^")
            .replace(" ", "")
    }

    private fun balanceParentheses(input: String): String {
        var openCount = 0
        var closeCount = 0
        for (ch in input) {
            if (ch == '(') openCount++
            if (ch == ')') closeCount++
        }
        val needed = openCount - closeCount
        return if (needed > 0) input + ")".repeat(needed) else input
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = input.length

        while (i < len) {
            val c = input[i]

            // Numbers (including decimals and scientific e)
            if (c.isDigit() || c == '.') {
                val start = i
                var hasDot = c == '.'
                i++
                while (i < len && (input[i].isDigit() || (!hasDot && input[i] == '.'))) {
                    if (input[i] == '.') hasDot = true
                    i++
                }
                // Check for scientific notation like 1.2e5 or 1.2e-5
                if (i < len && (input[i] == 'e' || input[i] == 'E')) {
                    val eIdx = i
                    i++
                    if (i < len && (input[i] == '+' || input[i] == '-')) {
                        i++
                    }
                    val expStart = i
                    while (i < len && input[i].isDigit()) {
                        i++
                    }
                    if (i == expStart) {
                        // Not a valid exponent, backtrack to eIdx
                        i = eIdx
                    }
                }
                tokens.add(input.substring(start, i))
                continue
            }

            // Word tokens (functions and named constants)
            if (c.isLetter()) {
                val start = i
                while (i < len && (input[i].isLetter() || input[i].isDigit())) {
                    i++
                }
                val word = input.substring(start, i)
                tokens.add(word)
                continue
            }

            // Operators & Punctuation
            when (c) {
                '+', '-', '*', '/', '^', '!', '(', ')', '%', ',' -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    i++ // ignore unrecognized symbols
                }
            }
        }

        // Implicit multiplication insertion
        return insertImplicitMultiplication(tokens)
    }

    private fun insertImplicitMultiplication(tokens: List<String>): List<String> {
        if (tokens.size < 2) return tokens
        val result = mutableListOf<String>()

        fun isValue(t: String): Boolean {
            return isNumber(t) || t == "PI" || t == "PHI" || t == "e" || t == "E" || t == ")" || t == "!"
        }

        fun isPrefix(t: String): Boolean {
            return isNumber(t) || t == "PI" || t == "PHI" || t == "e" || t == "E" || t == "(" || isFunction(t)
        }

        for (i in tokens.indices) {
            val current = tokens[i]
            result.add(current)
            if (i < tokens.size - 1) {
                val next = tokens[i + 1]
                if (isValue(current) && isPrefix(next)) {
                    // Avoid inserting between function and '(' (e.g. sin and '(')
                    if (!(isFunction(current) && next == "(")) {
                        result.add("*")
                    }
                }
            }
        }
        return result
    }

    private fun isNumber(s: String): Boolean {
        return s.toDoubleOrNull() != null
    }

    private fun isFunction(s: String): Boolean {
        return s in setOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "ln", "log", "log2", "sqrt", "cbrt", "abs", "nPr", "nCr"
        )
    }

    private class Parser(
        private val tokens: List<String>,
        private val angleMode: AngleMode
    ) {
        private var pos = 0

        private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): String = tokens[pos++]

        fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < tokens.size) {
                val op = peek()
                if (op == "+" || op == "-") {
                    consume()
                    val nextTerm = parseTerm()
                    value = if (op == "+") value + nextTerm else value - nextTerm
                } else {
                    break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (pos < tokens.size) {
                val op = peek()
                if (op == "*" || op == "/" || op == "%") {
                    consume()
                    val nextFactor = parseFactor()
                    value = when (op) {
                        "*" -> value * nextFactor
                        "/" -> {
                            if (abs(nextFactor) < EPSILON) {
                                throw ArithmeticException("Division by zero")
                            }
                            value / nextFactor
                        }
                        "%" -> value % nextFactor
                        else -> value
                    }
                } else {
                    break
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            val base = parseUnary()
            if (peek() == "^") {
                consume()
                val exponent = parseFactor() // right-associative
                return base.pow(exponent)
            }
            return base
        }

        private fun parseUnary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")
            if (token == "+") {
                consume()
                return parseUnary()
            }
            if (token == "-") {
                consume()
                return -parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (peek() == "!") {
                consume()
                value = factorial(value)
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            // Parentheses: ( expr )
            if (token == "(") {
                consume()
                val expr = parseExpression()
                if (peek() == ")") {
                    consume()
                }
                return expr
            }

            // Constants
            when (token) {
                "PI" -> {
                    consume()
                    return Math.PI
                }
                "e", "E" -> {
                    consume()
                    return Math.E
                }
                "PHI" -> {
                    consume()
                    return 1.618033988749895
                }
            }

            // Numbers
            val num = token.toDoubleOrNull()
            if (num != null) {
                consume()
                return num
            }

            // Functions
            if (isFunction(token)) {
                consume()
                return parseFunctionCall(token)
            }

            throw IllegalArgumentException("Unknown symbol: $token")
        }

        private fun parseFunctionCall(func: String): Double {
            // Function may be followed by '(' expr [',' expr] ')' or just an argument
            val hasParen = peek() == "("
            if (hasParen) consume()

            val arg1 = parseExpression()
            var arg2: Double? = null

            if (peek() == ",") {
                consume()
                arg2 = parseExpression()
            }

            if (hasParen && peek() == ")") {
                consume()
            }

            return evaluateFunction(func, arg1, arg2, angleMode)
        }
    }

    private fun evaluateFunction(func: String, arg: Double, arg2: Double?, angleMode: AngleMode): Double {
        return when (func) {
            "sin" -> {
                val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                cleanNearZero(sin(rad))
            }
            "cos" -> {
                val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                cleanNearZero(cos(rad))
            }
            "tan" -> {
                if (angleMode == AngleMode.DEG) {
                    val normalized = abs(arg % 180.0)
                    if (abs(normalized - 90.0) < 1e-9) {
                        throw ArithmeticException("Tangent undefined at 90°")
                    }
                    cleanNearZero(tan(Math.toRadians(arg)))
                } else {
                    cleanNearZero(tan(arg))
                }
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: asin argument must be in [-1, 1]")
                val rad = asin(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: acos argument must be in [-1, 1]")
                val rad = acos(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "asinh" -> ln(arg + sqrt(arg * arg + 1.0))
            "acosh" -> {
                if (arg < 1.0) throw ArithmeticException("Domain error: acosh argument must be >= 1")
                ln(arg + sqrt(arg * arg - 1.0))
            }
            "atanh" -> {
                if (abs(arg) >= 1.0) throw ArithmeticException("Domain error: |atanh| must be < 1")
                0.5 * ln((1.0 + arg) / (1.0 - arg))
            }
            "ln" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error: ln argument must be > 0")
                ln(arg)
            }
            "log" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error: log argument must be > 0")
                log10(arg)
            }
            "log2" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error: log2 argument must be > 0")
                ln(arg) / ln(2.0)
            }
            "sqrt" -> {
                if (arg < 0.0) throw ArithmeticException("Domain error: square root of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            "nPr" -> {
                val n = arg.roundToLong()
                val r = (arg2 ?: 0.0).roundToLong()
                if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid permutation parameters: n >= r >= 0")
                permutation(n, r)
            }
            "nCr" -> {
                val n = arg.roundToLong()
                val r = (arg2 ?: 0.0).roundToLong()
                if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid combination parameters: n >= r >= 0")
                combination(n, r)
            }
            else -> throw IllegalArgumentException("Unknown function: $func")
        }
    }

    private fun cleanNearZero(v: Double): Double {
        return if (abs(v) < 1e-15) 0.0 else v
    }

    fun factorial(n: Double): Double {
        if (n < 0) throw ArithmeticException("Factorial of negative number undefined")
        // If whole integer
        if (abs(n - round(n)) < 1e-10) {
            val k = round(n).toLong()
            if (k > 170) return Double.POSITIVE_INFINITY
            var result = 1.0
            for (i in 2..k) {
                result *= i.toDouble()
            }
            return result
        }
        // Lanczos Gamma function approximation for non-integers: Γ(n + 1)
        return gammaLanczos(n + 1.0)
    }

    private fun permutation(n: Long, r: Long): Double {
        var res = 1.0
        for (i in (n - r + 1)..n) {
            res *= i.toDouble()
        }
        return res
    }

    private fun combination(n: Long, r: Long): Double {
        val k = if (r > n - r) n - r else r
        var res = 1.0
        for (i in 1..k) {
            res = res * (n - i + 1).toDouble() / i.toDouble()
        }
        return res
    }

    private fun gammaLanczos(z: Double): Double {
        val p = doubleArrayOf(
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.138571095836524,
            9.9843695780195716e-6,
            1.5056327351493116e-7
        )
        if (z < 0.5) {
            return Math.PI / (sin(Math.PI * z) * gammaLanczos(1.0 - z))
        }
        val t = z - 1.0
        var x = 0.99999999999980993
        for (i in p.indices) {
            x += p[i] / (t + i + 1)
        }
        val t1 = t + 7.5
        return sqrt(2.0 * Math.PI) * t1.pow(t + 0.5) * exp(-t1) * x
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        val absVal = abs(value)

        // Snapping very close integers
        if (abs(value - round(value)) < 1e-12) {
            val rounded = round(value).toLong()
            val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
            val df = DecimalFormat("#,##0", symbols)
            return df.format(rounded)
        }

        // Scientific notation for very large or tiny numbers
        if (absVal >= 1e12 || (absVal > 0 && absVal <= 1e-9)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("0.######E0", symbols)
            return df.format(value)
        }

        // Standard decimal formatting up to 10 decimal digits without trailing zeros
        return try {
            val bd = BigDecimal(value.toString())
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            val plain = bd.toPlainString()
            // Format integer part with commas
            val parts = plain.split(".")
            val intPart = parts[0].toLongOrNull()
            if (intPart != null) {
                val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
                val df = DecimalFormat("#,##0", symbols)
                val formattedInt = df.format(intPart)
                if (parts.size > 1) "$formattedInt.${parts[1]}" else formattedInt
            } else {
                plain
            }
        } catch (_: Exception) {
            value.toString()
        }
    }

    /**
     * Approximates value as a rational fraction (e.g. 0.75 -> 3/4)
     */
    fun approximateFraction(value: Double): String? {
        if (value.isNaN() || value.isInfinite()) return null
        val absVal = abs(value)
        if (absVal > 10000 || absVal < 1e-5) return null
        if (abs(value - round(value)) < 1e-7) return null // Already whole integer

        val sign = if (value < 0) "-" else ""
        var x = absVal

        var h1 = 1L
        var h0 = 0L
        var k1 = 0L
        var k0 = 1L

        for (iter in 0..20) {
            val a = floor(x).toLong()
            val h2 = a * h1 + h0
            val k2 = a * k1 + k0

            if (k2 > 10000 || k2 <= 0) break

            val approx = h2.toDouble() / k2.toDouble()
            if (abs(absVal - approx) < 1e-6) {
                return if (k2 != 1L) "$sign$h2/$k2" else null
            }

            val diff = x - a.toDouble()
            if (diff < 1e-9) break
            x = 1.0 / diff

            h0 = h1
            h1 = h2
            k0 = k1
            k1 = k2
        }
        return null
    }

    /**
     * Computes integer representations (Hex, Bin, Oct) if the value is an integer within long range.
     */
    fun getIntegerRepresentations(value: Double): Triple<String?, String?, String?> {
        if (value.isNaN() || value.isInfinite()) return Triple(null, null, null)
        if (abs(value - round(value)) > 1e-9) return Triple(null, null, null)

        val longVal = try {
            round(value).toLong()
        } catch (_: Exception) {
            return Triple(null, null, null)
        }

        // Only compute if within reasonable non-overflowing magnitude
        if (abs(value) > 0xFFFFFFFFFFFFL) return Triple(null, null, null)

        val hex = "0x" + java.lang.Long.toHexString(longVal).uppercase()
        val oct = "0o" + java.lang.Long.toOctalString(longVal)
        val bin = if (abs(longVal) <= 65535) "0b" + java.lang.Long.toBinaryString(longVal) else null

        return Triple(hex, bin, oct)
    }
}
