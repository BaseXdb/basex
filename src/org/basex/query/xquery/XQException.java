package org.basex.query.xquery;

import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.util.StringList;

/**
 * This class indicates exceptions during query evaluation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQException extends QueryException {
  /** Error items. */
  public Item item = Seq.EMPTY;
  
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
  }

  /**
   * Constructor.
   * @param sl code completion list
   * @param s xquery error
   * @param e error arguments
   */
  public XQException(final StringList sl, final Object[] s, final Object... e) {
    this(s, e);
    complete = sl;
    if(!Prop.xqerrcode) return;
    code = s[1] == null ? s[0].toString() : String.format("%s%04d", s[0], s[1]);
  }
}
