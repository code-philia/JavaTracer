/**
 *
 */
package sql;


import instrumentation.AgentParams;

/**
 * @author knightsong
 *
 */
public enum Recorder {
    //	FILE,SQLITE3,MYSQL;
    FILE;

    public static TraceRecorder create(AgentParams params) {
        return switch (params.getTraceRecorderName()) {
            case "FILE" -> new FileRecorder(params);
//            case "SQLITE3" -> new SqliteRecorder(params.getDumpFile(), params.getRunId());
////		case "MYSQL":
////			return new MysqlRecorder(params.getRunId());
//            default -> new SqliteRecorder(params.getDumpFile(), params.getRunId());
            default -> throw new IllegalStateException(
                "Unexpected value: " + params.getTraceRecorderName());
        };
    }

}
