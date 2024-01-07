package org.cophi.javatracer.configs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.cophi.javatracer.log.Log;

public class JavaTracerAgentParameters {

    public static final String DELIMITER = "~";
    public static final String ASSIGN_SYMBOL = "=";
    protected Map<String, String> parameters = new HashMap<>();

    public JavaTracerAgentParameters() {

    }

    public JavaTracerAgentParameters(final Map<String, String> parameters) {
        this.parameters.putAll(parameters);
    }

    public JavaTracerAgentParameters(final JavaTracerAgentParameters parameters) {
        this.parameters.putAll(parameters.parameters);
    }

    public static JavaTracerAgentParameters parse(final String string) {
        Map<String, String> parameters = new HashMap<>();
        String[] parameterStrings = string.split(JavaTracerAgentParameters.DELIMITER);
        for (String parameterString : parameterStrings) {
            String[] parameter = parameterString.split(
                JavaTracerAgentParameters.ASSIGN_SYMBOL);
            if (parameter.length == 2) {
                parameters.put(parameter[0], parameter[1]);
            } else {
                throw new IllegalArgumentException(
                    Log.genMessage(
                        "Correct parameter string should following the format: parameterName1=parameterValue1;parameterName2=parameterValue2;... Invalid parameter string is given: "
                            + parameterString,
                        JavaTracerAgentParameters.class));
            }
        }
        return new JavaTracerAgentParameters(parameters);
    }

    public boolean containsParameters(final String parameter) {
        return parameters.containsKey(parameter);
    }

    public Set<Entry<String, String>> entrySet() {
        return parameters.entrySet();
    }

    public String getParameter(String parameter) {
        if (!this.containsParameters(parameter)) {
            throw new IllegalArgumentException(
                Log.genMessage("Parameter " + parameter + " is not found.",
                    JavaTracerAgentParameters.class));
        }
        return parameters.get(parameter);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    @Override
    public boolean equals(final Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        if (otherObj == this) {
            return true;
        }
        if (!(otherObj instanceof JavaTracerAgentParameters other)) {
            return false;
        }
        return parameters.equals(other.parameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> parametersEntry : parameters.entrySet()) {
            sb.append(parametersEntry.getKey())
                .append(JavaTracerAgentParameters.ASSIGN_SYMBOL)
                .append(parametersEntry.getValue())
                .append(JavaTracerAgentParameters.DELIMITER);
        }
        return sb.toString();
    }

    public void setParameter(String parameter, String value) {
        parameters.put(parameter, value);
    }

    public void update(final JavaTracerAgentParameters parameters) {
        this.parameters.putAll(parameters.parameters);
    }


}
