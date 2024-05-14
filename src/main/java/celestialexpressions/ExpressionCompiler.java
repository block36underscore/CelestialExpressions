package celestialexpressions;

import java.util.ArrayList;
import java.util.ListIterator;

import static celestialexpressions.ExpressionTokenizer.*;

public class ExpressionCompiler {
    public static Expression compile(String source, ExpressionContext context) {
        return assembleExpression(
                validateExpression(
                        identifyTokens(
                                splitTokens(
                                        removeIrrelevantChars(
                                                source)))), context);
    }

    public static Expression compile(String source) {
        return compile(source, new ExpressionContext());
    }

    public static Expression assembleExpression(ArrayList<Token> input, ExpressionContext context) {
        return (Expression) buildExpressionTree(input, context).getExpression();
    }

    public static ArrayList<Token> validateExpression(ArrayList<Token> input) {
        int grouping = 0;
        for (Token token : input) {
            if (token.type == Token.Type.GROUPING_START) grouping++;
            else if (token.type == Token.Type.GROUPING_END) grouping--;
        }

        if (grouping != 0) throw new InvalidExpressionException("Grouping symbols are not balanced.");

        return input;
    }

    public static class InvalidExpressionException extends RuntimeException {
        public InvalidExpressionException(String message) {
            super(message);
        }
    }

    public interface ExpressionTreeElement {}

    public static class ExpressionTree implements ExpressionTreeElement {
        public IExpression<?> start;
        public ArrayList<ExpressionTreePart> elements;

        public ExpressionTree(IExpression<?> start, ArrayList<ExpressionTreePart> elements) {
            this.start = start;
            this.elements = elements;
        }

        public IExpression<?> getExpression() {
            if (elements.isEmpty()) return this.start;

            ListIterator<ExpressionTreePart> iter = this.elements.listIterator();
            while (iter.hasNext()) {
                int index = iter.nextIndex();
                ExpressionTreePart element = iter.next();
                if (element.operator instanceof Expression.Pow) {
                    Expression.BinaryOperator combined = element.operator;
                    combined.LHS = (Expression) getPrevious(index);
                    combined.RHS = element.element;
                    if (index == 0) this.start = combined;
                    else this.elements.get(index - 1).element = combined;
                    iter.remove();
                }
            }

            ListIterator<ExpressionTreePart> iter2 = this.elements.listIterator();
            while (iter2.hasNext()) {
                int index = iter2.nextIndex();
                ExpressionTreePart element = iter2.next();
                if (element.operator instanceof Expression.Mul || element.operator instanceof Expression.Div) {
                    Expression.BinaryOperator combined = element.operator;
                    combined.LHS = (Expression) getPrevious(index);
                    combined.RHS = element.element;
                    if (index == 0) this.start = combined;
                    else this.elements.get(index - 1).element = combined;
                    iter2.remove();
                }
            }

            ListIterator<ExpressionTreePart> iter3 = this.elements.listIterator();
            while (iter3.hasNext()) {
                int index = iter3.nextIndex();
                ExpressionTreePart element = iter3.next();
                if (element.operator instanceof Expression.Add || element.operator instanceof Expression.Sub) {
                    Expression.BinaryOperator combined = element.operator;
                    combined.LHS = (Expression) getPrevious(index);
                    combined.RHS = element.element;
                    if (index == 0) this.start = combined;
                    else this.elements.get(index - 1).element = combined;
                    iter3.remove();
                }
            }

            ListIterator<ExpressionTreePart> iter4 = this.elements.listIterator();
            while (iter4.hasNext()) {
                int index = iter4.nextIndex();
                ExpressionTreePart element = iter4.next();
                if (element.operator instanceof Expression.And ||
                    element.operator instanceof Expression.Or  ||
                    element.operator instanceof Expression.Eq  ||
                    element.operator instanceof Expression.Gtr ||
                    element.operator instanceof Expression.Lss) {
                    Expression.BinaryOperator combined = element.operator;
                    combined.LHS = (Expression) getPrevious(index);
                    combined.RHS = element.element;
                    if (index == 0) this.start = combined;
                    else this.elements.get(index - 1).element = combined;
                    iter4.remove();
                }
            }

            return this.start;
        }

        IExpression<?> getPrevious(int index) {
            if (index == 0) return this.start;
            return this.elements.get(index - 1).element;
        }
    }

    public static class ExpressionTreePart implements ExpressionTreeElement {
        public final Expression.BinaryOperator operator;
        public Expression element;

        public ExpressionTreePart(Expression.BinaryOperator operator, Expression element) {
            this.operator = operator;
            this.element = element;
        }
    }

    public static class ExpressionTreeBuilder {
        IExpression<?> start;
        ArrayList<ExpressionTreePart> elements;

        public ExpressionTreeBuilder(IExpression<?> start) {
            this.start = start;
            this.elements = new ArrayList<>();
        }

        public void add(IExpression<?> element) {
            if (element instanceof Expression.BinaryOperator && ((Expression.BinaryOperator) element).LHS instanceof Expression.Empty && ((Expression.BinaryOperator) element).RHS instanceof Expression.Empty) this.elements.add(
                    new ExpressionTreePart((Expression.BinaryOperator) element, new Expression.Empty())
            );
            else {
                if (this.elements.isEmpty()) {
                    if (this.start instanceof Expression.Empty) {
                        this.start = element;
                    } else if (this.start instanceof Expression.UnaryOperator) {
                        ((Expression.UnaryOperator) this.start).expression = (Expression) element;
                    } else {
                        this.start = new Expression.Mul((Expression) this.start, (Expression) element);
                    }
                } else {
                    ExpressionTreePart last = this.elements.get(this.elements.size() - 1);
                    if (last.element instanceof Expression.Empty) {
                        last.element = (Expression) element;
                    } else if (last.element instanceof Expression.UnaryOperator) {
                        ((Expression.UnaryOperator) last.element).expression = (Expression) element;
                    } else {
                        last.element = new Expression.Mul(last.element, (Expression) element);
                    }
                }
            }
        }

        public ExpressionTree end() {
            return new ExpressionTree(this.start, this.elements);
        }
    }

    public static ExpressionTree buildExpressionTree(ArrayList<Token> input, ExpressionContext context) {
        ExpressionTreeBuilder builder = null;
        ListIterator<Token> iter = input.listIterator();
        while (iter.hasNext()) {
            if (builder == null) builder = new ExpressionTreeBuilder(getNextExpression(iter, context));
            else {
                IExpression<?> nextExpression = getNextExpression(iter, context);
                builder.add(nextExpression);
            }
        }
        if (builder == null) throw new InvalidExpressionException("Expression must have a value.");
        return builder.end();
    }

    public static IExpression<?> getNextExpression(ListIterator<Token> tokens, ExpressionContext context) {
        Token token = tokens.next();
        while (token.type == Token.Type.SPLITTER) token = tokens.next();
        if (token.type == Token.Type.GROUPING_START && token.text.equals("(")) {
            ArrayList<Token> subTokenArray = new ArrayList<>();
            int depth = 1;
            while (true) {
                Token toAdd = tokens.next();
                if (toAdd.type == Token.Type.GROUPING_START) depth++;
                else if (toAdd.type == Token.Type.GROUPING_END) depth--;
                if (depth > 0) subTokenArray.add(toAdd);
                else break;
            }
            return buildExpressionTree(subTokenArray, context).getExpression();
        } else if (token.type == Token.Type.GROUPING_START) {
            return processFunction(token, tokens, context);
        } else if (token.type != Token.Type.GROUPING_END) {
            return token.getExpression(context);
        }
        else throw new InvalidExpressionException("Attempted to get expression from end grouping symbol. This is likely a bug in the compiler.");
    }

    public static Expression processFunction(Token token, ListIterator<Token> tokens, ExpressionContext context) {
        String name = token.text;
        ArrayList<ArrayList<Token>> params = new ArrayList<>();
        int depth = 1;
        int argCount = 1;

        scan:
        while (true) {
            ArrayList<Token> subTokenArray = new ArrayList<>();
            while (true) {
                Token toAdd = tokens.next();
                if (toAdd.type == Token.Type.SPLITTER && depth == 1) {
                    argCount++;
                    params.add(subTokenArray);
                    break;
                }
                else if (toAdd.type == Token.Type.GROUPING_START) depth++;
                else if (toAdd.type == Token.Type.GROUPING_END) {
                    depth--;
                    if (depth == 0) {
                        params.add(subTokenArray);
                        break scan;
                    }
                }
                subTokenArray.add(toAdd);
            }
        }

        for (Module mod : context.getModules()) {
            for (Function.Signature func : mod.getFunctions().functions.keySet()) {
                System.out.println(func.name + ":" + func.args);
            }
        }

        System.out.println(context.hasFunction("sin", 1));

        Function function = context.getFunction(name, argCount);
        ArrayList<IExpression<?>> expressions = new ArrayList<>();
        for (ArrayList<Token> it : params) {
            expressions.add(buildExpressionTree(it, context).getExpression());
        }
        if (function.size != params.size() && function.size >= 0.0) {
            StringBuilder message = new StringBuilder();
            message.append("Function \"");
            message.append(name);
            message.append("\" takes ");
            message.append(function.size);
            if (function.size == 1) message.append(" parameter");
            else message.append(" parameters");
            message.append(", but ");
            message.append(params.size());
            message.append(" were provided.");
            throw new InvalidExpressionException(message.toString());
        }
        return new Expression.Fun(function, expressions);
    }
}
