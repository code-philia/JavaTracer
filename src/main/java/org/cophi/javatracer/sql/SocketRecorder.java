/**
 *
 */
package org.cophi.javatracer.sql;

import org.cophi.javatracer.instrumentation.Agent;
import org.cophi.javatracer.instrumentation.AgentParams;
import org.cophi.javatracer.instrumentation.instr.instruction.info.SerializableLineInfo;
import org.cophi.javatracer.instrumentation.output.TraceOutputWriter;
import org.cophi.javatracer.instrumentation.output.tcp.TcpConnector;
import java.util.HashMap;
import java.util.List;
import org.cophi.javatracer.model.trace.Trace;

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
