package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Built-in functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function definition. */
  public FuncDefinition definition;

  /**
   * Constructor.
   */
  protected StandardFunc() {
    super(null, Types.ITEM_ZM);
  }

  /**
   * Initializes the function.
   * @param ii input info (can be {@code null})
   * @param df function definition
   * @param args function arguments
   */
  final void init(final InputInfo ii, final FuncDefinition df, final Expr[] args) {
    info = ii;
    definition = df;
    exprs = args;
    exprType.assign(df.seqType);
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    checkPerm(cc.qc, definition.perm);
    simplifyArgs(cc);

    // apply custom optimizations
    final Expr expr = opt(cc);
    if(expr != this) return cc.replaceWith(this, expr);

    // pre-evaluate if arguments are values and not too large
    final SeqType st = definition.seqType;
    return values(st.occ.max > 1 || st.type instanceof FType, cc) && isSimple() ?
      cc.preEval(this) : this;
  }

  /**
   * Simplifies the types of all arguments. This function is overwritten by functions that
   * rely on the original argument type.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    final int al = args().length;
    for(int a = 0; a < al; a++) {
      // consider variable-size parameters
      final int p = Math.min(a, definition.types.length - 1);
      final Type type = definition.types[p].type;
      if(type.instanceOf(BasicType.ANY_ATOMIC_TYPE)) {
        final Simplify mode = type.instanceOf(BasicType.NUMERIC) ? Simplify.NUMBER :
            type.instanceOf(BasicType.STRING) ? Simplify.STRING : Simplify.DATA;
        arg(a, arg -> arg.simplifyFor(mode, cc));
      }
    }
  }

  /**
   * Performs function-specific optimizations.
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Expr opt(final CompileContext cc) throws QueryException {
    return this;
  }

  @Override
  public final StandardFunc copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(definition.get(info, copyAll(cc, vm, args())));
  }

  /**
   * Optimizes a function that returns an empty sequence when the first atomized argument is empty,
   * and adjusts the occurrence indicator if the argument will always yield one item.
   * @return original or optimized expression
   */
  protected final Expr optFirst() {
    return optFirst(true, true, null);
  }

  /**
   * Optimizes a function that returns an empty sequence when the first argument or the
   * context value is empty.
   * <ul>
   *   <li> Returns the first argument (or the context) if it yields an empty sequence.</li>
   *   <li> Sets the occurrence indicator to 1 if the argument returns at least one item.</li>
   * </ul>
   * @param occ assign occurrence indicator
   *   ({@code true} if function will always yield one result if first argument is non-empty)
   * @param atom argument will be atomized
   * @param value context value (ignored if {@code null})
   * @return original or optimized expression
   */
  protected final Expr optFirst(final boolean occ, final boolean atom, final Value value) {
    final Expr expr = defined(0) ? arg(0) : value;
    if(expr != null) {
      final SeqType st = expr.seqType();
      if(st.zero()) return expr instanceof Dummy ? Empty.VALUE : expr;
      if(occ && st.oneOrMore() && !(atom && st.mayBeFunction()) && exprType.seqType().zeroOrOne()) {
        exprType.assign(Occ.EXACTLY_ONE);
      }
    }
    return this;
  }

  /**
   * Serializes the data from the specified iterator.
   * @param iter data to serialize
   * @param sopts serialization parameters
   * @param err error to raise
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  protected final byte[] serialize(final Iter iter, final SerializerOptions sopts,
      final QueryError err, final QueryContext qc) throws QueryException {

    try {
      final ArrayOutput ao = new ArrayOutput();
      try(Serializer ser = Serializer.get(ao, sopts)) {
        for(Item item; (item = qc.next(iter)) != null;) {
          ser.serialize(item);
        }
      }
      return new TokenBuilder(ao.finish()).normalize().finish();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      throw err.get(info, ex);
    }
  }

  @Override
  public final boolean has(final Flag... flags) {
    for(final Flag flag : flags) {
      switch(flag) {
        case HOF:
          if(hofOffsets() > 0) return true;
          continue;
        case UPD:
          if(hasUPD()) return true;
          break;
        case CTX:
          if(hasCTX()) return true;
          break;
        case NDT:
          // check whether function arguments may contain non-deterministic code
          final int hof = hofOffsets(), al = args().length;
          for(int a = 0; a < al; a++) {
            if((hof & (1 << a)) != 0 && (!(arg(a) instanceof final Item item) ||
                !(item instanceof final FuncItem fi) || fi.expr.has(Flag.NDT))) return true;
          }
          break;
        default:
      }
      if(definition.has(flag)) return true;
    }
    // check arguments (without function invocation; it only applies to the function itself)
    return super.has(Flag.remove(flags, Flag.HOF));
  }

  /**
   * Indicates if this function is updating.
   * @return result of check
   */
  public boolean hasUPD() {
    // mix updates: higher-order function may be updating
    return definition.has(Flag.UPD) || sc().mixUpdates && hofOffsets() > 0;
  }

  /**
   * Indicates if this function relies on the context.
   * @return result of check
   */
  public boolean hasCTX() {
    return definition.has(Flag.CTX);
  }

  /**
   * Returns the offsets to possible higher-order function arguments.
   * Used to assess further properties, e.g. if a function is nondeterministic.
   * @return bit offsets to function arguments (1: first argument, 2: second argument, 4: ...),
   *   {@code 0} if no HOF parameter exists, or
   *   {@code Integer#MAX_VALUE} if the functions cannot be accessed via offsets
   */
  protected int hofOffsets() {
    if(definition.has(Flag.HOF)) return Integer.MAX_VALUE;
    int bits = 0;
    final int tl = definition.types.length;
    for(int t = 0; t < tl; t++) {
      if(definition.types[t].type instanceof FuncType) bits |= hofOffset(t);
    }
    return bits;
  }

  /**
   * Returns a higher-order bit offset for the specified argument if it is present.
   * @param i index of argument
   * @return bit offset or {@code 0}
   * @see #hofOffsets
   */
  protected final int hofOffset(final int i) {
    return defined(i) ? 1 << i : 0;
  }

  @Override
  public boolean vacuous() {
    return size() == 0 && !has(Flag.UPD);
  }

  /**
   * Returns a coerced version of a function item argument.
   * @param i index of argument
   * @param cc compilation context
   * @return coerced argument
   * @throws QueryException query exception
   */
  public final Expr coerceFunc(final int i, final CompileContext cc) throws QueryException {
    return coerceFunc(i, cc, -1);
  }

  /**
   * Returns a coerced version of a function item argument.
   * @param i index of function argument
   * @param cc compilation context
   * @param arity arity of target function (ignored if {@code -1})
   * @return coerced argument
   * @throws QueryException query exception
   */
  public final Expr coerceFunc(final int i, final CompileContext cc, final int arity)
      throws QueryException {

    FuncType ft = (FuncType) definition.types[i].type;
    if(arity != -1 && arity != ft.argTypes.length) ft = ft.with(arity);
    return new TypeCheck(info, arg(i), ft.seqType()).optimize(cc);
  }

  /**
   * Creates a new function item with refined types.
   * @param expr expression to refine
   * @param cc compilation context
   * @param argTypes required argument types
   * @return original expression or refined function item
   * @throws QueryException query context
   */
  public static Expr refineFunc(final Expr expr, final CompileContext cc, final SeqType... argTypes)
      throws QueryException {
    return expr instanceof final FuncItem fi ? fi.refine(argTypes, cc) : expr;
  }

  /**
   * Returns the arity of a function expression.
   * @param expr function
   * @return arity, or {@code -1} if unknown
   */
  public static int arity(final Expr expr) {
    final FuncType ft = expr.funcType();
    if(ft != null) {
      final SeqType[] at = ft.argTypes;
      if(at != null) return at.length;
    }
    return -1;
  }

  /**
   * Opens a database at compile time.
   * @param cc compilation context
   * @return self reference
   * @throws QueryException query exception
   */
  protected final Expr compileData(final CompileContext cc) throws QueryException {
    if(cc.dynamic && defined(0) && arg(0) instanceof Value) {
      final Data data = toData(cc.qc);
      exprType.data(data);
      cc.info(OPTOPEN_X, data.meta.name);
    }
    return this;
  }

  /**
   * Tries to embed a positional function call in the input argument.
   * @param cc compilation context
   * @param skip skip evaluation of remaining operands
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  protected Expr embed(final CompileContext cc, final boolean skip) throws QueryException {
    // head($nodes ! name()) → head($nodes) ! name()
    // foot((1 to 8) ! <_>{ . }</_>) → foot((1 to 8)) ! <_>{ . }</_>
    // do not rewrite positional access:  foot($nodes ! position())
    if(arg(0) instanceof SimpleMap) {
      final Expr[] ops = arg(0).args();
      if(((Checks<Expr>) op -> op == ops[0] || op.seqType().one() && !op.has(Flag.POS)).all(ops)) {
        final Expr[] args = new ExprList().add(args()).set(0, ops[0]).finish();
        final Expr fn = definition.get(info, args).optimize(cc);
        return skip ? fn : SimpleMap.get(cc, info, new ExprList(ops.clone()).set(0, fn).finish());
      }
    }
    return this;
  }

  /**
   * Converts an item to a date.
   * @param item item
   * @param qc query context
   * @return date
   * @throws QueryException query exception
   */
  protected final ADate toGregorian(final Item item, final QueryContext qc)
      throws QueryException {
    return (ADate) Types.GREGORIAN_ZO.coerce(item, null, qc, null, info);
  }

  /**
   * Converts an item to a date of the specified type.
   * @param item item
   * @param type expected type
   * @param qc query context
   * @return date
   * @throws QueryException query exception
   */
  protected final ADate toDate(final Item item, final BasicType type, final QueryContext qc)
      throws QueryException {
    return (ADate) (item.type.isUntyped() ? type.cast(item, qc, info) : checkType(item, type));
  }

  /**
   * Converts an item to a database node.
   * @param item item
   * @param mainmem accept main-memory database nodes
   * @return database node
   * @throws QueryException query exception
   */
  protected final DBNode toDBNode(final Item item, final boolean mainmem) throws QueryException {
    if(item instanceof final DBNode node && (mainmem || !node.data().inMemory())) return node;
    throw DB_NODE_X.get(info, item);
  }

  /**
   * Evaluates an expression to a token contained in an {@link AStr} instance.
   * @param expr expression
   * @param qc query context
   * @return {@link AStr} instance
   * @throws QueryException query exception
   */
  protected final AStr toStr(final Expr expr, final QueryContext qc) throws QueryException {
    final Item value = expr.atomItem(qc, info);
    return value instanceof final AStr str ? str : Str.get(toToken(value));
  }

  /**
   * Evaluates an expression to a token contained in an {@link AStr} instance.
   * @param expr expression
   * @param qc query context
   * @return {@link AStr} instance (zero-length if result is an empty sequence)
   * @throws QueryException query exception
   */
  protected final AStr toZeroStr(final Expr expr, final QueryContext qc) throws QueryException {
    final Item value = expr.atomItem(qc, info);
    return value.isEmpty() ? Str.EMPTY : value instanceof final AStr str ? str :
      Str.get(toToken(value));
  }

  /**
   * Evaluates an expression to a map.
   * @param expr expression
   * @param qc query context
   * @return map (empty map if the expression yields an empty sequence)
   * @throws QueryException query exception
   */
  protected final XQMap toEmptyMap(final Expr expr, final QueryContext qc) throws QueryException {
    final Item item = expr.item(qc, info);
    return item.isEmpty() ? XQMap.empty() : toMap(item);
  }

  /**
   * Checks if the specified item is a Duration item. If it is untyped, a duration is returned.
   * @param item item to be checked
   * @return duration
   * @throws QueryException query exception
   */
  protected final Dur toDur(final Item item) throws QueryException {
    if(item instanceof final Dur dur) return dur;
    if(item.type.isUntyped()) return new Dur(item.string(info), info);
    throw typeError(item, BasicType.DURATION, info);
  }

  /**
   * Evaluates an expression to a collation.
   * @param expr expression
   * @param qc query context
   * @return collation, or {@code null} for default collation
   * @throws QueryException query exception
   */
  protected final Collation toCollation(final Expr expr, final QueryContext qc)
      throws QueryException {
    return toCollation(toTokenOrNull(expr, qc), qc);
  }

  /**
   * Evaluates an item to a collation.
   * @param collation collation URI or {@code null}
   * @param qc query context
   * @return collation, or {@code null} for default collation
   * @throws QueryException query exception
   */
  protected final Collation toCollation(final byte[] collation, final QueryContext qc)
      throws QueryException {
    return Collation.get(collation, qc, info, WHICHCOLL_X);
  }

  /**
   * Evaluates an expression to a file path.
   * @param expr expression
   * @param qc query context
   * @return file path
   * @throws QueryException query exception
   */
  protected final Path toPath(final Expr expr, final QueryContext qc) throws QueryException {
    return toPath(toString(expr, qc));
  }

  /**
   * Converts a path to a file path.
   * @param path path string
   * @return file path
   * @throws QueryException query exception
   */
  protected final Path toPath(final String path) throws QueryException {
    try {
      return path.startsWith(IO.FILEPREF) ? Paths.get(new URI(path)) : Paths.get(path);
    } catch(final IllegalArgumentException | URISyntaxException ex) {
      Util.debug(ex);
      throw FILE_INVALID_PATH_X.get(info, path);
    }
  }

  /**
   * Evaluates an expression to a reference to an existing input resource.
   * @param expr expression
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IO toIO(final Expr expr, final QueryContext qc) throws QueryException {
    return toIO(toString(expr, qc), false);
  }

  /**
   * Returns a reference to an existing input resource.
   * @param uri URI string
   * @param content allow string content
   * @return IO reference
   * @throws QueryException query exception
   */
  protected final IO toIO(final String uri, final boolean content) throws QueryException {
    final IO io = sc().resolve(uri);
    if(io instanceof IOContent) {
      if(!content) throw RESURI_X.get(info, uri);
    } else {
      if(Strings.contains(io.path(), '#')) throw RESFRAG_X.get(info, io);
      if(io instanceof IOFile && io.isDir()) throw RESDIR_X.get(info, io);
      if(!io.exists()) throw RESWHICH_X.get(info, io);
      if(!Uri.get(uri).isValid()) throw RESURI_X.get(info, uri);
    }
    return io;
  }

  /**
   * Evaluates an expression to an input resource.
   * @param expr expression (xs:anyURI with URI or xs:string with content)
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IOContent toContent(final Expr expr, final QueryContext qc)
      throws QueryException {
    final Item item = toAtomItem(expr, qc);
    return item instanceof Uri ? toContent(string(item.string(info)), qc) :
      new IOContent(toToken(item));
  }

  /**
   * Returns an input resource.
   * @param source source
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IOContent toContent(final String source, final QueryContext qc)
      throws QueryException {
    checkPerm(qc, Perm.ADMIN);
    final IO io = toIO(source, false);
    try {
      return new IOContent(io.readString(), io.url());
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Returns a base URI for the given path and the associated option.
   * @param path custom path (can be {@code null})
   * @param options options
   * @param option base-uri option
   * @return base URI
   */
  protected final String toBaseUri(final String path, final Options options,
      final StringOption option) {
    final String base = options.get(option);
    return base != null && !base.isEmpty() ? base :
      path != null && !path.isEmpty() ? path : string(sc().baseURI().string());
  }

  /**
   * Evaluates an expression to an encoding string.
   * @param expr expression (can be empty)
   * @param err error to raise
   * @param qc query context
   * @return normalized encoding string or {@code null}
   * @throws QueryException query exception
   */
  protected final String toEncodingOrNull(final Expr expr, final QueryError err,
      final QueryContext qc) throws QueryException {
    return toEncodingOrNull(toStringOrNull(expr, qc), err);
  }

  /**
   * Evaluates an expression to an encoding string.
   * @param encoding encoding (can be {@code null})
   * @param err error to raise
   * @return normalized encoding string or {@code null}
   * @throws QueryException query exception
   */
  protected final String toEncodingOrNull(final String encoding, final QueryError err)
      throws QueryException {

    if(encoding == null) return null;
    final String error = Strings.checkEncoding(encoding);
    if(error != null) throw err.get(info, error);
    return Strings.normEncoding(encoding, false);
  }

  /**
   * Converts an item to a node or an atomized item.
   * @param expr expression
   * @param empty allow empty item
   * @param qc query context
   * @return node, atomized item or {@code null}
   * @throws QueryException query exception
   */
  protected final Item toNodeOrAtomItem(final Expr expr, final boolean empty, final QueryContext qc)
      throws QueryException {
    Item item = expr.item(qc, info);
    if(!(item instanceof XNode)) {
      item = item.atomItem(qc, info);
      if(item.isEmpty()) {
        if(empty) return null;
        throw typeError(item, BasicType.ITEM, info);
      }
    }
    return item;
  }

  /**
   * Evaluates an expression and returns serialization parameters.
   * Constructor for serialization functions.
   * @param expr expression (can be empty)
   * @param qc query context
   * @return serialization parameters
   * @throws QueryException query exception
   */
  protected final SerializerOptions toSerializerOptions(final Expr expr, final QueryContext qc)
      throws QueryException {

    final SerializerOptions options = new SerializerOptions();
    options.set(SerializerOptions.METHOD, SerialMethod.XML);

    final Item item = expr.item(qc, info);
    if(item instanceof final XQMap map) {
      options.assign(map, info);
    } else if(!item.isEmpty()) {
      options.assign(item, info);
    }
    return options;
  }

  /**
   * Evaluates an expression to a map with string keys and values.
   * @param expr expression (can be empty)
   * @param qc query context
   * @return user options
   * @throws QueryException query exception
   */
  protected final HashMap<String, String> toOptions(final Expr expr, final QueryContext qc)
      throws QueryException {
    return toOptions(expr, new Options(), qc).free();
  }

  /**
   * Evaluates an expression and returns options.
   * @param <E> options type
   * @param expr expression (can be empty)
   * @param options options template
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  protected final <E extends Options> E toOptions(final Expr expr, final E options,
      final QueryContext qc) throws QueryException {
    options.assign(toEmptyMap(expr, qc), info);
    return options;
  }

  /**
   * Evaluates an expression to variable bindings.
   * @param expr expression (can be empty)
   * @param qc query context
   * @return variable bindings
   * @throws QueryException query exception
   */
  protected final HashMap<String, Value> toBindings(final Expr expr, final QueryContext qc)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<>();
    toEmptyMap(expr, qc).forEach((key, value) -> {
      final byte[] k = key.type.isStringOrUntyped() ? key.string(info) : toQNm(key).unique();
      hm.put(string(k), value);
    });
    return hm;
  }

  /**
   * Evaluates the first expression to a database instance.
   * @param qc query context
   * @return database instance
   * @throws QueryException query exception
   */
  protected final Data toData(final QueryContext qc) throws QueryException {
    final Data data = exprType.data();
    return data != null ? data : toData(toName(arg(0), false, DB_NAME_X, qc), qc);
  }

  /**
   * Evaluates an expression to a name.
   * @param expr expression
   * @param empty accept empty names
   * @param error error to raise
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toName(final Expr expr, final boolean empty, final QueryError error,
      final QueryContext qc) throws QueryException {
    final String name = toZeroString(expr, qc);
    if(empty && name.isEmpty() || Databases.validName(name)) return name;
    throw error.get(info, name);
  }

  /**
   * Evaluates an expression to a number of milliseconds.
   * @param expr expression
   * @param qc query context
   * @return number of milliseconds
   * @throws QueryException query exception
   */
  protected final long toMs(final Expr expr, final QueryContext qc) throws QueryException {
    final Dtm dtm = (Dtm) checkType(expr, BasicType.DATE_TIME, qc);
    if(dtm.yea() > 292278993) throw INTRANGE_X.get(info, dtm.yea());
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
  }

  /**
   * Returns a database instance.
   * @param name name of database
   * @param qc query context
   * @return database instance
   * @throws QueryException query exception
   */
  protected final Data toData(final String name, final QueryContext qc) throws QueryException {
    return qc.resources.database(name, qc.user, definition.has(Flag.UPD), info);
  }

  /**
   * Evaluates an expression to a non-updating function item or {@code null}.
   * @param expr expression
   * @param nargs maximum number of supplied arguments
   * @param qc query context
   * @return function item or {@code null}
   * @throws QueryException query exception
   */
  protected final FItem toFunctionOrNull(final Expr expr, final int nargs, final QueryContext qc)
      throws QueryException {
    final Item item = expr.item(qc, info);
    return item.isEmpty() ? null : checkArity(toFunction(item, qc), nargs, false);
  }

  /**
   * Evaluates an expression to a non-updating function item.
   * @param expr expression
   * @param nargs maximum number of supplied arguments
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final int nargs, final QueryContext qc)
      throws QueryException {
    return toFunction(expr, nargs, false, qc);
  }

  /**
   * Evaluates an expression to a function item.
   * @param expr expression
   * @param nargs maximum number of supplied arguments
   * @param qc query context
   * @param updating updating flag
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final int nargs, final boolean updating,
      final QueryContext qc) throws QueryException {
    return checkArity(toFunction(expr, qc), nargs, updating);
  }

  /**
   * Evaluates an expression to a function item.
   * @param function function
   * @param nargs maximum number of supplied arguments
   * @param updating updating flag
   * @return function item
   * @throws QueryException query exception
   */
  private FItem checkArity(final FItem function, final int nargs, final boolean updating)
      throws QueryException {

    checkUp(function, updating);
    final int arity = function.arity();
    if(nargs < arity) throw arityError(function, arity, nargs, true, info);
    return function;
  }

  /**
   * Returns the boolean result of a higher-order function invocation.
   * @param predicate function to be invoked
   * @param args higher-order function arguments
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  protected final boolean test(final FItem predicate, final HofArgs args,
      final QueryContext qc) throws QueryException {
    final Item item = invoke(predicate, args, qc).atomItem(qc, info);
    return item != Empty.VALUE && toBoolean(item);
  }

  /**
   * Invokes a higher-order function.
   * @param function function to be invoked
   * @param args higher-order function arguments
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  protected final Value invoke(final FItem function, final HofArgs args, final QueryContext qc)
      throws QueryException {
    return function.invoke(qc, info, args.get());
  }

  /**
   * Indicates if the supplied argument is defined.
   * @param i index of argument
   * @return result of check
   */
  protected final boolean defined(final int i) {
    return arg(i) != Empty.UNDEFINED;
  }

  /**
   * Tries to lock a database supplied by the specified argument.
   * @param expr expression
   * @param backup backup flag
   * @param visitor visitor
   * @return result of check
   */
  protected final boolean dataLock(final Expr expr, final boolean backup,
      final ASTVisitor visitor) {
    return visitor.lock(() -> {
      final ArrayList<String> list = new ArrayList<>(1);
      String name = expr instanceof final Str str ? string(str.string()) :
        expr instanceof final Atm atm ? string(atm.string(info)) : null;
      if(name != null) {
        if(backup) {
          final String db = Databases.name(name);
          if(db.isEmpty()) {
            name = db;
          } else {
            list.add(db);
          }
        }
        if(name.isEmpty()) name = null;
      }
      list.add(name);
      return list;
    });
  }

  /**
   * Indicates if the supplied options argument may contain a function.
   * @param i index of argument
   * @return result of check
   */
  protected final boolean functionOption(final int i) {
    if(!(arg(i) instanceof final Value value)) return true;
    if(value instanceof final XQMap map) {
      for(final Item key : map.keys()) {
        try {
          if(map.get(key) instanceof FItem) return true;
        } catch(final QueryException ex) {
          Util.debug(ex);
        }
      }
    }
    return false;
  }

  /**
   * Returns the original exception, or a new exception for the specified error.
   * @param ex original exception
   * @param error error adapted error (ignored if {@code null})
   * @return new exception
   */
  protected final QueryException error(final QueryException ex, final QueryError error) {
    if(error == null) return ex;
    Util.debug(ex);
    return error.get(info, ex.getLocalizedMessage());
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final StandardFunc sf && definition == sf.definition &&
        super.equals(obj);
  }

  @Override
  public final String description() {
    return definition.toString();
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    final byte[] name = definition.name.prefixId(FN_URI);
    final int undefined = undefined();
    if(undefined == 0) {
      plan.add(plan.create(this, NAME, name), args());
    } else {
      final int al = args().length;
      final QNm[] names = definition.paramNames(al);
      final ExprList args = new ExprList(al - undefined);
      final StringList nms = new StringList(al - undefined);
      for(int a = 0; a < al; a++) {
        if(defined(a)) {
          args.add(arg(a));
          nms.add(names[a].toString());
        }
      }
      plan.add(plan.create(this, NAME, name, ARG, String.join(", ", nms.finish())), args.finish());
    }
  }

  @Override
  public final void toString(final QueryString qs) {
    final byte[] name = definition.name.prefixId(FN_URI);
    final int undefined = undefined();
    if(undefined == 0) {
      qs.token(name).params(args());
    } else {
      final int al = args().length;
      final QNm[] names = definition.paramNames(al);
      final Object[] args = new Object[al - undefined];
      boolean gap = false;
      for(int a = 0, b = 0; a < al; a++) {
        if(defined(a)) {
          args[b++] = gap ? names[a] + " := " + arg(a) : arg(a);
        } else {
          gap = true;
        }
      }
      qs.token(name).params(args);
    }
  }

  /**
   * Returns the number of undefined arguments.
   * @return count
   */
  private int undefined() {
    int c = 0;
    final int al = args().length;
    for(int a = 0; a < al; a++) {
      if(!defined(a)) c++;
    }
    return c;
  }
}
