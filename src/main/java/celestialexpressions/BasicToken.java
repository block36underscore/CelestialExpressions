package celestialexpressions;


public class BasicToken {
    public String text;
    public BasicToken.Type type;

    public BasicToken(String text, BasicToken.Type type) {
        super();
        this.text = text;
        this.type = type;
    }

    public String toString() {
        return this.text + ": " + this.type;
    }

    public enum Type {
        CONST, VARIABLE, OPERATOR, STRING_LITERAL
    }
}