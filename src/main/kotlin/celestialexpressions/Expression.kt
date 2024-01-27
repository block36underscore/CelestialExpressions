@file:JvmName("celestialexpressions.Expression")

package celestialexpressions

import kotlin.math.pow

fun interface IExpression<T>: ()->T

fun interface Expression: IExpression<Double> {

    class Const(val constant: Double) : Expression {
        override fun invoke() = constant
        override fun toString() = this.constant.toString()
    }

    open class Var(val id: String, context: ExpressionContext) : Expression {
        val supplier = context.getVariable(id)
        override fun invoke() = supplier()
        override fun toString() = this.id
    }

    class Empty: Expression {
        override fun invoke() = throw ExecutionError("Expression was executed before it was fully processed.")
        override fun toString() = "Empty"
    }

    abstract class UnaryOperator protected constructor(var expression: Expression = Empty()) : Expression

    class Negate(expression: Expression) : UnaryOperator(expression) {
        override fun toString() = "-${this.expression}"
        override fun invoke() = -expression()
        constructor(): this(Empty())
    }

    abstract class BinaryOperator(var LHS: Expression = Empty(), var RHS: Expression = Empty()) : Expression {
        override fun toString() = "(${this.LHS}, ${this.RHS})"
    }

    class Add(lhs: Expression, rhs: Expression) : BinaryOperator(lhs, rhs) {
        override fun invoke() = LHS() + RHS()
        constructor(): this(Empty(), Empty())
    }

    class Sub(lhs: Expression, rhs: Expression) : BinaryOperator(lhs, rhs) {
        override fun invoke() = LHS() - RHS()
        constructor(): this(Empty(), Empty())
    }

    class Mul(lhs: Expression, rhs: Expression) : BinaryOperator(lhs, rhs) {
        override fun invoke() = LHS() * RHS()
        constructor(): this(Empty(), Empty())
    }

    class Div(lhs: Expression, rhs: Expression) : BinaryOperator(lhs, rhs) {
        override fun invoke() = LHS() / RHS()
        constructor(): this(Empty(), Empty())
    }

    class Pow(lhs: Expression, rhs: Expression) : BinaryOperator(lhs, rhs) {
        override fun invoke() = LHS().pow(RHS())
        constructor(): this(Empty(), Empty())
    }

    abstract class PseudoBoolean(lhs: Expression, rhs: Expression): BinaryOperator(lhs, rhs) {
        override fun invoke() = if (operation()) 1.0 else 0.0
        abstract fun operation(): Boolean
    }

    class And(lhs: Expression, rhs: Expression) : PseudoBoolean(lhs, rhs) {
        override fun operation() = LHS() == 1.0 && RHS() == 1.0
        constructor(): this(Empty(), Empty())
    }

    class Or(lhs: Expression, rhs: Expression) : PseudoBoolean(lhs, rhs) {
        override fun operation() = LHS() == 1.0 || RHS() == 1.0
        constructor(): this(Empty(), Empty())
    }

    class Eq(lhs: Expression, rhs: Expression) : PseudoBoolean(lhs, rhs) {
        override fun operation() = LHS() == RHS()
        constructor(): this(Empty(), Empty())
    }

    class Gtr(lhs: Expression, rhs: Expression) : PseudoBoolean(lhs, rhs) {
        override fun operation() = LHS() > RHS()
        constructor(): this(Empty(), Empty())
    }

    class Lss(lhs: Expression, rhs: Expression) : PseudoBoolean(lhs, rhs) {
        override fun operation() = LHS() < RHS()
        constructor(): this(Empty(), Empty())
    }

    class Fun(val function: Function, val params: ArrayList<IExpression<Any>>) : Expression {
        override fun invoke() = function(params)
    }

}

fun interface SExpression: IExpression<String> {
    class Const(val constant: String) : SExpression {
        override fun invoke() = constant
    }
}

class ExecutionError(message: String): Exception(message)