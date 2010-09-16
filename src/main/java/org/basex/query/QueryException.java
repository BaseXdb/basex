package org.basex.query;

import static org.basex.core.Text.*;
import org.basex.io.IO;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.InputParser;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class indicates exceptions during query parsing or evaluation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class QueryException extends Exception {
  /** Error items. */
  public Iter iter = Iter.EMPTY;

  /** File reference. */
  private IO file;
  /** Possible completions. */
  private StringList complete;
  /** Error type. */
  private Err.Type type;
  /** Error number. */
  private int num;
  /** Alternative error code. */
  private String code;
  /** Error line. */
  private int line;
  /** Error column. */
  private int col;
  /** Marked error column. */
  private int markedCol;

  /**
   * Constructor.
   * @param ii input info
   * @param c code
   * @param s message
   * @param e message extension
   */
  public QueryException(final InputInfo ii, final String c, final String s,
      final Object... e) {
    super(Util.info(s, chop(e)));
    code = c;
    if(ii == null) return;

    line = 1;
    col = 1;
    final int qp = Math.min(ii.pos - 1, ii.query.length());
    for(int i = 0, ch; i < qp; i += Character.charCount(ch)) {
      ch = ii.query.codePointAt(i);
      if(ch == 0x0A) { line++; col = 1; } else if(ch != 0x0D) { col++; }
    }
  }

  /**
   * Constructor.
   * @param ii input info
   * @param s xquery error
   * @param e error arguments
   */
  public QueryException(final InputInfo ii, final Err s, final Object... e) {
    this(ii, null, s.desc, e);
    type = s.type;
    num = s.num;
  }

  /**
   * Chops the specified array entries to a maximum length.
   * @param t token array
   * @return argument
   */
  private static Object[] chop(final Object[] t) {
    for(int i = 0; i < t.length; ++i) {
      if(t[i] instanceof byte[]) {
        t[i] = Token.string((byte[]) t[i]);
      } else if(t[i] instanceof Throwable) {
        final Throwable th = (Throwable) t[i];
        t[i] = th.getMessage() != null ? th.getMessage() : th.toString();
      } else if(!(t[i] instanceof String)) {
        t[i] = t[i].toString();
      }
      // [CG] XQuery/Exception: verify if/which strings are to be chopped
      //final String s = t[i].toString();
      //t[i] = s.length() > 1000 ? s.substring(0, 1000) + DOTS : s;
    }
    return t;
  }

  /**
   * Returns the error code.
   * @return position
   */
  public String code() {
    return code == null ? String.format("%s%04d", type, num) : code;
  }
  
  /**
   * Returns the error type of this error.
   * @return error type
   */
  public Err.Type type() {
    return type;
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public int col() {
    return col;
  }

  /**
   * Returns the marked error column.
   * @return error column
   */
  public int markedCol() {
    return markedCol;
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
   * Finds line and column for the specified query parser.
   * @param parser parser
   */
  void pos(final InputParser parser) {
    markedCol = parser.qm;
    // check if information has already been added
    if(line != 0) return;

    file = parser.file;
    line = 1;
    col = 1;
    final int len = Math.min(parser.qm, parser.ql);
    for(int i = 0, ch; i < len; i += Character.charCount(ch)) {
      ch = parser.qu.codePointAt(i);
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
    if(type != null) sb.append("[" + code() + "] ");
    return sb + simple();
  }

  @Override
  public String getMessage() {
    final StringBuilder sb = new StringBuilder();
    if(line != 0) {
      sb.append(STOPPED + ' ');
      sb.append(Util.info(LINEINFO, line));
      if(col != 0) sb.append(QueryTokens.SEP + Util.info(COLINFO, col));
      if(file != null) sb.append(Util.info(' ' + FILEINFO, file));
      sb.append(": \n");
    }
    return sb.append(extended()).toString();
  }
}
