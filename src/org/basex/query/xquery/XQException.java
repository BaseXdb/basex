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
    if(!Prop.xqerrcode) return;
    code = s[1] == null ? s[0].toString() : String.format("%s%04d", s[0], s[1]);
  }
}
