package core;

import java.io.Serial;

/**
 * @author LLT
 */
public class SavRtException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;
  private Enum<?> type;

  public SavRtException(String details, Object... params) {
    this(toString(details, params));
  }

  public SavRtException(Enum<?> type, String msg) {
    this(msg);
    this.type = type;
  }

  public SavRtException(String details) {
    super(details);
  }

  public SavRtException(Throwable cause) {
    super(cause);
  }

  private static String toString(String details, Object... params) {
    if (params == null) {
      return details;
    }
    StringBuilder msg = new StringBuilder(details);
    for (Object param : params) {
      msg.append("\n").append(param.toString());
    }
    return msg.toString();
  }

  public Enum<?> getType() {
    return type;
  }
}
