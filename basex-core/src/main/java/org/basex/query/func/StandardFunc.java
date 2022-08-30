package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;
import org.basex.util.similarity.*;

/**
 * Built-in functions.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function definition. */
  public FuncDefinition definition;
  /** Static context. */
  public StaticContext sc;

  /** Data reference (can be {@code null}). */
  private Data data;

  /**
   * Constructor.
   */
  protected StandardFunc() {
    super(null, SeqType.ITEM_ZM);
  }

  /**
   * Initializes the function.
   * @param ii input info
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
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      // consider variable-size parameters
      final int p = Math.min(e, definition.params.length - 1);
      final Type type = definition.params[p].type;
      if(type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) {
        final Simplify mode = type.instanceOf(AtomType.NUMERIC) ? Simplify.NUMBER : Simplify.STRING;
        exprs[e] = exprs[e].simplifyFor(mode, cc);
      }
    }
  }

  /**
   * Performs function specific optimizations.
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
    final int el = exprs.length;
    final Expr[] arg = new Expr[el];
    for(int e = 0; e < el; e++) arg[e] = exprs[e].copy(cc, vm);
    return copyType(definition.get(sc, info, arg));
  }

  /**
   * Optimizes a function that returns an empty sequence when the first atomized argument is empty,
   * and adjusts the occurrence indicator if the argument will always yield one item.
   * @return original expression or function argument
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
   * @return original expression or function argument
   */
  protected final Expr optFirst(final boolean occ, final boolean atom, final Value value) {
    final Expr expr = exprs.length > 0 ? exprs[0] : value;
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
  public boolean has(final Flag... flags) {
    // check function definition
    for(final Flag flag : flags) {
      if(flag == Flag.UPD ? updating() : definition.has(flag)) return true;
    }
    // check arguments (without function invocation; it only applies to function itself)
    final Flag[] flgs = Flag.HOF.remove(flags);
    return flgs.length != 0 && super.has(flgs);
  }

  /**
   * Indicates if this function is updating.
   * @return result of check
   */
  public boolean updating() {
    // mix updates: higher-order function may be updating
    return definition.has(Flag.UPD) || sc.mixUpdates && definition.has(Flag.HOF);
  }

  @Override
  public boolean vacuous() {
    return size() == 0 && !has(Flag.UPD);
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
  public final Expr coerceFunc(final Expr expr, final CompileContext cc, final SeqType declType,
      final SeqType... argTypes) throws QueryException {

    // check if argument is function item
    if(!(expr instanceof FuncItem)) return expr;

    // check number of arguments
    final FuncItem func = (FuncItem) expr;
    final int al = argTypes.length, fargs = func.arity();
    if(fargs != al) return expr;

    // select most specific argument and return types
    final FuncType oldType = func.funcType();
    final SeqType[] oldArgs = oldType.argTypes, newArgs = new SeqType[al];
    for(int a = 0; a < al; a++) {
      newArgs[a] = argTypes[a].instanceOf(oldArgs[a]) ? argTypes[a] : oldArgs[a];
    }
    final SeqType newDecl = declType.instanceOf(oldType.declType) ? declType : oldType.declType;
    final FuncType newType = FuncType.get(newDecl, newArgs);

    // new type is more specific: coerce to new function type
    return !newType.eq(oldType) ? func.coerceTo(newType, cc.qc, info, true) : expr;
  }

  /**
   * Opens a database at compile time.
   * @param cc compilation context
   * @return self reference
   * @throws QueryException query exception
   */
  protected final Expr compileData(final CompileContext cc) throws QueryException {
    if(cc.dynamic && exprs.length > 0 && exprs[0] instanceof Value) {
      data = toData(cc.qc);
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
    // util:last((1 to 8) ! <_>{ . }</_>)  ->  util:last((1 to 8)) ! <_>{ . }</_>
    if(exprs[0] instanceof SimpleMap) {
      final Expr[] ops = exprs[0].args();
      if(((Checks<Expr>) op -> op == ops[0] || op.seqType().one()).all(ops)) {
        exprs[0] = ops[0];
        ops[0] = definition.get(sc, info, exprs).optimize(cc);
        return skip ? ops[0] : SimpleMap.get(cc, info, ops);
      }
    }
    return this;
  }

  /**
   * Returns the difference to the input length in an argument that counts the length
   * of an input expression.
   * @param i index of optional argument
   * @return length, or {@code Long#MIN_VALUE} if the value cannot be statically retrieved.
   */
  protected long countInputDiff(final int i) {
    if(exprs.length > i) {
      final Expr input = exprs[0], end = exprs[i];
      final Predicate<Expr> countInput = e -> Function.COUNT.is(e) && e.arg(0).equals(input);
      // function(E, count(E))  ->  0
      if(countInput.test(end)) return 0;
      // function(E, count(E) - 1)  ->  -1
      if(end instanceof Arith && countInput.test(end.arg(0)) && end.arg(1) instanceof Int) {
        final Calc calc = ((Arith) end).calc;
        final long sum = ((Int) exprs[1]).itr();
        if(calc == Calc.PLUS) return sum;
        if(calc == Calc.MINUS) return -sum;
      }
    }
    return Long.MIN_VALUE;
  }

  @Override
  public final Data data() {
    return data;
  }

  @Override
  public final void data(final Data dt) {
    data = dt;
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
   * @return database node
   * @throws QueryException query exception
   */
  protected final DBNode toDBNode(final Item item) throws QueryException {
    if(checkNoEmpty(item, NodeType.NODE) instanceof DBNode) return (DBNode) item;
    throw DB_NODE_X.get(info, item);
  }

  /**
   * Evaluates an expression to a collation.
   * @param i index of optional argument
   * @param qc query context
   * @return collation, or {@code null} for default collation
   * @throws QueryException query exception
   */
  protected final Collation toCollation(final int i, final QueryContext qc) throws QueryException {
    return toCollation(i, false, qc);
  }

  /**
   * Evaluates an expression to a collation.
   * @param i index of optional argument
   * @param empty allow empty argument
   * @param qc query context
   * @return collation, or {@code null} for default collation
   * @throws QueryException query exception
   */
  protected final Collation toCollation(final int i, final boolean empty, final QueryContext qc)
      throws QueryException {
    byte[] uri = null;
    if(i < exprs.length) {
      final Item item = exprs[i].atomItem(qc, info);
      if(item != Empty.VALUE) uri = toToken(item);
      else if(!empty) checkNoEmpty(item);
    }
    return Collation.get(uri, qc, sc, info, WHICHCOLL_X);
  }

  /**
   * Evaluates an expression to a file path.
   * @param i index of argument
   * @param qc query context
   * @return file path
   * @throws QueryException query exception
   */
  protected final Path toPath(final int i, final QueryContext qc) throws QueryException {
    return toPath(toString(exprs[i], qc));
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
    } catch(final InvalidPathException | URISyntaxException ex) {
      Util.debug(ex);
      throw FILE_INVALID_PATH_X.get(info, path);
    }
  }

  /**
   * Evaluates an expression to a reference to an existing input resource.
   * @param i index of URI argument
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IO toIO(final int i, final QueryContext qc) throws QueryException {
    return toIO(toString(exprs[i], qc));
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
   * @param i index of argument (xs:anyURI with URI or xs:string with content)
   * @param qc query context
   * @return input resource
   * @throws QueryException query exception
   */
  protected final IOContent toContent(final int i, final QueryContext qc) throws QueryException {
    final Item item = toItem(exprs[i], qc);
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
   * @param i index of optional argument
   * @param err error to raise
   * @param qc query context
   * @return normalized encoding string or {@code null}
   * @throws QueryException query exception
   */
  protected final String toEncodingOrNull(final int i, final QueryError err, final QueryContext qc)
      throws QueryException {

    if(i >= exprs.length) return null;
    final String encoding = toString(exprs[i], qc);
    try {
      if(Charset.isSupported(encoding)) return Strings.normEncoding(encoding);
    } catch(final IllegalArgumentException ex) {
      // character set is invalid or unknown (e.g. empty string)
      Util.debug(ex);
    }
    throw err.get(info, QueryError.similar(encoding,
        Levenshtein.similar(token(encoding), Strings.encodings())));
  }

  /**
   * Converts an item to a node or an atomized item.
   * @param i index of argument
   * @param qc query context
   * @return node or atomized item
   * @throws QueryException query exception
   */
  protected final Item toNodeOrAtomItem(final int i, final QueryContext qc) throws QueryException {
    final Item item = toItem(exprs[i], qc);
    return item instanceof ANode ? item : item.atomItem(qc, info);
  }

  /**
   * Evaluates an expression to options.
   * @param <E> options type
   * @param i index of optional argument
   * @param opts options
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  protected final <E extends Options> E toOptions(final int i, final E opts, final QueryContext qc)
      throws QueryException {
    return i < exprs.length ? new FuncOptions(info).assign(exprs[i].item(qc, info), opts) : opts;
  }

  /**
   * Evaluates an expression to variable bindings.
   * @param i index of optional argument
   * @param qc query context
   * @return variable bindings
   * @throws QueryException query exception
   */
  protected final HashMap<String, Value> toBindings(final int i, final QueryContext qc)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<>();
    if(i < exprs.length) {
      final Item item = exprs[i].item(qc, info);
      final XQMap map = item == Empty.VALUE ? XQMap.empty() : toMap(item);
      map.apply((key, value) -> {
        final byte[] k;
        if(key.type.isStringOrUntyped()) {
          k = key.string(null);
        } else {
          final QNm qnm = toQNm(key, false);
          final TokenBuilder tb = new TokenBuilder();
          if(qnm.uri() != null) tb.add('{').add(qnm.uri()).add('}');
          k = tb.add(qnm.local()).finish();
        }
        hm.put(string(k), value);
      });
    }
    return hm;
  }

  /**
   * Evaluates the first expression to a database instance.
   * @param qc query context
   * @return database instance
   * @throws QueryException query exception
   */
  protected final Data toData(final QueryContext qc) throws QueryException {
    return data != null ? data : qc.resources.database(toName(0, false, DB_NAME_X, qc), info);
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
   * Evaluates an expression to a non-updating function item.
   * @param expr expression
   * @param arity required number of arguments (arity)
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final int arity, final QueryContext qc)
      throws QueryException {
    return toFunction(expr, arity, false, qc);
  }

  /**
   * Evaluates an expression to a function item.
   * @param expr expression
   * @param arity required number of arguments (arity)
   * @param qc query context
   * @param updating updating flag
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem toFunction(final Expr expr, final int arity, final boolean updating,
      final QueryContext qc) throws QueryException {

    final FItem func = checkUp(toFunction(expr, qc), updating, sc);
    final int farity = func.arity();
    if(farity == arity) return func;
    throw FUNARITY_X_X.get(info, arguments(farity), arity);
  }

  /**
   * Evaluates an expression to a name.
   * @param i index of argument
   * @param empty allow empty name
   * @param error error to raise
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  protected final String toName(final int i, final boolean empty, final QueryError error,
      final QueryContext qc) throws QueryException {
    final String name = toString(exprs[i], qc);
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
   * Tries to lock a database supplied by the specified argument.
   * @param visitor visitor
   * @param backup backup flag
   * @param i index of argument
   * @return result of check
   */
  protected final boolean dataLock(final ASTVisitor visitor, final boolean backup, final int i) {
    return visitor.lock(() -> {
      final ArrayList<String> list = new ArrayList<>(1);
      final Expr expr = exprs[i];
      String name = expr instanceof Str ? string(((Str) expr).string()) :
        expr instanceof Atm ? string(((Atm) expr).string(null)) : null;
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
    plan.add(plan.create(this, NAME, definition.id()), exprs);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(definition.id()).params(exprs);
  }
}
