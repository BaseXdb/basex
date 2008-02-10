package org.basex.query;

import org.basex.BaseX;

/**
 * This class indicates exceptions during query parsing or evaluation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Tim Petrowsky
 */
public class QueryException extends Exception {
  /**
   * Constructor.
   * @param s message
   */
  public QueryException(final String s) {
    super(s);
  }

  /**
   * Constructor.
   * @param s message
   * @param e message extension
   */
  public QueryException(final Object s, final Object... e) {
    super(BaseX.info(s, e));
  }
}
