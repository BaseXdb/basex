package org.basex.util;

import org.basex.io.IO;

/**
 * Input information.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InputInfo {
  /** Input query. */
  private final String query;
  /** Parse position. */
  private final int pos;
  /** Input file. */
  public final IO file;
  /** Line and column number. */
  private int[] lc;

  /**
   * Optimizes and compiles the expression.
   * @param p parsing position
   */
  public InputInfo(final InputParser p) {
    query = p.query;
    pos = p.qp;
    file = p.file;
  }

  /**
   * Getter for line and column number.
   * @return two element array of line and column number
   */
  public int[] lineCol() {
    if(lc == null) lc = lineCol(query, Math.min(pos - 1, query.length()));
    return lc;
  }

  /**
   * Calculates the column and line number of a given offset in the string.
   * @param qu query string
   * @param qp offset
   * @return two element array of line and column number
   */
  public static int[] lineCol(final String qu, final int qp) {
    final int[] lc = {1, 1};
    for(int i = 0, ch; i < qp; i += Character.charCount(ch)) {
      ch = qu.codePointAt(i);
      if(ch == '\n') { lc[0]++; lc[1] = 1; } else if(ch != '\r') { lc[1]++; }
    }
    return lc;
  }

  @Override
  public String toString() {
    final int[] p = lineCol();
    return Util.info("InputInfo[Line %, Column %]", p[0], p[1]);
  }
}
