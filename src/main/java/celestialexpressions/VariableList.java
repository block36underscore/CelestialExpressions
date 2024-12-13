package celestialexpressions;

import java.util.HashMap;
import java.util.Map;


public class VariableList {
    private final Map<String, Expression> variables;

    public VariableList(HashMap<String, Expression> variables) {
        this.variables = variables;
    }

    public final Map<String, Expression> getVariables() {
        return this.variables;
    }

    public void registerVariable(String name, Expression supplier) {
        if (this.variables.containsKey(name)) {
            throw new IllegalArgumentException("Variable \"" + name + "\" is already registered.");
        } else {
            this.variables.put(name, supplier);
        }
    }

    public VariableList with(String name, Expression supplier) {
        this.registerVariable(name, supplier);
        return this;
    }

    public Expression getVariable(String name) {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        } else {
            throw new NoSuchVariableException("Variable \"" + name + "\" is not declared.");
        }
    }

    public boolean hasVariable(String name) {
        return this.variables.containsKey(name);
    }

    public VariableList() {
        this.variables = new HashMap<>();
    }

    public static class NoSuchVariableException extends RuntimeException {
        public NoSuchVariableException(String message) {
            super(message);
        }
    }
}
