// Module.java
package celestialexpressions;


public class Module {
    private final String name;
    private final VariableList variables;
    private final FunctionList functions;

    public Module(String name, VariableList variables, FunctionList functions) {
        super();
        this.name = name;
        this.variables = variables;
        this.functions = functions;
    }

    public final String getName() {
        return this.name;
    }

    protected final VariableList getVariables() {
        return this.variables;
    }

    protected final FunctionList getFunctions() {
        return this.functions;
    }

    public Expression getVariable(String name) {
        String[] split
                = name.split(":");
        return this.variables.getVariable(split[split.length - 1]);
    }

    public boolean hasVariable(String name) {
        String[] split = name.split(":");
        if (split.length > 2) {
            throw new VariableList.NoSuchVariableException("Illegal variable name \"" + name + "\", cannot have more than one colon.");
        } else {
            if (split.length == 2 && split[0].equals(this.name)) {
                return false;
            } else {
                return this.variables.hasVariable(name);
            }
        }
    }

    public Function getFunction(String name, int argCount) {
        String[] split = name.split(":");
        Function fun = this.functions.getFunction(split[split.length-1], argCount);
        if (fun != null) {
            return fun;
        } else {
            throw new FunctionList.NoSuchFunctionException("Function \"" + name + "\" is not declared.");
        }
    }

    public boolean hasFunction(String name, int argCount) {
        String[] split = name.split(":");
        if (split.length > 2) {
            throw new FunctionList.NoSuchFunctionException("Illegal function name \"" + name + "\", cannot have more than one colon.");
        } else {
            if (split.length == 2 && split[0].equals(this.name)) {
                return false;
            } else {
                return this.functions.hasFunction(name, argCount);
            }
        }
    }
}