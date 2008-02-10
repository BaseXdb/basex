package org.basex.query.xquery;

import org.basex.core.Prop;
import org.basex.query.QueryException;

/**
 * This class indicates exceptions during query evaluation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQException extends QueryException {
  /** Position information. */
  private String pos;
  /** Error code. */
  private Object code;
  /** Error code. */
  private Object num;

  /**
   * Empty constructor; used for interrupting a query.
   */
  public XQException() {
    super("");
  }

  /**
   * Constructor.
   * @param s xquery error
   * @param e error arguments
   */
  public XQException(final Object[] s, final Object... e) {
    super(s[2], e);
    code = s[0];
    num = s[1];
  }

  /**
   * Returns the error message.
   * @return string
   */
  public String msg() {
    return super.getMessage();
  }

  /**
   * Returns the error code.
   * @return string
   */
  public String code() {
    return num == null ? code.toString() : String.format("%s%04d", code, num);
  }

  /**
   * Returns the error position.
   * @return position
   */
  public String pos() {
    return pos;
  }

  /**
   * Sets the error position.
   * @param p position
   */
  public void pos(final String p) {
    pos = p;
  }

  @Override
  public String getMessage() {
    final StringBuilder sb = new StringBuilder();
    if(pos != null) sb.append(pos);
    if(Prop.xqerrcode) sb.append("[" + code() + "] ");
    sb.append(super.getMessage());
    return sb.toString();
  }
}
