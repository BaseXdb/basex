package org.basex.util;

import org.basex.io.IO;

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
  /** Input file. */
  public IO file;

  /**
   * Optimizes and compiles the expression.
   * @param p parsing position
   */
  public InputInfo(final InputParser p) {
    query = p.qu;
    pos = p.qp;
    file = p.file;
  }
}
