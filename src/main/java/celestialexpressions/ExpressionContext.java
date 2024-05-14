package celestialexpressions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;



public final class ExpressionContext {
    private final List<Module> modules;

    public final static Module STANDARD_MODULE = new Module(
            "std",
            new VariableList()
                    .with("PI", ()->Math.PI)
                    .with("pi", ()->Math.PI)
                    .with("e", ()->Math.E)
                    .with("E", ()->Math.E)
                    .with("maxInteger", ()-> (double) Integer.MAX_VALUE)
                    .with("minInteger", ()-> (double) Integer.MIN_VALUE)
                    .with("maxDouble", ()->Double.MAX_VALUE)
                    .with("minDouble", ()->Double.MIN_VALUE)
                    .with("localDayOfYear", ()-> (double) LocalDate.now().getDayOfYear())
                    .with("localDayOfMonth", ()-> (double) LocalDate.now().getDayOfMonth())
                    .with("localDayOfWeek", ()-> (double) LocalDate.now().getDayOfWeek().getValue())
                    .with("localMonth", ()-> (double) LocalDate.now().getMonth().getValue())
                    .with("localYear", ()-> (double) LocalDate.now().getYear())
                    .with("localSecondOfHour", ()-> (double) LocalDateTime.now().getSecond())
                    .with("localMinuteOfHour", ()-> (double) LocalDateTime.now().getMinute())
                    .with("localSecondOfDay", ()-> (double) (((LocalDate.now().atTime(LocalTime.now()).getHour() * 60) + LocalDate.now().atTime(LocalTime.now()).getMinute() * 60) + LocalDate.now().atTime(LocalTime.now()).getSecond()))
                    .with("localMinuteOfDay", ()-> (double) ((LocalDate.now().atTime(LocalTime.now()).getHour() * 60) + LocalDate.now().atTime(LocalTime.now()).getMinute()))
                    .with("localHour", ()-> (double) LocalTime.now().getHour())
                    .with("epochMilli", ()-> (double) Instant.now().toEpochMilli())
                    .with("random", Math::random),
            new FunctionList()
                    .with("min",            new Function((List<Object> arr) -> arr.stream().map(it->(double) it).reduce(Math::min).orElse(0.0), -1))
                    .with("max",            new Function((List<Object> arr) -> arr.stream().map(it->(double) it).reduce(Math::max).orElse(0.0), -1))
                    .with("sin",            new Function((List<Object> arr) -> Math.sin(Math.toRadians((double) arr.get(0))), 1))
                    .with("sinr",           new Function((List<Object> arr) -> Math.sin((double) arr.get(0)), 1))
                    .with("cos",            new Function((List<Object> arr) -> Math.cos(Math.toRadians((double) arr.get(0))), 1))
                    .with("cosr",           new Function((List<Object> arr) -> Math.cos((double) arr.get(0)), 1))
                    .with("tan",            new Function((List<Object> arr) -> Math.tan(Math.toRadians((double) arr.get(0))), 1))
                    .with("tanr",           new Function((List<Object> arr) -> Math.tan((double) arr.get(0)), 1))
                    .with("asin",           new Function((List<Object> arr) -> Math.asin(Math.toRadians((double) arr.get(0))), 1))
                    .with("asinr",          new Function((List<Object> arr) -> Math.asin((double) arr.get(0)), 1))
                    .with("acos",           new Function((List<Object> arr) -> Math.acos(Math.toRadians((double) arr.get(0))), 1))
                    .with("acosr",          new Function((List<Object> arr) -> Math.acos((double) arr.get(0)), 1))
                    .with("atan",           new Function((List<Object> arr) -> Math.atan(Math.toRadians((double) arr.get(0))), 1))
                    .with("atanr",          new Function((List<Object> arr) -> Math.atan((double) arr.get(0)), 1))
                    .with("radians",        new Function((List<Object> arr) -> Math.toRadians((double) arr.get(0)), 1))
                    .with("deg",            new Function((List<Object> arr) -> Math.toDegrees((double) arr.get(0)), 1))
                    .with("floor",          new Function((List<Object> arr) -> Math.floor((double) arr.get(0)), 1))
                    .with("ceil",           new Function((List<Object> arr) -> Math.ceil((double) arr.get(0)), 1))
                    .with("round",          new Function((List<Object> arr) -> (double) Math.round((double) arr.get(0)), 1))
                    .with("abs",            new Function((List<Object> arr) -> Math.abs((double) arr.get(0)), 1))
                    .with("sqrt",           new Function((List<Object> arr) -> Math.sqrt((double) arr.get(0)), 1))
                    .with("consoleLog",     new Function((List<Object> arr) -> {System.out.println(arr.get(0)); return 0.0;}, 1))
                    .with("ifElse",         new Function((List<Object> arr) -> ((double) arr.get(0) != 0.0) ? (double) arr.get(1) : (double) arr.get(2), 3))
    );

    public static List<Module> DEFAULT_MODULES = Collections.singletonList(STANDARD_MODULE);
            
    public ExpressionContext(ArrayList<Module> modules) {
        this.modules = modules;
        this.modules.add(0, ExpressionContext.STANDARD_MODULE);
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public boolean addModule(Module module) {
        return this.modules.add(module);
    }

    public boolean hasVariable(String name) {
        Iterator<Module> modules = this.modules.iterator();

        Module module;
        do {
            if (!modules.hasNext()) {
                return false;
            }

            module = modules.next();
        } while(!module.hasVariable(name));

        return true;
    }

    public Expression getVariable(String name) {
        this.scanVariableConflicts(name);
        Iterator<Module> modules = this.modules.iterator();

        Module module;
        do {
            if (!modules.hasNext()) {
                throw new VariableList.NoSuchVariableException("No variable named \"" + name + "\" is declared.");
            }

            module = modules.next();
        } while(!module.hasVariable(name));

        return module.getVariable(name);
    }

    public void scanVariableConflicts(String name) {
        ArrayList<String> found = new ArrayList<>();

        for (Module module : this.modules) {
            if (module.hasVariable(name)) {
                found.add(module.getName());
            }
        }

        if (found.size() > 1) {
            throw new ConflictException(name, found, "Variable");
        }
    }

    public boolean hasFunction(String name, int argCount) {
        Iterator<Module> modules = this.modules.iterator();

        Module module;
        do {
            if (!modules.hasNext()) {
                return false;
            }

            module = modules.next();
        } while(!module.hasFunction(name, argCount));

        return true;
    }

    public Function getFunction(String name, int argCount) {
        this.scanFunctionConflicts(name, argCount);

        for (Module module : this.modules) {
            if (module.hasFunction(name, argCount)) return module.getFunction(name, argCount);
            if (module.hasFunction(name, -1)) return module.getFunction(name, -1);
        }

        throw new FunctionList.NoSuchFunctionException("No function named \"" + name + "\" with " + argCount + " argument" + (argCount > 1 ? "s" : "") + " is declared.");
    }

    public void scanFunctionConflicts(String name, int argCount) {
        ArrayList<String> found = new ArrayList<>();

        for (Module module : this.modules) {
            if (module.hasFunction(name, argCount)) {
                found.add(module.getName());
            }
        }

        if (found.size() > 1) {
            throw new ConflictException(name, found, "Function");
        }
    }

    public String toString() {
        return "ExpressionContext(modules=" + this.modules + ')';
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof ExpressionContext)) {
            return false;
        } else {
            ExpressionContext otherContext = (ExpressionContext) other;
            return this.modules.equals(otherContext.modules);
        }
    }

    public ExpressionContext() {
        this(new ArrayList<>());
    }

    public static final class ConflictException extends RuntimeException {
        public ConflictException(String variable, ArrayList<String> modules, String type) {
            super(generateMessage(variable, modules, type));
        }

        private static String generateMessage(String variable, ArrayList<String> modules, String type) {
            StringBuilder builder = new StringBuilder();
            builder.append(type);
            builder.append(" \"");
            builder.append(variable);
            builder.append("\" was found in multiple modules: ");
            for (String mod : modules) {
                builder.append(mod);
                builder.append(", ");
            }

            return builder.toString();
        }
    }
}