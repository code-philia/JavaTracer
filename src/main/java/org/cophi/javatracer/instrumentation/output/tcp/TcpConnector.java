package org.cophi.javatracer.instrumentation.output.tcp;

import org.cophi.javatracer.exceptions.SavRtException;
import org.cophi.javatracer.instrumentation.output.TraceOutputWriter;
import java.io.IOException;
import java.net.Socket;

public class TcpConnector {

    private final int tcpPort;
    private TraceOutputWriter inputWriter;
    private Socket server;

    public TcpConnector(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public TraceOutputWriter connect() throws Exception {
        while (true) {
            try {
                server = new Socket("localhost", tcpPort);
                // TODO: set timeout!
                break;
            } catch (IOException e) {
                throw new SavRtException(e);
            }
        }
        try {
            inputWriter = new TraceOutputWriter(server.getOutputStream());
        } catch (IOException e) {
            throw new SavRtException(e);
        }
        return inputWriter;
    }

    public void close() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputWriter != null) {
            try {
                inputWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
