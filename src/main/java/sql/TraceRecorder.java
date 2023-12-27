/**
 *
 */
package sql;

import instrumentation.instr.instruction.info.SerializableLineInfo;
import java.util.HashMap;
import java.util.List;
import model.trace.Trace;

/**
 * @author knightsong
 *
 */
public interface TraceRecorder {

    void store(List<Trace> trace);

    void serialize(HashMap<Integer, SerializableLineInfo> instructionTable);
}
