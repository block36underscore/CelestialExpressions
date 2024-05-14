package celestialexpressions;

@FunctionalInterface
public interface IExpression<T> {
    T invoke();
}
