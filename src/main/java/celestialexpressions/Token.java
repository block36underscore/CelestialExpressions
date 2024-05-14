package celestialexpressions;


public class Token {
    public String text;
    public Token.Type type;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    IExpression<?> getExpression(ExpressionContext context) {
        switch (this.type) {
            case CONST: return new Expression.Const(Double.parseDouble(this.text));
            case VARIABLE:
            case NULLARY:
                return new Expression.Var(this.text, context); 
            case UNARY: return new Expression.Negate();
            case BINARY:
                switch (this.text) {
                    case "+": return new Expression.Add();
                    case "-": return new Expression.Sub();
                    case "*": return new Expression.Mul();
                    case "/": return new Expression.Div();
                    case "^": return new Expression.Pow();
                    case "&": return new Expression.And();
                    case "|": return new Expression.Or();
                    case "=": return new Expression.Eq();
                    case ">": return new Expression.Gtr();
                    case "<": return new Expression.Lss();
                    default: throw new ParsingException("Invalid character was not caught. Please report this exception.");
                }
            case SPLITTER:
            case GROUPING_START:
            case GROUPING_END:
                return null;
            case STRING_LITERAL:
                return new Expression.SExpression.Const(this.text);
            default: throw new ParsingException("This exception should be unreachable. Please report this is it occurs");
        }
    }

    public enum Type {
        CONST, VARIABLE, NULLARY, UNARY, BINARY, GROUPING_START, GROUPING_END, SPLITTER, STRING_LITERAL
    }

    public static class ParsingException extends RuntimeException {
        public ParsingException(String message) {
            super(message);
        }
    }

    @Override
    public String toString() {
        return this.text + ": " + this.type.toString();
    }
}
