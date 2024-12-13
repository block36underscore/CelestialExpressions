package celestialexpressions;


public final class ModuleBuilder {
    private final String name;
    private final VariableList variables;
    private final FunctionList functions;

    public ModuleBuilder(String name) {
        super();
        this.name = name;
        this.variables = new VariableList();
        this.functions = new FunctionList();
    }

    public String getName() {
        return this.name;
    }

    public void addVariable(String name, Expression supplier) {
        assertNameIsValid(name);
        this.variables.registerVariable(name, supplier);
    }

    public void addFunction(String name, Function function) {
        assertNameIsValid(name);
        this.functions.registerFunction(name, function);
    }

    public Module build() {
        return new Module(this.name, this.variables, this.functions);
    }

    private static void assertNameIsValid(String name) {
        String[] split = name.split(":");
        if (split.length != 1) throw new ModuleRegistrationException("Name \"" + name + "\" is an invalid function or variable name. Names must not contain a semicolon");
    }

    public static class ModuleRegistrationException extends RuntimeException {
        public ModuleRegistrationException(String message) {
            super(message);
        }
    }
}