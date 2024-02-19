package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
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
import org.basex.util.similarity.*;

/**
 * Built-in functions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function definition. */
  public FuncDefinition definition;
  /** Static context. */
  public StaticContext sc;

  /**
   * Constructor.
   */
  protected StandardFunc() {
    super(null, SeqType.ITEM_ZM);
  }

  /**
   * Initializes the function.
   * @param ii input info (can be {@code null})
   * @param sctx static context
   * @param df function definition
   * @param args function arguments
   */
  final void init(final StaticContext sctx, final InputInfo ii, final FuncDefinition df,
      final Expr[] args) {
    sc = sctx;
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
    return allAreValues(st.occ.max > 1 || st.type instanceof FuncType) && isSimple()
        ? cc.preEval(this) : this;
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
      if(type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
        final Simplify mode = type.instanceOf(AtomType.NUMERIC) ? Simplify.NUMBER : Simplify.STRING;
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
  public final StandardFunc copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(definition.get(sc, info, copyAll(cc, vm, args())));
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
   *   ({@code true} if function will always yield a result if first argument is non-empty)
   * @param atom argument will be atomized
   * @param value context value (ignored if {@code null})
   * @return original or optimized expression
   */
  protected final Expr optFirst(final boolean occ, final boolean atom, final Value value) {
    final Expr expr = defined(0) ? arg(0) : value;
    if(expr != null) {
      final SeqType st = expr.seqType();
      if(st.zero()) return expr;
      if(occ && st.oneOrMore() && !(atom && st.mayBeArray())) exprType.assign(Occ.EXACTLY_ONE);
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
    // check function
    int hof = hofIndex();
    if(hof >= 0 && hof < Integer.MIN_VALUE && !defined(hof)) hof = -1;
    for(final Flag flag : flags) {
      switch(flag) {
        case UPD:
          if(hasUPD()) return true;
          break;
        case CTX:
          if(hasCTX()) return true;
          break;
        case HOF:
          if(hof >= 0) return true;
          break;
        case NDT:
          // check whether function argument may contain non-deterministic functions
          if(hof == Integer.MIN_VALUE) return true;
          if(hof >= 0) {
            if(!(arg(hof) instanceof Value)) return true;
            for(final Item item : (Value) arg(hof)) {
              if(!(item instanceof FuncItem) || ((FuncItem) item).expr.has(Flag.NDT)) return true;
            }
          }
          break;
        default:
      }
      if(definition.has(flag)) return true;
    }
    // check arguments (without function invocation; it only applies to function itself)
    final Flag[] flgs = Flag.HOF.remove(flags);
    return flgs.length != 0 && super.has(flgs);
  }

  /**
   * Indicates if this function is updating.
   * @return result of check
   */
  public boolean hasUPD() {
    // mix updates: higher-order function may be updating
    return definition.has(Flag.UPD) || sc.mixUpdates && hofIndex() >= 0;
  }

  /**
   * Indicates if this function relies on the context.
   * @return result of check
   */
  public boolean hasCTX() {
    return definition.has(Flag.CTX);
  }

  /**
   * Returns the index of a single higher-order function parameter.
   * @return index, {@code -1} if no HOF parameter exist, or {@code Integer#MAX_VALUE} if the
   *   number cannot be returned or if multiple HOF parameters exist
   */
  public int hofIndex() {
    return definition.has(Flag.HOF) ? Integer.MAX_VALUE : -1;
  }

  @Override
  public boolean vacuous() {
    return size() == 0 && !has(Flag.UPD);
  }

  /**
   * Returns a coerced function item argument.
   * @param i index of argument
   * @param cc compilation context
   * @return coerced argument
   * @throws QueryException query exception
   */
  public final Expr coerce(final int i, final CompileContext cc) throws QueryException {
    return coerce(i, cc, -1);
  }

  /**
   * Returns a coerced function item argument.
   * @param i index of function argument
   * @param cc compilation context
   * @param arity arity of target function (ignored if {@code -1})
   * @return coerced argument
   * @throws QueryException query exception
   */
  public final Expr coerce(final int i, final CompileContext cc, final int arity)
      throws QueryException {

    FuncType ft = (FuncType) definition.types[i].type;
    if(arity != -1 && arity != ft.argTypes.length) ft = ft.with(arity);
    return new TypeCheck(info, sc, arg(i), ft.seqType(), true).optimize(cc);
  }

  /**
   * Refines the type of a function item argument.
   * @param expr function
   * @param cc compilation context
   * @param declType declared return type
   * @param argTypes argument types
   * @return old or new expression
   * @throws QueryException query context
   */
  public final Expr refineFunc(final Expr expr, final CompileContext cc, final SeqType declType,
      final SeqType... argTypes) throws QueryException {

    // check if argument is function item
    if(!(expr instanceof FuncItem)) return expr;

    // check number of arguments
    final FuncItem func = (FuncItem) expr;
    final int nargs = argTypes.length, arity = func.arity();
    if(arity > nargs) return expr;

    // select most specific argument and return types
    final FuncType oldType = func.funcType();
    final SeqType[] oldArgTypes = oldType.argTypes, newArgTypes = new SeqType[arity];
    for(int a = 0; a < arity; a++) {
      newArgTypes[a] = argTypes[a].instanceOf(oldArgTypes[a]) ? argTypes[a] : oldArgTypes[a];
    }
    final SeqType newDecl = declType.instanceOf(oldType.declType) ? declType : oldType.declType;
    final FuncType newType = FuncType.get(newDecl, newArgTypes);

    // new type is more specific: coerce to new function type
    return !newType.eq(oldType) ? func.coerceTo(newType, cc.qc, info, true) : expr;
  }

  /**
   * Returns the arity of a function expression.
   * @param expr function
   * @return arity, or {@code -1} if unknown
   */
  public int arity(final Expr expr) {
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
    // head($nodes ! name())  ->  head($nodes) ! name()
    // foot((1 to 8) ! <_>{ . }</_>)  ->  foot((1 to 8)) ! <_>{ . }</_>
    // do not rewrite positional access:  foot($nodes ! position())
    if(arg(0) instanceof SimpleMap) {
      final Expr[] ops = arg(0).args();
      if(((Checks<Expr>) op -> op == ops[0] || op.seqType().one() && !op.has(Flag.POS)).all(ops)) {
        final Expr[] args = new ExprList(args().clone()).set(0, ops[0]).finish();
        final Expr fn = definition.get(sc, info, args).optimize(cc);
        return skip ? fn : SimpleMap.get(cc, info, new ExprList(ops.clone()).set(0, fn).finish());
      }
    }
    return this;
  }

  /**
   * Converts an item to a date of the specified type.
   * @param item item
   * @param type expected type
   * @param qc query context
   * @return date
   * @throws QueryException query exception
   */
  protected final ADate toDate(final Item item, final AtomType type, final QueryContext qc)
      throws QueryException {
    return (ADate) (item.type.isUntyped() ? type.cast(item, qc, sc, info) : checkType(item, type));
  }

  /**
   * Converts an item to a database node.
   * @param item item
   * @param mainmem accept main-memory database nodes
   * @return database node
   * @throws QueryException query exception
   */
  protected final DBNode toDBNode(final Item item, final boolean mainmem) throws QueryException {
    if(item instanceof DBNode && (mainmem || !item.data().inMemory())) return (DBNode) item;
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
    return value instanceof AStr ? (AStr) value : Str.get(toToken(value));
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
    return value.isEmpty() ? Str.EMPTY : value instanceof AStr ? (AStr) value :
      Str.get(toToken(value));
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
    return Collation.get(toTokenOrNull(expr, qc), qc, sc, info, WHICHCOLL_X);
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
    return toIO(toString(expr, qc));
  }

  /**
   * Returns a reference to an existing input resource.
   * @param uri URI string
   * @return io reference
   * @throws QueryException query exception
   */
  protected final IO toIO(final String uri) throws QueryException {
    final IO io = sc.resolve(uri);
    if(!io.exists()) throw WHICHRES_X.get(info, io);
    if(io instanceof IOFile && io.isDir()) throw RESDIR_X.get(info, io);
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
   * @param uri URI
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IOContent toContent(final String uri, final QueryContext qc)
      throws QueryException {
    checkPerm(qc, Perm.ADMIN);
    final IO io = toIO(uri);
    try {
      return new IOContent(io.string(), io.url());
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
      path != null && !path.isEmpty() ? path : string(sc.baseURI().string());
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

    final Item encoding = expr.atomItem(qc, info);
    if(encoding.size() == 0) return null;

    final String enc = toString(encoding);
    try {
      if(Charset.isSupported(enc)) return Strings.normEncoding(enc);
    } catch(final IllegalArgumentException ex) {
      // character set is invalid or unknown (e.g. empty string)
      Util.debug(ex);
    }
    throw err.get(info, QueryError.similar(enc,
        Levenshtein.similar(token(enc), Strings.encodings())));
  }

  /**
   * Converts an item to a node or an atomized item.
   * @param expr expression
   * @param qc query context
   * @return node or atomized item
   * @throws QueryException query exception
   */
  protected final Item toNodeOrAtomItem(final Expr expr, final QueryContext qc)
      throws QueryException {
    Item item = expr.item(qc, info);
    if(!(item instanceof ANode)) item = item.atomItem(qc, info);
    if(item.isEmpty()) throw EMPTYFOUND.get(info);
    return item;
  }

  /**
   * Evaluates an expression to user options.
   * @param expr expression (can be empty)
   * @param qc query context
   * @return user options
   * @throws QueryException query exception
   */
  protected final HashMap<String, String> toOptions(final Expr expr, final QueryContext qc)
      throws QueryException {
    return toOptions(expr, new Options(), false, qc).free();
  }

  /**
   * Evaluates an expression, if it exists, and returns options.
   * @param <E> options type
   * @param expr expression (can be empty)
   * @param options options template
   * @param enforce raise error if a supplied option is unknown
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  protected final <E extends Options> E toOptions(final Expr expr, final E options,
      final boolean enforce, final QueryContext qc) throws QueryException {
    return new FuncOptions(info).assign(expr.item(qc, info), options, enforce);
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
    final Item item = expr.item(qc, info);
    final XQMap map = item.isEmpty() ? XQMap.empty() : toMap(item);
    map.apply((key, value) -> {
      final byte[] k = key.type.isStringOrUntyped() ? key.string(info) : toQNm(key).internal();
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
    return data != null ? data : qc.resources.database(toName(arg(0), false, DB_NAME_X, qc), info);
  }

  /**
   * Checks if the current user has given permissions. If negative, an exception is thrown.
   * @param qc query context
   * @param perm permission
   * @throws QueryException query exception
   */
  protected void checkPerm(final QueryContext qc, final Perm perm) throws QueryException {
    if(perm != Perm.NONE && !qc.context.user().has(perm))
      throw BASEX_PERMISSION_X_X.get(info, perm, this);
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
   * @param func function
   * @param nargs maximum number of supplied arguments
   * @param updating updating flag
   * @return function item
   * @throws QueryException query exception
   */
  private FItem checkArity(final FItem func, final int nargs, final boolean updating)
      throws QueryException {

    checkUp(func, updating, sc);
    final int arity = func.arity();
    if(nargs < arity) throw arityError(func, arity, nargs, true, info);
    return func;
  }

  /**
   * Returns the boolean result of a function invocation.
   * @param qc query context
   * @param predicate function to be invoked
   * @param args arguments
   * @return result
   * @throws QueryException query exception
   */
  protected final boolean toBoolean(final QueryContext qc, final FItem predicate,
      final Value... args) throws QueryException {
    return toBoolean(predicate.invoke(qc, info, args).item(qc, info));
  }

  /**
   * Evaluates an expression to a name.
   * @param expr expression
   * @param empty allow empty name
   * @param error error to raise
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toName(final Expr expr, final boolean empty, final QueryError error,
      final QueryContext qc) throws QueryException {
    final String name = toString(expr, qc);
    if(empty && name.length() == 0 || Databases.validName(name)) return name;
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
    final Dtm dtm = (Dtm) checkType(expr, AtomType.DATE_TIME, qc);
    if(dtm.yea() > 292278993) throw INTRANGE_X.get(info, dtm.yea());
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
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
      String name = expr instanceof Str ? string(((Str) expr).string()) :
        expr instanceof Atm ? string(((Atm) expr).string(info)) : null;
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

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof StandardFunc &&
        definition == ((StandardFunc) obj).definition && super.equals(obj);
  }

  @Override
  public final String description() {
    return definition.toString();
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    final int undefined = undefined();
    if(undefined == 0) {
      plan.add(plan.create(this, NAME, definition.id()), args());
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
      plan.add(plan.create(this, NAME, definition.id(), ARG, String.join(", ", nms.finish())),
          args.finish());
    }
  }

  @Override
  public final void toString(final QueryString qs) {
    final int undefined = undefined();
    if(undefined == 0) {
      qs.token(definition.id()).params(args());
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
      qs.token(definition.id()).params(args);
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
