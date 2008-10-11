package org.basex.fs;

import java.io.IOException;
import org.basex.BaseX;

/**
 * This class indicates exceptions during file system executions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FSException extends IOException {
  /** Error strings. */
  private static final String[] FSERR = new String[128];

  // Initializes the error strings after the first class class.
  static {
    for(final String[] c : FSText.CODES) FSERR[Integer.parseInt(c[0])] = c[1];
  }

  /**
   * Constructor.
   * @param s message
   */
  public FSException(final String s) {
    super(s);
  }

  /**
   * Constructor.
   * @param cmd command reference
   * @param s error info
   * @param code error code
   */
  public FSException(final String cmd, final Object s, final int code) {
    super(error(cmd, s, code));
  }

  /**
   * Creates an error string.
   * @param cmd command reference
   * @param s error info
   * @param c error code
   * @return error string
   */
  public static String error(final String cmd, final Object s, final int c) {
    final int e = Math.min(c, FSERR.length - 1);
    final String err = FSERR[e] != null ? FSERR[e] : FSERR[0];
    return BaseX.info("%: \"%\" %", cmd, s, err);
  }
}
