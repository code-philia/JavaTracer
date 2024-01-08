package org.cophi.javatracer.instrumentation.filters;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class WildcardMatcher {

    protected final Pattern pattern;

    public WildcardMatcher(final String expression) {
        this.pattern = this.initPatternMatcher(expression);
    }

    public WildcardMatcher(final List<String> expressions) {
        this.pattern = this.initPatternMatcher(expressions);
    }

    private static CharSequence toRegex(final String expression) {
        final StringBuilder regex = new StringBuilder(expression.length() * 2);
        String suffix = null;
        int endIdx = -1;
        // java.util.*
        if ((endIdx = WildcardMatcher.endsWith(expression, ".*")) > 0) {
            suffix = ".*"; // any character
        }
        // java.util.*\
        else if ((endIdx = WildcardMatcher.endsWith(expression, ".*\\")) > 0) {
            suffix = "[^\\.]*";
        }
        // java.util.ArrayList*
        else if ((endIdx = WildcardMatcher.endsWith(expression, "*")) > 0) {
            suffix = "(\\$.*)*";
            endIdx--;
        }
        // java.util.ArrayList
        else {
            endIdx = expression.length() - 1;
        }

        char[] charArray = expression.toCharArray();
        for (int i = 0; i <= endIdx; i++) {
            char c = charArray[i];
            regex.append(Pattern.quote(String.valueOf(c)));
        }
        if (suffix != null) {
            regex.append(suffix);
        }
        return regex;
    }

    private static int endsWith(String value, String suffix) {
        if (value.endsWith(suffix)) {
            return value.length() - suffix.length();
        }
        return -1;
    }

    public boolean matches(final String s) {
        return this.pattern.matcher(s).matches();
    }

    protected Pattern initPatternMatcher(final String expression) {
        final String[] parts = expression.split(File.pathSeparator);
        return this.initPatternMatcher(Arrays.stream(parts).toList());
    }

    protected Pattern initPatternMatcher(final List<String> expressions) {
        final StringBuilder regex = new StringBuilder(expressions.size() * 2);
        boolean next = false;
        for (final String expression : expressions) {
            if (next) {
                regex.append('|');
            }
            regex.append('(').append(toRegex(expression)).append(')');
            next = true;
        }
        return Pattern.compile(regex.toString());
    }

}
