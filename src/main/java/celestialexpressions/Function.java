package celestialexpressions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Function {
    public final FunctionExecutor supplier;
    public final int size;

    public Function(FunctionExecutor supplier, int size) {
        this.supplier = supplier;
        this.size = size;
    }

    public final FunctionExecutor getSupplier() {
        return this.supplier;
    }

    public final int getSize() {
        return this.size;
    }

    public final double invoke(List<IExpression<?>> params) {
        return this.supplier.invoke(params.stream().map(IExpression::invoke).collect(Collectors.toList()));
    }

    @FunctionalInterface
    public interface FunctionExecutor {
        Double invoke(List<Object> input);
    }

    public static class Signature {
        public final String name;
        public final int args;

        public Signature(String name, int args) {
            this.name = name;
            this.args = args;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof Signature) {
                return Objects.equals(this.name, ((Signature) other).name) &&
                       this.args == ((Signature) other).args;
            } else return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode() + this.args;
        }
    }
}
