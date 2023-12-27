package utils;

/**
 * @param <T>
 * @author LLT
 */
@FunctionalInterface
public interface Predicate<T> {

    boolean apply(T val);
}
