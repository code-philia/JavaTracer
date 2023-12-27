/**
 *
 */
package sql;

import instrumentation.Agent;
import instrumentation.AgentParams;
import instrumentation.instr.instruction.info.SerializableLineInfo;
import instrumentation.output.TraceOutputWriter;
import instrumentation.output.tcp.TcpConnector;
import java.util.HashMap;
import java.util.List;
import model.trace.Trace;

/**
 *
 *
 */
public class SocketRecorder implements TraceRecorder {

    AgentParams agentParams;

    public SocketRecorder(AgentParams agentParams) {
        this.agentParams = agentParams;
    }

    /*
     * @see sql.TraceRecorder#store(model.trace.Trace)
     */
    @Override
    public void store(List<Trace> traceList) {
        TcpConnector tcpConnector = new TcpConnector(agentParams.getTcpPort());
        TraceOutputWriter traceWriter;
        try {
            traceWriter = tcpConnector.connect();
            traceWriter.writeString(Agent.getProgramMsg());
            traceWriter.writeTrace(traceList);
            traceWriter.flush();
            Thread.sleep(10000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tcpConnector.close();
    }

    @Override
    public void serialize(HashMap<Integer, SerializableLineInfo> instructionTable) {
        // TODO Auto-generated method stub

    }
}
