package org.basex.util;

/**
 * This class contains the original query, its file reference, and line/column
 * information.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class InputInfo {
  /** File reference. */
  private final String file;
  /** Input query. */
  private final String query;
  /** Parse position. */
  private final int pos;
  /** Line and column number. */
  private int[] lineCol;

  /**
   * Constructor.
   * @param parser input parser, containing information on the current parsing state
   */
  public InputInfo(final InputParser parser) {
    query = parser.input;
    file = parser.file;
    pos = parser.pos;
  }

  /**
   * Returns the input reference.
   * @return input reference
   */
  public String file() {
    return file;
  }

  /**
   * Returns an array with the line and column position of the associated expression.
   * @return line and column position
   */
  public int[] lineCol() {
    if(lineCol == null) lineCol = lineCol(query, Math.min(pos, query.length()));
    return lineCol;
  }

  /**
   * Calculates the column and line number of a given offset in the string.
   * @param query query string
   * @param pos query position
   * @return two element array of line and column number
   */
  private static int[] lineCol(final String query, final int pos) {
    int l = 1, c = 1;
    for(int i = 0, ch; i < pos; i += Character.charCount(ch)) {
      ch = query.codePointAt(i);
      if(ch == '\n') { l++; c = 1; } else if(ch != '\r') { c++; }
    }
    return new int[] { l, c };
  }

  @Override
  public boolean equals(final Object object) {
    if(!(object instanceof InputInfo)) return false;
    final InputInfo ii = (InputInfo) object;
    return (file != null ? file.equals(ii.file) : query.equals(ii.query)) &&
        pos == ii.pos;
  }

  @Override
  public int hashCode() {
    return (file != null ? file.hashCode() : query.hashCode()) + pos;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(file == null ? "." : file);
    final int[] lc = lineCol();
    tb.add(", ").addExt(lc[0]).add('/').addExt(lc[1]);
    return tb.toString();
  }
}
