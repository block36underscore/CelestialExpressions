@file:JvmName("ExpressionCompiler")

package celestialexpressions

@Throws(ParsingError::class)
fun compile(source: String, context: ExpressionContext): Expression =
    assembleExpression(
    validateExpression(
    identifyTokens(
    splitTokens(
    source)
    )
    ), context)

fun compile(source: String) = compile(source, ExpressionContext())

fun compile(source: String, modules: Collection<String>) = compile(source, ExpressionContext(registrar.getModules(modules)))

@Throws(InvalidExpressionError::class)
fun validateExpression(input: ArrayList<Token>) : ArrayList<Token> {
    var grouping = 0
    for (token in input) {
        if (token.type == TokenType.GROUPING_START) grouping++
        else if (token.type == TokenType.GROUPING_END) grouping--
    }

    if (grouping != 0) throw InvalidExpressionError("Grouping symbols are not balanced.")

    return input
}


fun assembleExpression(input: ArrayList<Token>, context: ExpressionContext) = buildExpressionTree(input, context).getExpression()

fun getNextExpression(tokens: ListIterator<Token>, context: ExpressionContext): Expression {
    val token = tokens.next()
    return if (token.type == TokenType.GROUPING_START && token.text == "(") {
        val subTokenArray = ArrayList<Token>()
        var depth = 1
        while (true) {
            val toAdd = tokens.next()
            if (toAdd.type == TokenType.GROUPING_START) depth++
            else if (toAdd.type == TokenType.GROUPING_END) depth--
            if (depth > 0) subTokenArray.add(toAdd)
            else break
        }
        buildExpressionTree(subTokenArray, context).getExpression()
    } else if (token.type == TokenType.GROUPING_START) {
        processFunction(token, tokens, context)
    } else if (token.type != TokenType.GROUPING_END) token.getExpression(context)!!
    else throw InvalidExpressionError("Attempted to get expression from end grouping symbol. This is guaranteed to be a bug in the compiler")
}

fun processFunction(token: Token, tokens: ListIterator<Token>, context: ExpressionContext): Expression {
    val name = token.text
    val params = ArrayList<ArrayList<Token>>()
    var depth = 1

    scan@while (true) {
        val subTokenArray = ArrayList<Token>()
        while (true) {
            val toAdd = tokens.next()
            if (toAdd.type == TokenType.GROUPING_START) depth++
            else if (toAdd.type == TokenType.GROUPING_END) {
                depth--
                if (depth == 0) {
                    params.add(subTokenArray)
                    break@scan
                }
            }
            else if (toAdd.type == TokenType.SPLITTER) {
                if (depth == 1) break
            }
            subTokenArray.add(toAdd)
        }
        params.add(subTokenArray)
    }
    val expressions = ArrayList<Expression>()
    params.forEach {
        expressions.add(buildExpressionTree(it, context).getExpression())
    }
    val function = context.getFunction(name)
    if (function.size?.equals(params.size) == false) throw InvalidExpressionError(
        "celestialexpressions.Function $name takes ${function.size} parameter${if (function.size == 1) "" else "s"}, but ${params.size} ${if(params.size==1) "was" else "were"} provided")
    return Expression.Fun(expressions, context.getFunction(name))
}

interface ExpressionTreeElement

class ExpressionTreePart(val operator: Expression.BinaryOperator, var element: Expression): ExpressionTreeElement

class ExpressionTree(var start: Expression, val elements: ArrayList<ExpressionTreePart> = ArrayList()):
    ExpressionTreeElement {
    fun getExpression(): Expression {
        if (elements.isEmpty()) return this.start

        val iter = this.elements.listIterator()
        while (iter.hasNext()) {
            val index = iter.nextIndex()
            val element = iter.next()
            when (element.operator) {
                is Expression.Pow -> {
                    val combined = element.operator
                    combined.LHS = getPrevious(index)
                    combined.RHS = element.element
                    if (index == 0) this.start = combined
                    else this.elements[index - 1].element = combined
                    iter.remove()
                }
            }
        }
        
        val iter2 = this.elements.listIterator()
        while (iter2.hasNext()) {
            val index = iter2.nextIndex()
            val element = iter2.next()
            when (element.operator) {
                is Expression.Mul, is Expression.Div -> {
                    val combined = element.operator
                    combined.LHS = getPrevious(index)
                    combined.RHS = element.element
                    if (index == 0) this.start = combined
                    else this.elements[index - 1].element = combined
                    iter2.remove()
                }
            }
        }

        val iter3 = this.elements.listIterator()
        while (iter3.hasNext()) {
            val index = iter3.nextIndex()
            val element = iter3.next()
            when (element.operator) {
                is Expression.Add, is Expression.Sub -> {
                    val combined = element.operator
                    combined.LHS = getPrevious(index)
                    combined.RHS = element.element
                    if (index == 0) this.start = combined
                    else this.elements[index - 1].element = combined
                    iter3.remove()
                }
            }
        }

        val iter4 = this.elements.listIterator()
        while (iter4.hasNext()) {
            val index = iter4.nextIndex()
            val element = iter4.next()
            when (element.operator) {
                is Expression.And, is Expression.Or, is Expression.Eq, is Expression.Gtr, is Expression.Lss -> {
                    val combined = element.operator
                    combined.LHS = getPrevious(index)
                    combined.RHS = element.element
                    if (index == 0) this.start = combined
                    else this.elements[index - 1].element = combined
                    iter4.remove()
                }
            }
        }

        return this.start
    }

    fun getPrevious(index: Int): Expression {
        if (index == 0) return this.start
        return this.elements[index-1].element
    }
}

class ExpressionTreeBuilder(var start: Expression) {
    val elements = ArrayList<ExpressionTreePart>()
    fun add(element: Expression) {
        if (element is Expression.BinaryOperator && element.LHS is Expression.Empty && element.RHS is Expression.Empty) this.elements.add(
            ExpressionTreePart(element, Expression.Empty())
        )
        else {
            if (this.elements.isEmpty()) {
                if (this.start is Expression.Empty) this.start = element
                else if (this.start is Expression.UnaryOperator) (this.start as Expression.UnaryOperator).expression = element
                else this.start = Expression.Mul(this.start, element)
            } else {
                val last = this.elements.last()
                if (last.element is Expression.Empty) last.element = element
                else if (last.element is Expression.UnaryOperator) (last.element as Expression.UnaryOperator).expression = element
                else last.element = Expression.Mul(last.element, element)
            }
        }
    }
    fun end() = ExpressionTree(this.start, this.elements)
}

fun buildExpressionTree(input: ArrayList<Token>, context: ExpressionContext): ExpressionTree {
    /*input.forEach {
        println(it)
    }
    println()*/

    var builder: ExpressionTreeBuilder? = null
    val iter = input.listIterator()
    while (iter.hasNext()) {
        if (builder == null) builder = ExpressionTreeBuilder(getNextExpression(iter, context))
        else builder.add(getNextExpression(iter, context))
    }
    if (builder == null) throw InvalidExpressionError("celestialexpressions.Expression can not be empty")
    return builder.end()
}


class InvalidExpressionError(message: String) : Exception(message)
class AssemblyError(s: String) : Exception(s)
