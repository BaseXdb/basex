package org.basex.util;

/**
 * This class contains the original query, its file reference, and line/column
 * information.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InputInfo {
  /** File reference. */
  public final String file;
  /** Input query. */
  private final String query;
  /** Parse position. */
  private final int pos;
  /** Line and column number. */
  private int[] lc;

  /**
   * Constructor.
   * @param p input parser, containing information on the current parsing state
   */
  public InputInfo(final InputParser p) {
    query = p.input;
    file = p.file;
    pos = p.ip;
  }

  /**
   * Returns an array with the line and column position of the associated expression.
   * @return line and column position
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
    final int[] lc = { 1, 1 };
    for(int i = 0, ch; i < qp; i += Character.charCount(ch)) {
      ch = qu.codePointAt(i);
      if(ch == '\n') { lc[0]++; lc[1] = 1; } else if(ch != '\r') { lc[1]++; }
    }
    return lc;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof InputInfo)) return false;
    final InputInfo ii = (InputInfo) obj;
    return (file != null ? file.equals(ii.file) : query.equals(ii.query)) &&
        pos == ii.pos;
  }

  @Override
  public int hashCode() {
    return (file != null ? file.hashCode() : query.hashCode()) + pos;
  }

  @Override
  public String toString() {
    final int[] p = lineCol();
    return Util.info("InputInfo[Line %, Column %]", p[0], p[1]);
  }
}
