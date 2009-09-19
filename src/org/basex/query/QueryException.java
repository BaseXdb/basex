package org.basex.query;

import static org.basex.query.QueryTokens.*;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.query.iter.Iter;
import org.basex.util.InputParser;
import org.basex.util.StringList;

/**
 * This class indicates exceptions during query parsing or evaluation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class QueryException extends Exception {
  /** Error items. */
  public Iter iter = Iter.EMPTY;

  /** File reference. */
  private IO file;
  /** Possible completions. */
  private StringList complete;
  /** Error code. */
  private String code;
  /** Error line. */
  private int line;
  /** Error column. */
  private int col;

  /**
   * Constructor.
   * @param s message
   * @param e message extension
   */
  public QueryException(final Object s, final Object... e) {
    super(Main.info(s, e));
  }

  /**
   * Constructor.
   * @param s xquery error
   * @param e error arguments
   */
  public QueryException(final Object[] s, final Object... e) {
    this(s[2], e);
    // skip error codes in gui mode
    if(Prop.gui) return;
    code = s[1] == null ? s[0].toString() : String.format("%s%04d", s[0], s[1]);
  }

  /**
   * Returns the error code.
   * @return position
   */
  public String code() {
    return code;
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public int col() {
    return col;
  }

  /**
   * Returns the error line.
   * @return error line
   */
  public int line() {
    return line;
  }

  /**
   * Possible completions.
   * @return error line
   */
  public StringList complete() {
    return complete == null ? new StringList() : complete;
  }

  /**
   * Sets the error position.
   * @param parser parser
   */
  public void pos(final InputParser parser) {
    if(line != 0 || parser == null) return;
    file = parser.file;
    line = 1;
    col = 1;
    for(int i = 0; i < parser.qm && i < parser.ql; i++) {
      final char ch = parser.qu.charAt(i);
      if(ch == 0x0A) { line++; col = 1; } else if(ch != 0x0D) { col++; }
    }
  }

  /**
   * Sets code completions.
   * @param qp query parser
   * @param comp completions
   */
  public void complete(final InputParser qp, final StringList comp) {
    complete = comp;
    pos(qp);
  }

  /**
   * Returns the simple error message.
   * @return string
   */
  public String simple() {
    return super.getMessage();
  }

  /**
   * Returns an extended error message.
   * @return string
   */
  public String extended() {
    final StringBuilder sb = new StringBuilder();
    if(code != null) sb.append("[" + code + "] ");
    return sb + simple();
  }

  @Override
  public String getMessage() {
    final StringBuilder sb = new StringBuilder();
    if(line != 0) {
      sb.append(STOPPED);
      sb.append(Main.info(LINEINFO, line));
      if(col != 0) sb.append(Main.info(COLINFO, col));
      if(file != null) sb.append(Main.info(FILEINFO, file));
      sb.append(": \n");
    }
    return sb.append(extended()).toString();
  }
}
