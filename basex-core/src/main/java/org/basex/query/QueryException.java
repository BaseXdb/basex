package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Thrown to indicate an exception during the parsing or evaluation of a query.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class QueryException extends Exception {
  /** Static exception. */
  public static final QueryException ERROR = new QueryException("") {
    @Override
    public synchronized Throwable fillInStackTrace() { return this; }
  };

  /** Stack. */
  private final ArrayList<InputInfo> stack = new ArrayList<>();
  /** Error QName. */
  private final QNm name;
  /** Error value. */
  private Value value = Empty.SEQ;
  /** Error reference. */
  private Err error;
  /** Code suggestions. */
  private StringList suggest;
  /** Error line and column. */
  private InputInfo info;
  /** Marked error column. */
  private int markedCol;
  /** Marks if this exception is catchable by a {@code try/catch} expression. */
  private boolean catchable = true;

  /**
   * Constructor, specifying an exception or error. {@link Err#BASX_GENERIC_X} will be set
   * as error code.
   * @param cause exception or error
   */
  public QueryException(final Throwable cause) {
    this(Util.message(cause));
  }

  /**
   * Constructor, specifying a simple error message. {@link Err#BASX_GENERIC_X} will be set
   * as error code.
   * @param message error message
   */
  public QueryException(final String message) {
    this(null, BASX_GENERIC_X, message);
  }

  /**
   * Default constructor.
   * @param info input info
   * @param error error reference
   * @param ext error extension
   */
  public QueryException(final InputInfo info, final Err error, final Object... ext) {
    this(info, error.qname(), error.desc, ext);
    this.error = error;
  }

  /**
   * Constructor, specifying the error code and message as string.
   * @param info input info
   * @param name error code
   * @param message error message
   * @param ext error extension
   */
  public QueryException(final InputInfo info, final QNm name, final String message,
      final Object... ext) {

    super(message(message, ext));
    this.name = name;
    if(info != null) info(info);
    for(final Object o : ext) {
      if(o instanceof Throwable) {
        initCause((Throwable) o);
        break;
      }
    }
  }

  /**
   * Returns the error column.
   * @return error column
   */
  public int column() {
    return info == null ? 0 : info.column();
  }

  /**
   * Returns the marked error column.
   * @return marked error column
   */
  public int markedColumn() {
    return markedCol;
  }

  /**
   * Returns the error line.
   * @return error line
   */
  public int line() {
    return info == null ? 0 : info.line();
  }

  /**
   * Returns the file.
   * @return error line
   */
  public String file() {
    return info == null ? null : info.path();
  }

  /**
   * Returns suggestions for code suggestions.
   * @return suggestions
   */
  public StringList suggest() {
    return suggest == null ? new StringList() : suggest;
  }

  /**
   * Sets code suggestions.
   * @param qp query parser
   * @param sug code suggestions
   * @return self reference
   */
  public QueryException suggest(final InputParser qp, final StringList sug) {
    suggest = sug;
    pos(qp);
    return this;
  }

  /**
   * Adds an input info to the stack.
   * @param ii input info
   */
  public void add(final InputInfo ii) {
    if(ii != null) stack.add(ii);
  }

  /**
   * Sets input info.
   * @param ii input info
   * @return self reference
   */
  public QueryException info(final InputInfo ii) {
    info = ii;
    return this;
  }

  /**
   * Returns the input info.
   * @return input info
   */
  public InputInfo info() {
    return info;
  }

  /**
   * Sets the error value.
   * @param val error value
   * @return self reference
   */
  public QueryException value(final Value val) {
    value = val;
    return this;
  }

  /**
   * Sets an error.
   * @param err error
   * @return self reference
   */
  public QueryException err(final Err err) {
    error = err;
    return this;
  }

  /**
   * Finds line and column for the specified query parser.
   * @param parser parser
   */
  void pos(final InputParser parser) {
    markedCol = parser.mark;
    if(info != null) return;
    // check if line/column information has already been added
    parser.pos = Math.min(parser.mark, parser.length);
    info = new InputInfo(parser);
  }

  /**
   * Returns the error name.
   * @return error name
   */
  public QNm qname() {
    return name;
  }

  /**
   * Returns the error.
   * @return error
   */
  public Err err() {
    return error;
  }

  /**
   * Returns the error value.
   * @return error value
   */
  public Value value() {
    return value;
  }

  @Override
  public String getLocalizedMessage() {
    return super.getMessage();
  }

  @Override
  public String getMessage() {
    final TokenBuilder tb = new TokenBuilder();
    if(info != null) tb.add(STOPPED_AT).add(info.toString()).add(COL).add(NL);
    final byte[] code = name.local();
    if(code.length != 0) tb.add('[').add(name.prefixId(QueryText.ERROR_URI)).add("] ");
    tb.add(getLocalizedMessage());
    if(!stack.isEmpty()) {
      tb.add(NL).add(NL).add(STACK_TRACE).add(COL);
      for(final InputInfo ii : stack) tb.add(NL).add(LI).add(ii.toString());
    }
    return tb.toString();
  }

  /**
   * Checks if this exception can be caught by a {@code try/catch} expression.
   * @return result of check
   */
  public boolean isCatchable() {
    return catchable;
  }

  /**
   * Makes this exception uncatchable by a {@code try/catch} expression.
   * @return self reference for convenience
   */
  public QueryException notCatchable() {
    catchable = false;
    return this;
  }

  /**
   * Creates the error message from the specified text and extension array.
   * @param text text message with optional placeholders
   * @param ext info extensions
   * @return argument
   */
  private static String message(final String text, final Object[] ext) {
    final int es = ext.length;
    for(int e = 0; e < es; e++) {
      if(ext[e] instanceof ExprInfo) ext[e] = chop(((ExprInfo) ext[e]).toErrorString(), null);
    }
    return Util.info(text, ext);
  }
}
