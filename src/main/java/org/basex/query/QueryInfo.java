package org.basex.query;

import org.basex.util.InputParser;

/**
 * Parsing information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class QueryInfo {
  /** Input query. */
  public final String query;
  /** Parse position. */
  public final int pos;

  /**
   * Optimizes and compiles the expression.
   * @param p parsing position
   */
  public QueryInfo(final InputParser p) {
    query = p.qu;
    pos = p.qp;
  }
}
