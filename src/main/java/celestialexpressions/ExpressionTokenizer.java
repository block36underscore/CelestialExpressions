package celestialexpressions;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.regex.Pattern;

public class ExpressionTokenizer {
    public static String removeIrrelevantChars(String input) {
        StringBuilder out = new StringBuilder();

        boolean inStringD = false;
        boolean inStringS = false;

        for (char c : input.toCharArray()) {
            if (inStringD || inStringS) {
                switch (c) {
                    case '"':
                        inStringD = false;
                        break;
                    case '\'':
                        inStringS = false;
                        break;
                }
                out.append(c);
            } else switch (c) {
                case '"':
                    out.append(c);
                    inStringD = true;
                    break;
                case '\'':
                    out.append(c);
                    inStringS = true;
                    break;
                default: if (!Pattern.matches("[\\s#]", String.valueOf(c))) {
                    out.append(c);
                    break;
                }
            }
        }

        return out.toString();
    }

    public static ArrayList<BasicToken> splitTokens(String input) {
        ArrayList<BasicToken> out = new ArrayList<>();
        if (input.isEmpty()) return out;
        BasicToken.Type currentTokenType = null;
        int tokenStart = 0;
        int dots = 0;

        for (int i = 0; i < input.length(); i++) {
            String c = "" + input.charAt(i);

            // Ignore Whitespace and '#' symbol (for legacy reasons, may be changed in the future)
            if (c.equals("\"") || c.equals("'")) {
                if (currentTokenType != BasicToken.Type.STRING_LITERAL) {
                    addToken(out, input, tokenStart, i, currentTokenType);
                    currentTokenType = BasicToken.Type.STRING_LITERAL;
                    tokenStart = i+1;
                    continue;
                } else if (input.charAt(i-1) != '\\') {
                    addToken(out, input, tokenStart, i, currentTokenType);
                    currentTokenType = null;
                    continue;
                }
            } else if (currentTokenType == BasicToken.Type.STRING_LITERAL) {
                continue;
            } else if (Pattern.matches("[\\s#]", c)) continue;
            if (currentTokenType == BasicToken.Type.STRING_LITERAL) {
                continue;
                // Detect splitters
            } else if (c.equals(",")) {
                addToken(out, input, tokenStart, i, currentTokenType);
                currentTokenType = BasicToken.Type.OPERATOR;
                tokenStart = i;
                // Detect numerical constant
            } else if (Pattern.matches("[\\d.]", c) && (currentTokenType == BasicToken.Type.OPERATOR || currentTokenType == null || currentTokenType == BasicToken.Type.CONST)) {
                if (currentTokenType != BasicToken.Type.CONST) {
                    dots = 0;
                    addToken(out, input, tokenStart, i, currentTokenType);
                    currentTokenType = BasicToken.Type.CONST;
                    tokenStart = i;
                }
                if (c.equals(".")) {
                    if (dots >= 1) throw new Token.ParsingException("Multiple periods in one decimal number at index $i.");
                    dots++;
                }
                // Detect operators and grouping symbols
            } else if (Pattern.matches("[+-/*^()&|=><]", c)) {
                addToken(out, input, tokenStart, i, currentTokenType);
                currentTokenType = BasicToken.Type.OPERATOR;
                tokenStart = i;
                // Detect variables
            } else if (currentTokenType != BasicToken.Type.VARIABLE) {
                boolean func = false;
                for (int j=i; j < input.length(); j++) {
                    char c2 = input.charAt(j);
                    if (c2 == '(') {
                        func = true;
                        break;
                    } else if (Pattern.matches("[\\d.+-/*^)&|=><#]", String.valueOf(c2))) break;
                }
                if (!func) {
                    addToken(out, input, tokenStart, i, currentTokenType);
                    currentTokenType = BasicToken.Type.VARIABLE;
                    tokenStart = i;
                } else if (currentTokenType != BasicToken.Type.OPERATOR || (tokenStart-i==-1 && Pattern.matches("[\\d.+-/*^()&|=><#]", input.substring(tokenStart, tokenStart+1)))) {
                    addToken(out, input, tokenStart, i, currentTokenType);
                    currentTokenType = BasicToken.Type.OPERATOR;
                    tokenStart = i;
                }
            }
        }
        addToken(out, input, tokenStart, input.length(), currentTokenType);

        return out;
    }

    public static void addToken(ArrayList<BasicToken> out, String input, int start, int end, BasicToken.Type type) {
        if (type == null) return;
        out.add(
                new BasicToken(
                        input.substring(start, end),
                        type
                )
        );
    }

    public static ArrayList<Token> identifyTokens(ArrayList<BasicToken> input) {
        ArrayList<Token> out = new ArrayList<>();
        ListIterator<BasicToken> iter = input.listIterator();

        while (iter.hasNext()) {
            int i = iter.nextIndex();
            BasicToken token = iter.next();

            Token.Type type = null;

            switch (token.type) {
                case CONST:
                    type = Token.Type.CONST;
                    break;
                case STRING_LITERAL:
                    type = Token.Type.STRING_LITERAL;
                    break;
                case VARIABLE:
                    type = Token.Type.VARIABLE;
                    break;
                case OPERATOR:
                    switch (token.text) {
                        case "+":
                        case "*":
                        case "/":
                        case "^":
                        case "&":
                        case "|":
                        case "=":
                        case ">":
                        case "<":
                            type = Token.Type.BINARY;
                            break;
                        case "-":
                            if (i==0) type = Token.Type.UNARY;
                            else
                                switch (out.get(out.size()-1).type) {
                                case BINARY:
                                case GROUPING_START:
                                case UNARY:
                                case SPLITTER:
                                    type = Token.Type.UNARY;
                                    break;
                                default: type = Token.Type.BINARY;
                            }
                            break;
                        case ")":
                            type = Token.Type.GROUPING_END;
                            break;
                        default:
                            switch (token.text.charAt(token.text.length() - 1)) {
                                case '(':
                                    type = Token.Type.GROUPING_START;
                                    break;
                                case ')':
                                    type = Token.Type.NULLARY;
                                    break;
                                case ',':
                                    type = Token.Type.SPLITTER;
                                    break;
                                default:
                                    if (iter.next().text.equals("(")) type = Token.Type.GROUPING_START;
                                    else throw new Token.ParsingException("Please report this exception.");
                        }
                        break;
                    }
            }

            out.add(new Token(type, token.text));
        }

        return out;
    }
}