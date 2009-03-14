package org.basex.query;

import static org.basex.query.QueryTokens.*;
import org.basex.BaseX;
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
public class QueryException extends Exception {
  /** Error items. */
  public Iter iter = Iter.EMPTY;

  /** File reference. */
  protected IO file;
  /** Possible completions. */
  protected StringList complete;
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
   * Constructor.
   * @param s xquery error
   * @param e error arguments
   */
  public QueryException(final Object[] s, final Object... e) {
    this(s[2], e);
    if(!Prop.xqerrcode) return;
    code = s[1] == null ? s[0].toString() : String.format("%s%04d", s[0], s[1]);
  }

  /**
   * Returns the error code.
   * @return position
   */
  public final String code() {
    return code;
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public final int col() {
    return col;
  }

  /**
   * Returns the error line.
   * @return error line
   */
  public final int line() {
    return line;
  }

  /**
   * Possible completions.
   * @return error line
   */
  public final StringList complete() {
    return complete == null ? new StringList() : complete;
  }

  /**
   * Sets the error position.
   * @param parser parser
   */
  public final void pos(final InputParser parser) {
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
  public final void complete(final InputParser qp, final StringList comp) {
    complete = comp;
    qp.qm++;
    pos(qp);
  }

  /**
   * Returns the simple error message.
   * @return string
   */
  public final String simple() {
    return super.getMessage();
  }

  /**
   * Returns an extended error message.
   * @return string
   */
  public final String extended() {
    final StringBuilder sb = new StringBuilder();
    if(code != null) sb.append("[" + code + "] ");
    return sb + simple();
  }

  @Override
  public final String getMessage() {
    final StringBuilder sb = new StringBuilder();
    if(line != 0) {
      sb.append(STOPPED);
      sb.append(BaseX.info(LINEINFO, line));
      if(col != 0) sb.append(BaseX.info(COLINFO, col));
      if(file != null) sb.append(BaseX.info(FILEINFO, file));
      sb.append(": \n");
    }
    return sb.append(extended()).toString();
  }
}
