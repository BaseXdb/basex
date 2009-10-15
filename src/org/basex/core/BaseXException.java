package org.basex.core;

/**
 * This is a simple container for sessions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class BaseXException extends RuntimeException {
  /**
   * Constructor.
   * @param msg detail message
   */
  public BaseXException(final String msg) {
    super(msg);
  }
}
