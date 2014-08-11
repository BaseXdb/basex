package org.basex.util;

import org.basex.query.expr.*;
import org.basex.query.util.*;


/**
 * This class contains information on the original query, which will be evaluated for
 * error feedback and debugging purposes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class InputInfo {
  /** Indicates if a raised error should be static. */
  private boolean check;
  /** Input path. */
  private final String path;
  /** Input query. */
  private String query;
  /** Parse position. */
  private int pos = -1;
  /** Line number ({@code 0} if not initialized). */
  private int line;
  /** Column number ({@code 0} if not initialized). */
  private int col;

  /**
   * Constructor.
   * @param parser input parser, containing information on the current parsing state
   */
  public InputInfo(final InputParser parser) {
    query = parser.input;
    path = parser.file;
    pos = parser.pos;
  }

  /**
   * Constructor.
   * @param path input path
   * @param line line
   * @param col column
   */
  public InputInfo(final String path, final int line, final int col) {
    this.path = path;
    this.line = line;
    this.col = col;
  }

  /**
   * Returns the input reference.
   * @return input reference
   */
  public String path() {
    return path;
  }

  /**
   * Returns the line position.
   * @return line position
   */
  public int line() {
    if(line == 0) lineCol();
    return line;
  }

  /**
   * Returns the column position.
   * @return column position
   */
  public int column() {
    if(col == 0) lineCol();
    return col;
  }

  /**
   * Calculates the column and line number in a string.
   */
  private void lineCol() {
    final int cl = Math.min(pos, query.length());
    final String q = query;
    int l = 1, c = 1;
    for(int i = 0, ch; i < cl; i += Character.charCount(ch)) {
      ch = q.codePointAt(i);
      if(ch == '\n') { l++; c = 1; } else if(ch != '\r') { c++; }
    }
    line = l;
    col = c;
  }

  /**
   * Returns the check flag (invoked by {@link Err#get(InputInfo, Object...)}).
   * @return check flag
   */
  public boolean check() {
    return check;
  }

  /**
   * Sets the check flag (invoked e.g. by {@link Castable#optimize}).
   * @param value value to set
   */
  public void check(final boolean value) {
    check = value;
  }

  @Override
  public boolean equals(final Object object) {
    if(!(object instanceof InputInfo)) return false;
    final InputInfo ii = (InputInfo) object;
    return (path != null ? path.equals(ii.path) : query.equals(ii.query)) && pos == ii.pos;
  }

  @Override
  public int hashCode() {
    return (path != null ? path.hashCode() : query.hashCode()) + pos;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(path == null ? "." : path).add(", ").addExt(line()).add('/').addExt(column());
    return tb.toString();
  }
}
