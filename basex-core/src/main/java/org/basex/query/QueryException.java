package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Thrown to indicate an exception during the parsing or evaluation of a query.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class QueryException extends Exception {
  /** Index of the first error value that depends on the error location. */
  public static final int LOCATION = 4;
  /** Static exception. */
  static final QueryException ERROR = new QueryException("") {
    @Override
    public synchronized Throwable fillInStackTrace() { return this; }
  };

  /** Stack. */
  private final ArrayList<InputInfo> stack = new ArrayList<>();
  /** Error QName. */
  private final QNm name;
  /** Error value. */
  private Value value = Empty.VALUE;
  /** Serialization parameters for the error output ({@code null} if not specified). */
  private SerializerOptions output;
  /** Error reference ({@code null} for dynamic error messages). */
  private QueryError error;
  /** Error line and column (can be {@code null}). */
  private InputInfo info;
  /** Marked error column. */
  private int markedCol;
  /** Marks if this exception is catchable by a {@code try/catch} expression. */
  private boolean catchable = true;
  /** Marks if this exception was raised by the initializing expression of a record field. */
  private boolean initializer;

  // no public constructor with a single Throwable: ForkJoinTask would use it to wrap thrown errors

  /**
   * Constructor, specifying a simple error message. {@link QueryError#BASEX_ERROR_X} will be set
   * as error code.
   * @param message error message
   */
  public QueryException(final String message) {
    this(null, BASEX_ERROR_X, message);
  }

  /**
   * Default constructor.
   * @param info input info (can be {@code null})
   * @param error error reference
   * @param ext error extension
   */
  public QueryException(final InputInfo info, final QueryError error, final Object... ext) {
    this(info, error.qname(), error.message(), ext);
    this.error = error;
  }

  /**
   * Constructor, specifying the error code and message as string.
   * @param info input info (can be {@code null})
   * @param name error code
   * @param message error message
   * @param ext error extension
   */
  public QueryException(final InputInfo info, final QNm name, final String message,
      final Object... ext) {

    super(message(message, ext, info));
    this.name = name;
    if(info != null) info(info);
    for(final Object o : ext) {
      if(o instanceof final Throwable th) cause(th);
    }
  }

  /**
   * Copy constructor.
   * @param ex exception to be copied
   */
  private QueryException(final QueryException ex) {
    super(ex.getLocalizedMessage());
    cause(ex.getCause());
    name = ex.name;
    value = ex.value;
    output = ex.output;
    error = ex.error;
    info = ex.info;
    markedCol = ex.markedCol;
    catchable = ex.catchable;
    initializer = ex.initializer;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return Prop.debug ? super.fillInStackTrace() : this;
  }

  /**
   * Attaches the throwable that caused this error.
   * @param cause cause (can be {@code null})
   * @return self reference
   */
  public final QueryException cause(final Throwable cause) {
    if(cause != null && cause != this && getCause() == null) initCause(cause);
    return this;
  }

  /**
   * Returns the column number of the error.
   * @return column number, or {@code 0} if unknown
   */
  public final int column() {
    return info == null ? 0 : info.column();
  }

  /**
   * Returns the column number of a marked error.
   * @return column number
   */
  public final int markedColumn() {
    return markedCol;
  }

  /**
   * Returns the line number of the error.
   * @return line number, or {@code 0} if unknown
   */
  public final int line() {
    return info == null ? 0 : info.line();
  }

  /**
   * Returns the input path.
   * @return input path, or {@code null} if unknown
   */
  public final String path() {
    return info == null ? null : info.path();
  }

  /**
   * Sets code suggestions.
   * @param qp query parser
   * @return self reference
   */
  public final QueryException suggest(final InputParser qp) {
    pos(qp);
    return this;
  }

  /**
   * Adds an input info to the stack.
   * @param ii input info (can be {@code null})
   * @return self reference
   */
  public final QueryException add(final InputInfo ii) {
    if(ii != null) stack.add(ii);
    return this;
  }

  /**
   * Sets input info.
   * @param ii input info (can be {@code null})
   * @return self reference
   */
  public final QueryException info(final InputInfo ii) {
    info = ii;
    return this;
  }

  /**
   * Returns the input info.
   * @return input info (can be {@code null})
   */
  public final InputInfo info() {
    return info;
  }

  /**
   * Sets the error value.
   * @param val error value (can be {@code null})
   * @return self reference
   */
  public final QueryException value(final Value val) {
    value = val;
    return this;
  }

  /**
   * Sets an error.
   * @param err error
   * @return self reference
   */
  final QueryException error(final QueryError err) {
    error = err;
    return this;
  }

  /**
   * Adopts input information from the specified parser.
   * @param parser parser
   */
  final void pos(final InputParser parser) {
    markedCol = parser.mark;
    if(info != null) return;
    // check if line/column information has already been added
    parser.pos = Math.min(parser.mark, parser.length);
    info = parser.info();
  }

  /**
   * Returns the error name.
   * @return error name
   */
  public final QNm qname() {
    return name;
  }

  /**
   * Returns the error.
   * @return error (can be {@code null})
   */
  public final QueryError error() {
    return error;
  }

  /**
   * Returns the error value.
   * @return error value
   */
  public final Value value() {
    return value;
  }

  /**
   * Attaches serialization parameters for rendering the error as output.
   * @param so serialization parameters
   * @return self reference
   */
  public final QueryException output(final SerializerOptions so) {
    output = so;
    return this;
  }

  /**
   * Returns serialization parameters for rendering the error as output.
   * @return serialization parameters (can be {@code null})
   */
  public final SerializerOptions output() {
    return output;
  }

  /**
   * Checks if the exception matches the specified type.
   * @param type error type
   * @return result of check
   */
  public final boolean matches(final ErrType type) {
    return error != null && error.toString().startsWith(type.name());
  }

  @Override
  public final String getLocalizedMessage() {
    return super.getMessage();
  }

  @Override
  public final String getMessage() {
    final TokenBuilder tb = new TokenBuilder();
    if(info != null) tb.add(STOPPED_AT).add(location(info)).add(COL).add(NL);
    final byte[] code = name.local();
    if(code.length != 0) tb.add('[').add(name.prefixId(QueryText.ERROR_URI)).add("] ");
    tb.add(getLocalizedMessage());
    if(!stack.isEmpty()) {
      tb.add(NL).add(NL).add(STACK_TRACE).add(COL);
      for(final InputInfo ii : stack) tb.add(NL).add(LI).add(location(ii));
    }
    return tb.toString();
  }

  /**
   * Checks if this exception can be caught by a {@code try/catch} expression.
   * @return result of check
   */
  public final boolean isCatchable() {
    return catchable;
  }

  /**
   * Makes this exception uncatchable by a {@code try/catch} expression.
   * @return self reference for convenience
   */
  public final QueryException notCatchable() {
    catchable = false;
    return this;
  }

  /**
   * Checks if this exception was raised by the initializing expression of a record field.
   * @return result of check
   */
  public final boolean isInitializer() {
    return initializer;
  }

  /**
   * Marks this exception as raised by the initializing expression of a record field.
   * @return self reference for convenience
   */
  public final QueryException initializer() {
    if(this != ERROR) initializer = true;
    return this;
  }

  /**
   * Returns a copy of this exception with an empty stack.
   * @return copy
   */
  public final QueryException copy() {
    return new QueryException(this);
  }

  /**
   * Returns variables for a new catch expression.
   * @param qc query context
   * @param info input info
   * @return variables
   */
  public static Var[] variables(final QueryContext qc, final InputInfo info) {
    final RecordType record = Records.ERROR_DETAILS.get();
    final TokenObjectMap<ShapeField> fields = record.fields();
    final int fs = fields.size();
    final Var[] vars = new Var[fs + 1];
    for(int f = 1; f <= fs; f++) {
      final QNm name = new QNm(ERR_PREFIX, fields.key(f), ERROR_URI);
      vars[f - 1] = new Var(name, fields.value(f).seqType(), qc, info);
    }
    vars[fs] = new Var(new QNm(ERR_PREFIX, "map", ERROR_URI), record.seqType(), qc, info);
    return vars;
  }

  /**
   * Returns an array with values for this exception, optionally omitting the location values.
   * @param location include the values from index {@link #LOCATION} onward
   * @return values
   */
  public ValueList values(final boolean location) {
    final ValueList list = new ValueList();
    list.add(qname());
    list.add(Str.get(getLocalizedMessage()));
    list.add(value() != null ? value() : Empty.VALUE);
    list.add(path() != null ? Str.get(path()) : Empty.VALUE);
    if(location) {
      list.add(line() != 0 ? Itr.get(line()) : Empty.VALUE);
      list.add(column() != 0 ? Itr.get(column()) : Empty.VALUE);
      final TokenBuilder trace = new TokenBuilder();
      if(info != null) trace.add(location(info)).add('\n');
      for(final InputInfo ii : stack) trace.add(location(ii)).add('\n');
      list.add(Str.get(trace.finish()));
      list.add(Empty.VALUE);
      list.add(XQMap.get(Records.ERROR_DETAILS.get(), list.toArray()));
    }
    return list;
  }

  /**
   * Returns a map with values for this exception. This is the same map that a try/catch clause
   * exposes as {@code $err:map} (the last entry of the value list).
   * @return values
   */
  public XQMap map() {
    return (XQMap) values(true).peek();
  }

  /**
   * Returns an error location, prefixed by the enclosing declaration.
   * @param ii input info
   * @return location
   */
  private static String location(final InputInfo ii) {
    final String decl = ii.decl();
    return decl == null ? ii.toString() : Strings.concat(decl, " (", ii, ')');
  }

  /**
   * Creates the error message from the specified text and extension array.
   * @param text text message with optional placeholders
   * @param ext info extensions
   * @param info input info (can be {@code null})
   * @return error message
   */
  private static String message(final String text, final Object[] ext, final InputInfo info) {
    final TokenList list = new TokenList(ext.length);
    for(final Object e : ext) {
      list.add(normalize(e instanceof final ExprInfo ei ? ei.toErrorString() : e, info));
    }
    return Util.info(text, (Object[]) list.finish());
  }
}
