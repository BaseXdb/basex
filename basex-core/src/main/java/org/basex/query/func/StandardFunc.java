package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
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
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

/**
 * Built-in functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function signature. */
  public Function sig;
  /** Static context. */
  public StaticContext sc;

  /**
   * Constructor.
   */
  protected StandardFunc() {
    super(null);
  }

  /**
   * Constructor.
   * @param ii input info
   * @param sctx static context
   * @param f function definition
   * @param args function arguments
   * @return self reference
   */
  StandardFunc init(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr[] args) {
    sc = sctx;
    sig = f;
    info = ii;
    exprs = args;
    seqType = f.ret;
    return this;
  }

  @Override
  public final Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    // compile all arguments
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public final Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // skip context-based or non-deterministic functions, and non-values
    return optPre(has(Flag.CTX) || has(Flag.NDT) || has(Flag.HOF) || has(Flag.UPD) ||
        !allAreValues() ? opt(qc, scp) : sig.ret.zeroOrOne() ? item(qc, info) : value(qc), qc);
  }

  /**
   * Performs function specific optimizations.
   * @param qc query context
   * @param scp variable scope
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    return this;
  }

  @Override
  public final StandardFunc copy(final QueryContext qc, final VarScope scp,
      final IntObjMap<Var> vs) {
    final int es = exprs.length;
    final Expr[] arg = new Expr[es];
    for(int e = 0; e < es; e++) arg[e] = exprs[e].copy(qc, scp, vs);
    return sig.get(sc, info, arg);
  }

  /**
   * Serializes the data from the specified iterator.
   * @param ir data to serialize
   * @param opts serialization parameters
   * @param err error code
   * @return result
   * @throws QueryException query exception
   */
  protected byte[] serialize(final Iter ir, final SerializerOptions opts, final QueryError err)
      throws QueryException {

    try {
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = Serializer.get(ao, opts);
      for(Item it; (it = ir.next()) != null;) ser.serialize(it);
      ser.close();
      return ao.normalize().finish();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      throw err.get(info, ex);
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return sig.has(flag) || flag != Flag.HOF && super.has(flag);
  }

  @Override
  public final boolean isFunction(final Function f) {
    return sig == f;
  }

  @Override
  public boolean isVacuous() {
    return !has(Flag.UPD) && seqType.eq(SeqType.EMP);
  }

  @Override
  public final String description() {
    return sig.toString();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, sig.desc), exprs);
  }

  @Override
  public final String toString() {
    return new TokenBuilder(sig.id()).add('(').addSep(exprs, SEP).add(')').toString();
  }

  /**
   * Returns the specified argument, or the context value if it does not exist.
   * @param index argument index
   * @param qc query context
   * @return expression
   * @throws QueryException query exception
   */
  protected Expr arg(final int index, final QueryContext qc) throws QueryException {
    return exprs.length == index ? ctxValue(qc) : exprs[index];
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final DBNode toDBNode(final Item it) throws QueryException {
    if(checkNoEmpty(it, NodeType.NOD) instanceof DBNode) return (DBNode) it;
    throw BXDB_NODB_X_X.get(info, it.type, it);
  }

  /**
   * Checks if the specified collation is supported.
   * @param i argument index
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
   * @param i argument index
   * @param qc query context
   * @return file instance
   * @throws QueryException query exception
   */
  protected Path toPath(final int i, final QueryContext qc) throws QueryException {
    if(i >= exprs.length) return null;
    final String file = string(toToken(exprs[i], qc));
    try {
      return Paths.get(IOUrl.isFileURL(file) ? IOUrl.toFile(file) : file);
    } catch(final InvalidPathException ex) {
      throw FILE_INVALID_PATH_X.get(info, file);
    }
  }

  /**
   * Returns a valid reference if a file is found in the specified path or the static base uri.
   * Otherwise, returns an error.
   * @param path file path
   * @param qc query context
   * @return input source, or exception
   * @throws QueryException query exception
   */
  protected IO checkPath(final Expr path, final QueryContext qc) throws QueryException {
    return QueryResources.checkPath(new QueryInput(string(toToken(path, qc))), sc.baseIO(), info);
  }

  /**
   * Returns a normalized encoding representation.
   * @param i index of encoding argument
   * @param err error for invalid encoding
   * @param qc query context
   * @return text entry
   * @throws QueryException query exception
   */
  protected final String toEncoding(final int i, final QueryError err, final QueryContext qc)
      throws QueryException {

    if(i >= exprs.length) return null;
    final String enc = string(toToken(exprs[i], qc));
    try {
      if(Charset.isSupported(enc)) return Strings.normEncoding(enc);
    } catch(final IllegalArgumentException ignored) {
      /* character set is invalid or unknown (e.g. empty string) */
    }
    throw err.get(info, enc);
  }

  /**
   * Parses the options at the specified index.
   * @param <E> options type
   * @param i index of options argument
   * @param qnm QName
   * @param opts options
   * @param qc query context
   * @return passed on options
   * @throws QueryException query exception
   */
  protected <E extends Options> E toOptions(final int i, final QNm qnm, final E opts,
      final QueryContext qc) throws QueryException {
    if(i < exprs.length) new FuncOptions(qnm, info).parse(exprs[i].item(qc, info), opts);
    return opts;
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
      final Map map = toMap(exprs[i], qc);
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
   * @param p permission
   * @throws QueryException query exception
   */
  private void checkPerm(final QueryContext qc, final Perm p) throws QueryException {
    if(!qc.context.user().has(p)) throw BASX_PERM_X.get(info, p);
  }

  /**
   * Casts and checks the function item for its arity.
   * @param e expression
   * @param a arity
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  protected FItem checkArity(final Expr e, final int a, final QueryContext qc)
      throws QueryException {

    final FItem it = toFunc(e, qc);
    if(it.arity() == a) return it;
    throw castError(info, it, FuncType.arity(a));
  }

  /**
   * Converts the specified dateTime to milliseconds.
   * @param ex expression
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  protected final long dateTimeToMs(final Expr ex, final QueryContext qc) throws QueryException {
    final Dtm dtm = (Dtm) checkAtomic(ex, qc, AtomType.DTM);
    if(dtm.yea() > 292278993) throw INTRANGE_X.get(info, dtm.yea());
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
  }

  /**
   * Tries to mark the specified argument for locking.
   * @param visitor visitor
   * @param i index of argument
   * @return success flag
   */
  protected final boolean dataLock(final ASTVisitor visitor, final int i) {
    return visitor.lock(exprs[i] instanceof Str ? string(((Str) exprs[i]).string()) : null);
  }

  /**
   * Caches and materializes all items of the specified iterator.
   * @param iter iterator
   * @param vb value builder
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void cache(final Iter iter, final ValueBuilder vb, final QueryContext qc)
      throws QueryException {

    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(it instanceof FItem) throw FISTRING_X.get(info, it.type);
      vb.add(it.materialize(info));
    }
  }

  /**
   * Compares several signatures for equality.
   * @param sig signature to be found
   * @param sigs signatures to be compared
   * @return result of check
   */
  protected static boolean oneOf(final Function sig, final Function... sigs) {
    for(final Function s : sigs) if(sig == s) return true;
    return false;
  }
}
