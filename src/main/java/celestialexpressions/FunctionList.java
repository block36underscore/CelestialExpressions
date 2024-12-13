package celestialexpressions;

import java.util.HashMap;

public class FunctionList {
    public final HashMap<Function.Signature, Function> functions;

    public FunctionList(HashMap<Function.Signature, Function> functions) {
        this.functions = functions;
    }

    public final HashMap<Function.Signature, Function> getFunctions() {
        return this.functions;
    }

    public void registerFunction(String name, Function function) {
        if (this.functions.containsKey(new Function.Signature(name, function.getSize()))) {
            throw new IllegalArgumentException("Function \"" + name + "\" is already registered.");
        } else {
            this.functions.put(new Function.Signature(name, function.getSize()), function);
        }
    }

    public FunctionList with(String name, Function function) {
        this.registerFunction(name, function);
        return this;
    }

    public Function getFunction(String name, int argCount) {
        return this.functions.get(new Function.Signature(name, argCount));
    }

    public boolean hasFunction(String name, int argCount) {
        return this.functions.containsKey(new Function.Signature(name, argCount));
    }

    public FunctionList() {
        this(new HashMap<>());
    }

    public static class NoSuchFunctionException extends RuntimeException {
        public NoSuchFunctionException(String message) {
            super(message);
        }
    }
}