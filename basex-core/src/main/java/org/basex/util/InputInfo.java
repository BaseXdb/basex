package org.basex.util;

import org.basex.query.*;
import org.basex.query.value.type.*;

/**
 * This class contains information on the original query, which will be evaluated for
 * error feedback and debugging purposes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InputInfo {
  /**
   * Indicates if a raised error will only be handled internally.
   * If this flag is activated, only light-weight errors will be created.
   */
  private boolean internal;
  /** Input path. */
  private final String path;
  /** Input string (can be {@code null}). */
  private String input;
  /** Line number ({@code 0} if not initialized). */
  private int line;
  /** Column number of (if not initialized) string position. */
  private int col;

  /**
   * Constructor.
   * @param parser input parser, containing information on the current parsing state
   */
  public InputInfo(final InputParser parser) {
    input = parser.input;
    path = parser.file;
    col = parser.pos;
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
    init();
    return line;
  }

  /**
   * Returns the column position.
   * @return column position
   */
  public int column() {
    init();
    return col;
  }

  /**
   * Calculates the column and line number in a string.
   */
  private void init() {
    // positions have already been calculated
    if(line != 0) return;

    final int cl = Math.min(col, input.length());
    final String q = input;
    int l = 1, c = 1;
    for(int i = 0, ch; i < cl; i += Character.charCount(ch)) {
      ch = q.codePointAt(i);
      if(ch == '\n') { l++; c = 1; }
      else if(ch != '\r') { c++; }
    }
    line = l;
    col = c;
  }

  /**
   * Returns the check flag (invoked by {@link QueryError#get(InputInfo, Object...)}).
   * @return check flag
   */
  public boolean internal() {
    return internal;
  }

  /**
   * Activates light-weight error handling (invoked e.g. by {@link SeqType#cast(
   * org.basex.query.value.Value, boolean, QueryContext, StaticContext, InputInfo)}).
   * @param value value to set
   */
  public void internal(final boolean value) {
    internal = value;
  }

  @Override
  public boolean equals(final Object object) {
    if(!(object instanceof InputInfo)) return false;
    final InputInfo ii = (InputInfo) object;
    return (path != null ? path.equals(ii.path) : input.equals(ii.input)) &&
        column() == ii.column() && line() == ii.line();
  }

  @Override
  public int hashCode() {
    return (path != null ? path.hashCode() : input.hashCode()) + column() + (line() << 16);
  }

  @Override
  public String toString() {
    return Strings.concat(path == null ? "." : path, ", ", line(), '/', column());
  }
}
