package utils;

/**
 * @author LLT
 * @param <T>
 */
@FunctionalInterface
public interface Predicate<T> {
  boolean apply(T val);
}
