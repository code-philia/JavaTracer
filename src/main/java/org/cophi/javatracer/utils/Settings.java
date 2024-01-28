package org.cophi.javatracer.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import org.cophi.javatracer.model.trace.PotentialCorrectPatternList;
import org.cophi.javatracer.model.variables.UserInterestedVariables;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Settings {

    public static String projectName;
    public static String launchClass;
    public static String testMethod;

    public static boolean isRecordSnapshot;
    public static HashMap<String, ICompilationUnit> iCompilationUnitMap = new HashMap<>();
    public static boolean isApplyAdvancedInspector;
    public static boolean isRunTest;
    public static boolean isRunWtihDebugMode;
    public static int stepLimit;

    public static String autoFeedbackMethod;
    /**
     * We divide the library code into two categories: the interesting ones (e.g., those in
     * java.util.*) and the normal ones. We only capture the data and control dependency for our
     * interested library code. However, it is possible that the interested library code is called
     * by some normal library code, of course, these normal library code is called by application
     * code. In such case, we will not capture the data/control dependencies of the library code
     * called by normal library code. By this means, many of the library variables will not be
     * visited, saving the time for recording the trace.
     * <p>
     * We use this field to decide whether such an optimization is enabled or not.
     */
    public static boolean applyLibraryOptimization = true;
    public static int distribtionLayer = 3;

    /**
     * The portion remains in a trace node when propagating suspiciousness.
     */
    public static double remainingRate = 0.5;
    public static PotentialCorrectPatternList potentialCorrectPatterns = new PotentialCorrectPatternList();
    /**
     * the variables checked by user as wrong.
     */
    public static UserInterestedVariables interestedVariables = new UserInterestedVariables();
    /**
     * the trace order in execution trace that are marked by user as wrong-path, it is used to check
     * whether certain trace node is marked by user as wrong-path.
     */
    public static HashSet<Integer> wrongPathNodeOrder = new HashSet<>();
    /**
     * This stack allow user to undo his checking operations.
     */
    public static Stack<CheckingState> checkingStateStack = new Stack<>();
    /**
     * The following two map is used to trade space for time.
     */
    public static HashMap<String, CompilationUnit> compilationUnitMap = new HashMap<>();
    public static boolean enableLoopInference = true;
    public static boolean supportConcurrentTrace;
    private static Integer variableLayer;

    public static int getVariableLayer() {
        int level = Settings.variableLayer;
        return level;
    }

    public static void setVariableLayer(Integer level) {
        variableLayer = level;
    }

}