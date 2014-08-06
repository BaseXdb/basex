package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
 * Standard (built-in) functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class StandardFunc extends Arr {
  /** Function signature. */
  Function func;
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  protected StandardFunc(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(info, args);
    this.sc = sc;
    this.func = func;
    this.seqType = func.ret;
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
        !allAreValues() ? opt(qc, scp) : func.ret.zeroOrOne() ? item(qc, info) : value(qc), qc);
  }

  /**
   * Performs function specific optimizations.
   * @param qc query context
   * @param scp variable scope
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    return this;
  }

  @Override
  public final StandardFunc copy(final QueryContext qc, final VarScope scp,
      final IntObjMap<Var> vs) {
    final int es = exprs.length;
    final Expr[] arg = new Expr[es];
    for(int e = 0; e < es; e++) arg[e] = exprs[e].copy(qc, scp, vs);
    return func.get(sc, info, arg);
  }

  /**
   * Atomizes the specified item.
   * @param it input item
   * @param ii input info
   * @return atomized item
   * @throws QueryException query exception
   */
  public static Item atom(final Item it, final InputInfo ii) throws QueryException {
    final Type ip = it.type;
    return it instanceof ANode ? ip == NodeType.PI || ip == NodeType.COM ?
        Str.get(it.string(ii)) : new Atm(it.string(ii)) : it.materialize(ii);
  }

  /**
   * Serializes the data from the specified iterator.
   * @param ir data to serialize
   * @param opts serialization parameters
   * @param err error code
   * @return result
   * @throws QueryException query exception
   */
  byte[] serialize(final Iter ir, final SerializerOptions opts, final Err err)
      throws QueryException {

    final ArrayOutput ao = new ArrayOutput();
    try {
      final Serializer ser = Serializer.get(ao, opts);
      for(Item it; (it = ir.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      throw err.get(info, ex);
    }
    return ao.finish();
  }

  @Override
  public boolean has(final Flag flag) {
    return func.has(flag) || flag != Flag.X30 && flag != Flag.HOF && super.has(flag);
  }

  @Override
  public final boolean isFunction(final Function f) {
    return func == f;
  }

  @Override
  public final boolean isVacuous() {
    return !has(Flag.UPD) && seqType.eq(SeqType.EMP);
  }

  @Override
  public final String description() {
    return func.toString();
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, func.desc), exprs);
  }

  @Override
  public final String toString() {
    final String desc = func.toString();
    return new TokenBuilder(desc.substring(0,
        desc.indexOf('(') + 1)).addSep(exprs, SEP).add(PAR2).toString();
  }

  /**
   * Checks if the specified expression is a database node.
   * Returns the node or an exception.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  protected final DBNode checkDBNode(final Item it) throws QueryException {
    if(checkNoEmpty(it, NodeType.NOD) instanceof DBNode) return (DBNode) it;
    throw BXDB_NODB.get(info, it.type, it);
  }

  /**
   * Checks if the specified collation is supported.
   * @param i argument index
   * @param qc query context
   * @return collator, or {@code null} (default collation)
   * @throws QueryException query exception
   */
  protected final Collation checkColl(final int i, final QueryContext qc) throws QueryException {
    final byte[] coll = i >= exprs.length ? null : checkStr(exprs[i], qc);
    return Collation.get(coll, qc, sc, info, WHICHCOLL);
  }

  /**
   * Returns a database instance for the first string argument of the function.
   * This method assumes that the function has at least one argument.
   * @param qc query context
   * @return data instance
   * @throws QueryException query exception
   */
  protected final Data checkData(final QueryContext qc) throws QueryException {
    final String name = string(checkStr(exprs[0], qc));
    if(!Databases.validName(name)) throw INVDB.get(info, name);
    return qc.resources.database(name, info);
  }

  /**
   * Converts the specified argument to a file path.
   * @param i argument index
   * @param qc query context
   * @return file instance
   * @throws QueryException query exception
   */
  protected Path checkPath(final int i, final QueryContext qc) throws QueryException {
    if(i >= exprs.length) return null;
    final String file = string(checkStr(exprs[i], qc));
    try {
      return Paths.get(IOUrl.isFileURL(file) ? IOUrl.toFile(file) : file);
    } catch(final InvalidPathException ex) {
      throw FILE_INVALID_PATH.get(info, file);
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
    return QueryResources.checkPath(new QueryInput(string(checkStr(path, qc))), sc.baseIO(), info);
  }

  /**
   * Returns a normalized encoding representation.
   * @param i index of encoding argument
   * @param err error for invalid encoding
   * @param qc query context
   * @return text entry
   * @throws QueryException query exception
   */
  protected final String checkEncoding(final int i, final Err err, final QueryContext qc)
      throws QueryException {

    if(i >= exprs.length) return null;
    final String enc = string(checkStr(exprs[i], qc));
    try {
      if(Charset.isSupported(enc)) return normEncoding(enc);
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
  protected <E extends Options> E checkOptions(final int i, final QNm qnm, final E opts,
      final QueryContext qc) throws QueryException {
    if(i < exprs.length) new FuncOptions(qnm, info).parse(exprs[i].item(qc, info), opts);
    return opts;
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
   * Checks if the current user has create permissions. If negative, an
   * exception is thrown.
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
    if(!qc.context.user.has(p)) throw BASX_PERM.get(info, p);
  }

  /**
   * Converts the specified dateTime to milliseconds.
   * @param ex expression
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  protected final long dateTimeToMs(final Expr ex, final QueryContext qc) throws QueryException {
    final Dtm dtm = (Dtm) checkType(checkItem(ex, qc), AtomType.DTM);
    if(dtm.yea() > 292278993) throw INTRANGE.get(info, dtm.yea());
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
  }

  /**
   * Checks if one of the specified arguments point to databases that need to be locked.
   * @param visitor visitor
   * @param dbs database arguments
   * @return result of check
   */
  protected final boolean dataLock(final ASTVisitor visitor, final int dbs) {
    boolean more = true;
    for(int db = 0; db < dbs; db++) {
      more &= visitor.lock(exprs[db] instanceof Str ? string(((Str) exprs[db]).string()) : null);
    }
    return more;
  }

  /**
   * Returns all keys and values of the specified binding argument.
   * @param i index of argument
   * @param qc query context
   * @return resulting map
   * @throws QueryException query exception
   */
  protected final HashMap<String, Value> bindings(final int i, final QueryContext qc)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<>();
    final int es = exprs.length;
    if(i < es) {
      final Map map = checkMap(checkItem(exprs[i], qc));
      for(final Item it : map.keys()) {
        final byte[] key;
        if(it instanceof Str) {
          key = it.string(null);
        } else {
          final QNm qnm = (QNm) checkType(it, AtomType.QNM);
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
      if(it instanceof FItem) throw FIVALUE.get(info, it.type);
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
