package org.basex.util.log;

import java.math.*;
import java.util.*;

/**
 * Log entry.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LogEntry {
  /** Time. */
  public String time;
  /** Address. */
  public String address;
  /** User. */
  public String user;
  /** Type. */
  public String type;
  /** Message. */
  public String info;
  /** Runtime. */
  public String runtime;

  /** Milliseconds. */
  public BigDecimal ms;

  /** Date. */
  Date date;

  /** Cached string representation. */
  private String string;

  @Override
  public String toString() {
    if(string == null) {
      final Object[] fields = { time, address, user, type, info, runtime, ms };
      final StringBuilder sb = new StringBuilder();
      for(final Object field : fields) {
        if(!sb.isEmpty()) sb.append('\t');
        if(field != null) sb.append(field);
      }
      string = sb.toString();
    }
    return string;
  }
}
