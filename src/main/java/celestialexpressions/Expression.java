package celestialexpressions;

import java.util.ArrayList;
import java.util.Objects;


public interface Expression extends IExpression<Double> {

    class Add extends BinaryOperator {
        public Add(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public Add() {
            super();
        }


        public Double invoke() {
            return (this.getLHS().invoke()) + (this.getRHS().invoke());
        }
    }

    class And extends PseudoBoolean {
        public And(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public boolean operation() {
            return (this.getLHS().invoke()) == 1.0 && (this.getRHS().invoke()) == 1.0;
        }

        public And() {
            super();
        }
    }

    abstract class BinaryOperator implements Expression {
        
        public Expression LHS;
        
        public Expression RHS;

        public BinaryOperator(Expression LHS, Expression RHS) {
            super();
            this.LHS = LHS;
            this.RHS = RHS;
        }

        public BinaryOperator() {
            this(new Empty(), new Empty());
        }
        
        public final Expression getLHS() {
            return this.LHS;
        }

        
        public final Expression getRHS() {
            return this.RHS;
        }
        
        public String toString() {
            return "" + '(' + this.LHS + ", " + this.RHS + ')';
        }
    }
    
    class Const implements Expression {
        final double constant;

        public Const(double constant) {
            this.constant = constant;
        }

        
        public Double invoke() {
            return this.constant;
        }

        
        public String toString() {
            return String.valueOf(this.constant);
        }
    }

    class Div extends BinaryOperator {
        public Div(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        
        public Double invoke() {
            return this.getLHS().invoke() / this.getRHS().invoke();
        }

        public Div() {
            super();
        }
    }

    class Empty implements Expression {
        
        public Double invoke() {
            throw new ExecutionError("Expression was executed before it was fully processed.");
        }

        
        public String toString() {
            return "Empty";
        }

        static class ExecutionError extends RuntimeException {
            public ExecutionError(String message) {
                super(message);
            }
        }
    }
    
    class Eq extends PseudoBoolean {
        public Eq(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public boolean operation() {
            return Objects.equals(this.getLHS().invoke(), this.getRHS().invoke());
        }

        public Eq() {
            super();
        }
    }

    class Fun implements Expression {
        
        private final Function function;
        
        private final ArrayList<IExpression<?>> params;

        public Fun(Function function, ArrayList<IExpression<?>> params) {
            super();
            this.function = function;
            this.params = params;
        }

        
        public Double invoke() {
            return this.function.invoke(this.params);
        }

    }

    class Gtr extends PseudoBoolean {
        public Gtr(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public boolean operation() {
            return (this.getLHS().invoke()) > (this.getRHS().invoke());
        }

        public Gtr() {
            super();
        }
    }

    class Lss extends PseudoBoolean {
        public Lss(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public boolean operation() {
            return (this.getLHS().invoke()) < (this.getRHS().invoke());
        }

        public Lss() {
            super();
        }
    }

    class Mul extends BinaryOperator {
        public Mul(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        
        public Double invoke() {
            return (this.getLHS().invoke()) * (this.getRHS().invoke());
        }

        public Mul() {
            super();
        }
    }

    class Negate extends UnaryOperator {
        public Negate(Expression expression) {
            super(expression);
        }

        
        public String toString() {
            return "" + '-' + this.getExpression();
        }

        
        public Double invoke() {
            return -(this.getExpression().invoke());
        }

        public Negate() {
            this((new Empty()));
        }
    }

    class Or extends PseudoBoolean {
        public Or(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public boolean operation() {
            return (this.getLHS().invoke()) == 1.0 || (this.getRHS().invoke()) == 1.0;
        }

        public Or() {
            super();
        }
    }

    class Pow extends BinaryOperator {
        public Pow(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        
        public Double invoke() {
            return Math.pow((this.getLHS().invoke()), (this.getRHS().invoke()));
        }

        public Pow() {
            super();
        }
    }

    abstract class PseudoBoolean extends BinaryOperator {
        public PseudoBoolean(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        public PseudoBoolean() {
            this(new Empty(), new Empty());
        }


        public Double invoke() {
            return this.operation() ? 1.0 : 0.0;
        }

        public abstract boolean operation();
    }

    class Sub extends BinaryOperator {
        public Sub(Expression lhs, Expression rhs) {
            super(lhs, rhs);
        }

        
        public Double invoke() {
            return (this.getLHS().invoke()) - (this.getRHS().invoke());
        }

        public Sub() {
            super();
        }
    }

    abstract class UnaryOperator implements Expression {
        
        public Expression expression;

        protected UnaryOperator(Expression expression) {
            super();
            this.expression = expression;
        }

        
        public final Expression getExpression() {
            return this.expression;
        }
    }

    class Var implements Expression {
        
        private final String id;
        
        public final Expression supplier;

        public Var(String id, ExpressionContext context) {
            super();
            this.id = id;
            this.supplier = context.getVariable(this.id);
        }

        
        public final String getId() {
            return this.id;
        }

        
        public final Expression getSupplier() {
            return this.supplier;
        }

        
        public Double invoke() {
            return this.supplier.invoke();
        }

        
        public String toString() {
            return this.id;
        }
    }

    interface SExpression extends IExpression<String> {
        class Const implements SExpression {
            public final String str;

            public Const(String str) {
                this.str = str;
            }

            @Override
            public String invoke() {
                return this.str;
            }

            @Override
            public String toString() {
                return "String(" + this.str + ")";
            }
        }
    }
}
