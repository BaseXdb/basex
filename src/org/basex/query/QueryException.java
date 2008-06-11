package org.basex.query;

import static org.basex.query.QueryTokens.*;
import org.basex.BaseX;
import org.basex.io.IO;

/**
 * This class indicates exceptions during query parsing or evaluation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class QueryException extends Exception {
  /** File reference. */
  protected IO file;
  /** Error code. */
  protected String code;
  /** Error line. */
  protected int line;
  /** Error column. */
  protected int col;

  /**
   * Constructor.
   * @param s message
   * @param e message extension
   */
  public QueryException(final Object s, final Object... e) {
    super(BaseX.info(s, e));
  }

  /**
   * Returns the error code.
   * @return position
   */
  public final String code() {
    return code;
  }

  /**
   * Returns the error line.
   * @return error line
   */
  public final int line() {
    return line;
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public final int col() {
    return col;
  }

  /**
   * Sets the error position.
   * @param parser parser
   */
  public final void pos(final QueryParser parser) {
    if(line != 0 || parser == null) return;
    file = parser.file;
    line = 1;
    col = 1;
    for(int i = 0; i + 1 < parser.qp && i < parser.ql; i++) {
      final char ch = parser.qu.charAt(i);
      if(ch == 0x0A) { line++; col = 1; } else if(ch != 0x0D) { col++; }
    }
  }

  /**
   * Returns the error message.
   * @return string
   */
  public final String msg() {
    return super.getMessage();
  }

  @Override
  public final String getMessage() {
    final StringBuilder sb = new StringBuilder();
    if(line != 0) {
      sb.append(file == null ? BaseX.info(POSINFO, line, col) :
        BaseX.info(POSFILEINFO, line, col, file));
    }
    if(code != null) sb.append("[" + code + "] ");
    sb.append(super.getMessage());
    return sb.toString();
  }
}
