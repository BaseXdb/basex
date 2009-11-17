package org.basex.core;

/**
 * This is a simple container for sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class BaseXException extends Exception {
  /**
   * Constructs an exception with the specified message and extension.
   * @param s message
   * @param e message extension
   */
  public BaseXException(final String s, final Object... e) {
    super(Main.info(s, e));
  }
}
