package org.cophi.javatracer.instrumentation.filters;

import java.util.HashSet;
import java.util.Set;
import org.cophi.javatracer.log.Log;

public class JDKFilter {

    protected static final Set<String> JDKExclusives;

    static {
        JDKExclusives = new HashSet<>();
        JDKExclusives.add(Integer.class.getName());
        JDKExclusives.add(Boolean.class.getName());
        JDKExclusives.add(Float.class.getName());
        JDKExclusives.add(Character.class.getName());
        JDKExclusives.add(Double.class.getName());
        JDKExclusives.add(Long.class.getName());
        JDKExclusives.add(Short.class.getName());
        JDKExclusives.add(Byte.class.getName());
        JDKExclusives.add(Object.class.getName());
        JDKExclusives.add(String.class.getName());
        JDKExclusives.add("java.lang.Thread");
        JDKExclusives.add("java.lang.ThreadLocal");
        JDKExclusives.add("java.lang.Error");
        JDKExclusives.add("java.lang.AssertionError");
        JDKExclusives.add("java.lang.Class");
    }

    private JDKFilter() {
        throw new IllegalStateException(Log.genMessage("Utility class", this.getClass()));
    }

    public static boolean contains(final String className) {
        return JDKFilter.JDKExclusives.contains(className);
    }

}
