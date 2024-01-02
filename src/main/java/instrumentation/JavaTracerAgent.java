package instrumentation;

import instrumentation.transformers.MethodNameTransformer;
import java.lang.instrument.Instrumentation;

public class JavaTracerAgent {

    public static void premain(String argentArgs, Instrumentation inst) {
        inst.addTransformer(new MethodNameTransformer());
    }

}
