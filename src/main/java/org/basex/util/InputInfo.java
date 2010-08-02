package org.basex.util;

/**
 * Input information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class InputInfo {
  /** Input query. */
  public final String query;
  /** Parse position. */
  public final int pos;

  /**
   * Optimizes and compiles the expression.
   * @param p parsing position
   */
  public InputInfo(final InputParser p) {
    query = p.qu;
    pos = p.qp;
  }
}
