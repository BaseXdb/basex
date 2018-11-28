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
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.XQMap;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Built-in functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Minimum size of a loop that should not be unrolled. */
  public static final int UNROLL_LIMIT = 10;

  /** Function signature. */
  public Function sig;
  /** Static context. */
  public StaticContext sc;

  /**
   * Constructor.
   */
  protected StandardFunc() {
    super(null, SeqType.ITEM_ZM);
  }

  /**
   * Constructor. Invoked by {@link Function#get(StaticContext, InputInfo, Expr...)}.
   * @param ii input info
   * @param sctx static context
   * @param func function definition
   * @param args function arguments
   * @return self reference
   */
  final StandardFunc init(final StaticContext sctx, final InputInfo ii, final Function func,
      final Expr[] args) {
    sc = sctx;
    sig = func;
    info = ii;
    exprs = args;
    exprType.assign(func.seqType);
    return this;
  }

  @Override
  public final Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr = opt(cc);
    return cc.replaceWith(this, expr != this ?
      // return optimized expression
      expr : preEval() ?
      // pre-evaluate function
      (sig.seqType.zeroOrOne() ? item(cc.qc, info) : value(cc.qc)) :
      // return original function
      this);
  }

  /**
   * Checks if the function can be pre-evaluated.
   * <ul>
   *   <li> All arguments must be values. If the function may return multiple items,
   *     the arguments must be of small size.</li>
   *   <li> Function must be {@link #isSimple() simple}.</li>
   * </ul>
   * @return result of check
   */
  protected boolean preEval() {
    return allAreValues(sig.seqType.occ.max > 1) && isSimple();
  }

  /**
   * Performs function specific optimizations.
   * @param cc compilation context
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Expr opt(final CompileContext cc) throws QueryException {
    return this;
  }

  @Override
  public final StandardFunc copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final int es = exprs.length;
    final Expr[] arg = new Expr[es];
    for(int e = 0; e < es; e++) arg[e] = exprs[e].copy(cc, vm);
    return copyType(sig.get(sc, info, arg));
  }

  /**
   * Optimizes a function that returns an empty sequence when the first argument is empty as well.
   * Sets the occurrence indicator to 1 if the first expression returns at least one non-array item.
   * Replaces the function with the argument if it yields no results.
   * @return original expression, or function argument (if it yields no results)
   */
  protected Expr optFirst() {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;
    if(st.oneOrMore() && !st.mayBeArray()) exprType.assign(Occ.ONE);
    return this;
  }

  /**
   * Serializes the data from the specified iterator.
   * @param iter data to serialize
   * @param opts serialization parameters
   * @param err error code
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  protected final byte[] serialize(final Iter iter, final SerializerOptions opts,
      final QueryError err, final QueryContext qc) throws QueryException {

    try {
      final ArrayOutput ao = new ArrayOutput();
      try(Serializer ser = Serializer.get(ao, opts)) {
        for(Item item; (item = qc.next(iter)) != null;) ser.serialize(item);
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
    // check signature flags
    for(final Flag flag : flags) {
      if(sig.has(flag)) return true;
    }
    // mix updates: higher-order function may be updating
    if(Flag.UPD.in(flags) && sc.mixUpdates && sig.has(Flag.HOF)) return true;
    // check arguments (without function invocation; it only applies to function itself)
    final Flag[] flgs = Flag.HOF.remove(flags);
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public boolean isVacuous() {
    return !has(Flag.UPD) && size() == 0;
  }

  /**
   * Refines the type of a function item argument.
   * @param i index of argument
   * @param cc compilation context
   * @param declType declared return type
   * @param argTypes argument types
   * @return success flag
   * @throws QueryException query context
   */
  public boolean coerceFunc(final int i, final CompileContext cc, final SeqType declType,
      final SeqType... argTypes) throws QueryException {

    // check if argument is function item
    final Expr func = exprs[i];
    if(!(func instanceof FuncItem)) return false;

    // check number of arguments
    final FuncItem fitem = (FuncItem) func;
    final FuncType oldType = fitem.funcType();
    final int al = argTypes.length;
    final SeqType[] oldArgs = oldType.argTypes;
    if(al != oldArgs.length) return false;

    // select most specific argument and return types
    final SeqType[] newArgs = new SeqType[al];
    for(int a = 0; a < al; a++) {
      newArgs[a] = argTypes[a].instanceOf(oldArgs[a]) ? argTypes[a] : oldArgs[a];
    }
    final SeqType newDecl = declType.instanceOf(oldType.declType) ? declType : oldType.declType;
    final FuncType newType = FuncType.get(newDecl, newArgs);

    // new type is more specific: coerce to new function type
    if(!newType.eq(oldType)) exprs[i] = fitem.coerceTo(newType, cc.qc, info, true);
    return true;
  }

  /**
   * Returns the specified argument, or the context value if it does not exist.
   * @param i index of argument
   * @param qc query context
   * @return expression
   * @throws QueryException query exception
   */
  protected final Expr ctxArg(final int i, final QueryContext qc) throws QueryException {
    return exprs.length == i ? ctxValue(qc) : exprs[i];
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param item item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final DBNode toDBNode(final Item item) throws QueryException {
    if(checkNoEmpty(item, NodeType.NOD) instanceof DBNode) return (DBNode) item;
    throw DB_NODE_X.get(info, item);
  }

  /**
   * Checks if the specified collation is supported.
   * @param i index of argument
   * @param qc query context
   * @return collator or {@code null} (default collation)
   * @throws QueryException query exception
   */
  protected final Collation toCollation(final int i, final QueryContext qc) throws QueryException {
    final byte[] coll = i >= exprs.length ? null : toToken(exprs[i], qc);
    return Collation.get(coll, qc, sc, info, WHICHCOLL_X);
  }

  /**
   * Converts the specified argument to a file path.
   * @param i index of argument
   * @param qc query context
   * @return file instance
   * @throws QueryException query exception
   */
  protected final Path toPath(final int i, final QueryContext qc) throws QueryException {
    return i < exprs.length ? toPath(toToken(exprs[i], qc)) : null;
  }

  /**
   * Converts the specified string to a file path.
   * @param path path string
   * @return file instance
   * @throws QueryException query exception
   */
  protected final Path toPath(final byte[] path) throws QueryException {
    try {
      final String p = string(path);
      return p.startsWith(IO.FILEPREF) ? Paths.get(new URI(p)) : Paths.get(p);
    } catch(final InvalidPathException | URISyntaxException ex) {
      Util.debug(ex);
      throw FILE_INVALID_PATH_X.get(info, QueryError.chop(path, info));
    }
  }

  /**
   * Returns a valid reference if a file is found at the specified path or the static base uri.
   * Otherwise, returns an error.
   * @param i index of URI argument
   * @param qc query context
   * @return input source, or exception
   * @throws QueryException query exception
   */
  protected final IO checkPath(final int i, final QueryContext qc) throws QueryException {
    return checkPath(toToken(exprs[i], qc));
  }

  /**
   * Returns a valid reference if a file is found at the specified path or the static base uri.
   * Otherwise, returns an error.
   * @param uri file URI
   * @return input source, or exception
   * @throws QueryException query exception
   */
  protected final IO checkPath(final byte[] uri) throws QueryException {
    final QueryInput qi = new QueryInput(string(uri), sc);
    if(qi.io.exists()) return qi.io;
    throw WHICHRES_X.get(info, QueryError.chop(uri, info));
  }

  /**
   * Returns a normalized encoding representation.
   * @param i index of encoding argument
   * @param err error for invalid encoding
   * @param qc query context
   * @return string or {@code null}
   * @throws QueryException query exception
   */
  protected final String toEncoding(final int i, final QueryError err, final QueryContext qc)
      throws QueryException {

    if(i >= exprs.length) return null;
    final String encoding = string(toToken(exprs[i], qc));
    try {
      if(Charset.isSupported(encoding)) return Strings.normEncoding(encoding);
    } catch(final IllegalArgumentException ignored) {
      /* character set is invalid or unknown (e.g. empty string) */
    }
    throw err.get(info, encoding);
  }

  /**
   * Returns the expression at the specified index as node or atomized item.
   * Returns the item or throws an exception.
   * @param i index of argument
   * @param qc query context
   * @return node, atomized item or {@code null}
   * @throws QueryException query exception
   */
  protected final Item toNodeOrAtomItem(final int i, final QueryContext qc) throws QueryException {
    if(i >= exprs.length) return null;
    final Item item = toItem(exprs[i], qc);
    return item instanceof ANode ? item : item.atomItem(qc, info);
  }

  /**
   * Parses the options at the specified index.
   * @param <E> options type
   * @param i index of argument (can exceed length of argument, or may yield an empty sequence)
   * @param opts options
   * @param qc query context
   * @return passed on options
   * @throws QueryException query exception
   */
  protected final <E extends Options> E toOptions(final int i, final E opts, final QueryContext qc)
      throws QueryException {
    return i >= exprs.length ? opts : new FuncOptions(info).assign(exprs[i].item(qc, info), opts);
  }

  /**
   * Returns all keys and values of the specified binding argument.
   * @param i index of argument
   * @param qc query context
   * @return resulting map
   * @throws QueryException query exception
   */
  protected final HashMap<String, Value> toBindings(final int i, final QueryContext qc)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<>();
    final int es = exprs.length;
    if(i < es) {
      final Item item = exprs[i].item(qc, info);
      final XQMap map = item == null ? XQMap.EMPTY : toMap(item);
      for(final Item it : map.keys()) {
        final byte[] key;
        if(it.type.isStringOrUntyped()) {
          key = it.string(null);
        } else {
          final QNm qnm = toQNm(it, false);
          final TokenBuilder tb = new TokenBuilder();
          if(qnm.uri() != null) tb.add('{').add(qnm.uri()).add('}');
          key = tb.add(qnm.local()).finish();
        }
        hm.put(string(key), map.get(it, info));
      }
    }
    return hm;
  }

  /**
   * Returns a database instance for the first string argument of the function.
   * This method assumes that the function has at least one argument.
   * @param qc query context
   * @return data instance
   * @throws QueryException query exception
   */
  protected final Data checkData(final QueryContext qc) throws QueryException {
    final String name = string(toToken(exprs[0], qc));
    if(!Databases.validName(name)) throw INVDB_X.get(info, name);
    return qc.resources.database(name, info);
  }

  /**
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void checkAdmin(final QueryContext qc) throws QueryException {
    checkPerm(qc, Perm.ADMIN);
  }

  /**
   * Checks if the current user has create permissions. If negative, an exception is thrown.
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void checkCreate(final QueryContext qc) throws QueryException {
    checkPerm(qc, Perm.CREATE);
  }

  /**
   * Checks if the current user has given permissions. If negative, an
   * exception is thrown.
   * @param qc query context
   * @param perm permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext qc, final Perm perm) throws QueryException {
    if(!qc.context.user().has(perm)) throw BASEX_PERMISSION_X_X.get(info, perm, this);
  }

  /**
   * Casts and checks the function item for its arity.
   * @param expr expression
   * @param nargs number of arguments (arity)
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem checkArity(final Expr expr, final int nargs, final QueryContext qc)
      throws QueryException {
    return checkArity(expr, nargs, false, qc);
  }

  /**
   * Casts and checks the function item for its arity.
   * @param expr expression
   * @param nargs number of arguments (arity)
   * @param qc query context
   * @param updating updating flag
   * @return function item
   * @throws QueryException query exception
   */
  protected final FItem checkArity(final Expr expr, final int nargs, final boolean updating,
      final QueryContext qc) throws QueryException {

    final FItem func = checkUp(toFunc(expr, qc), updating, sc);
    if(func.arity() == nargs) return func;
    final int fargs = func.arity();
    throw FUNARITY_X_X.get(info, arguments(fargs), nargs);
  }

  /**
   * Converts the specified dateTime to milliseconds.
   * @param expr expression
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  protected final long dateTimeToMs(final Expr expr, final QueryContext qc) throws QueryException {
    final Dtm dtm = (Dtm) checkType(expr, qc, AtomType.DTM);
    if(dtm.yea() > 292278993) throw INTRANGE_X.get(info, dtm.yea());
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
  }

  /**
   * Tries to mark the specified argument for locking.
   * @param visitor visitor
   * @param i index of database argument
   * @return result of check
   */
  protected final boolean dataLock(final ASTVisitor visitor, final int i) {
    final String db = exprs[i] instanceof Str ? string(((Str) exprs[i]).string()) : null;
    return visitor.lock(db, false);
  }

  /**
   * Returns the arguments of a standard function.
   * @param func functions argument
   * @return arguments
   */
  protected static final Expr[] args(final Expr func) {
    return ((StandardFunc) func).exprs;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof StandardFunc && sig == ((StandardFunc) obj).sig &&
        super.equals(obj);
  }

  @Override
  public final String description() {
    return sig.toString();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAME, sig.desc), exprs);
  }

  @Override
  public final String toString() {
    return new TokenBuilder().add(sig.id()).add('(').addSep(exprs, SEP).add(')').toString();
  }
}
