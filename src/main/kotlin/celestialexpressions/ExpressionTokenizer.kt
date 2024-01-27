@file:JvmName("ExpressionTokenizer")

package celestialexpressions

import java.util.regex.Pattern

fun removeIrrelevantChars(input: String): String {
    val out = StringBuilder()

    var inStringD = false
    var inStringS = false

    input.iterator().forEach {
        if (inStringD || inStringS) {
            when (it) {
                '"' -> {
                    inStringD = false
                }
                '\'' -> {
                    inStringS = false
                }
            }
            out.append(it)
        } else when (it) {
            '"' -> {
                out.append(it)
                inStringD = true
            }
            '\'' -> {
                out.append(it)
                inStringS = true
            }
            else -> if (!Pattern.matches("[\\s#]", it.toString())) {
                out.append(it)
            }
        }
    }

    return out.toString()
}
@Throws(ParsingError::class)
fun splitTokens(input: String): ArrayList<BasicToken> {
    val out = ArrayList<BasicToken>()
    if (input.isEmpty()) return out
    var currentTokenType: BasicTokenType? = null
    var tokenStart = 0

    //Keep track of special characters that may only be used a certain number of times in a token
    var dots = 0
    for (i in input.indices) {
        val c = "" + input[i]

        // Ignore Whitespace and '#' symbol (for legacy reasons, may be changed in the future)
        if (c == "\"" || c == "'") {
            if (currentTokenType != BasicTokenType.STRING_LITERAL) {
                addToken(out, input, tokenStart, i, currentTokenType)
                currentTokenType = BasicTokenType.STRING_LITERAL
                tokenStart = i+1
                continue
            } else if (input[i-1] != '\\') {
                addToken(out, input, tokenStart, i, currentTokenType)
                currentTokenType = null
                continue
            }
        } else if (currentTokenType == BasicTokenType.STRING_LITERAL) {
            continue
        } else if (Pattern.matches("[\\s#]", c)) continue
        if (currentTokenType == BasicTokenType.STRING_LITERAL) {
            continue
            // Detect splitters
        } else if (c.equals(",")) {
            addToken(out, input, tokenStart, i, currentTokenType)
            currentTokenType = BasicTokenType.OPERATOR
            tokenStart = i
        // Detect numerical constant
        } else if (Pattern.matches("[\\d.]", c) && (currentTokenType == BasicTokenType.OPERATOR || currentTokenType == null || currentTokenType == BasicTokenType.CONST)) {
            if (currentTokenType != BasicTokenType.CONST) {
                dots = 0
                addToken(out, input, tokenStart, i, currentTokenType)
                currentTokenType = BasicTokenType.CONST
                tokenStart = i
            }
            if (c == ".") {
                if (dots >= 1) throw ParsingError("Multiple periods in one decimal number at index $i.")
                dots++
            }
            // Detect operators and grouping symbols
        } else if (Pattern.matches("[+-/*^()&|=><]", c)) {
            addToken(out, input, tokenStart, i, currentTokenType)
            currentTokenType = BasicTokenType.OPERATOR
            tokenStart = i
            // Detect variables
        } else if (currentTokenType != BasicTokenType.VARIABLE) {
            var func = false
            for (j in i until input.length) {
                val c2 = input[j]
                if (c2 == '(') {
                    func = true
                    break
                } else if (Pattern.matches("[\\d.+-/*^)&|=><#]", c2.toString())) break
            }
            if (!func) {
                addToken(out, input, tokenStart, i, currentTokenType)
                currentTokenType = BasicTokenType.VARIABLE
                tokenStart = i
            } else if (currentTokenType != BasicTokenType.OPERATOR || (tokenStart-i==-1 && Pattern.matches("[\\d.+-/*^()&|=><#]", input[tokenStart].toString()))) {
                addToken(out, input, tokenStart, i, currentTokenType)
                currentTokenType = BasicTokenType.OPERATOR
                tokenStart = i
            }
        }
    }
    addToken(out, input, tokenStart, input.length, currentTokenType)
    return out
}

fun identifyTokens(input: ArrayList<BasicToken>): ArrayList<Token> {
    val array = ArrayList<Token>()
    val iter = input.listIterator()
    while (iter.hasNext()) {
        val i = iter.nextIndex()
        val token = iter.next()
        array.add(
            Token(token.text,
            when (token.type) {
            BasicTokenType.CONST ->  TokenType.CONST

            BasicTokenType.STRING_LITERAL -> TokenType.STRING_LITERAL

            BasicTokenType.VARIABLE ->  TokenType.VARIABLE

            BasicTokenType.OPERATOR -> {
                when (token.text) {
                    "+","*","/","^","&","|","=",">","<" -> {
                        TokenType.BINARY
                    }
                    "-" -> {
                        if (i==0) TokenType.UNARY
                        else
                            when (array.last().type) {
                                TokenType.BINARY, TokenType.GROUPING_START, TokenType.UNARY, TokenType.SPLITTER -> TokenType.UNARY
                                else -> TokenType.BINARY
                            }
                    }
                    ")" -> TokenType.GROUPING_END
                    else -> {
                        when (token.text.last()) {
                            '(' -> TokenType.GROUPING_START
                            ')' -> TokenType.NULLARY
                            ',' -> TokenType.SPLITTER
                            else -> {
                                if (iter.next().text == "(") TokenType.GROUPING_START
                                else throw ParsingError("Please report this exception.")
                            }
                        }
                    }
                }
            }
        })
        )
    }
    return array
}

private fun addToken(out: ArrayList<BasicToken>, input: String, start: Int, end: Int, type: BasicTokenType?) {
    if (type == null) return
    out.add(
        BasicToken(
            input.substring(start, end),
            type
    )
    )
}

class BasicToken(var text: String, var type: BasicTokenType) {
    override fun toString(): String {
        return "$text: $type"
    }
}

class Token(var text: String, var type: TokenType) {
    override fun toString(): String {
        return "$text: $type"
    }

    fun getExpression(context: ExpressionContext): IExpression<out Any>? {
        return when (this.type) {
            TokenType.CONST -> Expression.Const(this.text.toDouble())
            TokenType.VARIABLE -> Expression.Var(this.text, context)
            TokenType.NULLARY -> Expression.Var(this.text, context)
            TokenType.UNARY -> Expression.Negate()
            TokenType.BINARY -> when (this.text) {
                "+" -> Expression.Add()
                "-" -> Expression.Sub()
                "*" -> Expression.Mul()
                "/" -> Expression.Div()
                "^" -> Expression.Pow()
                "&" -> Expression.And()
                "|" -> Expression.Or()
                "=" -> Expression.Eq()
                ">" -> Expression.Gtr()
                "<" -> Expression.Lss()
                else -> throw ParsingError("Invalid character was not caught. Please report this exception.")
            }
            TokenType.SPLITTER -> null
            TokenType.GROUPING_START -> null
            TokenType.GROUPING_END -> null
            TokenType.STRING_LITERAL -> SExpression.Const(this.text)
        }
    }
}

class ParsingError(s: String) : Exception(s)

enum class BasicTokenType {
    CONST, VARIABLE, OPERATOR, STRING_LITERAL
}

enum class TokenType {
    CONST, VARIABLE, NULLARY, UNARY, BINARY, GROUPING_START, GROUPING_END, SPLITTER, STRING_LITERAL
}