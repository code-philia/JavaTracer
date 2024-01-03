package org.cophi.javatracer.utils;

/**
 * @param <T>
 * @author LLT
 */
@FunctionalInterface
public interface Predicate<T> {

    boolean apply(T val);
}
