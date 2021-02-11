package org.basex.server;

import java.math.*;

/**
 * Log entry.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Milliseconds. */
  public BigDecimal ms;
  /** Message. */
  public String message;
}
