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
    this.type = func.ret;
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    // compile all arguments
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public final Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // skip context-based or non-deterministic functions, and non-values
    return optPre(has(Flag.CTX) || has(Flag.NDT) || has(Flag.HOF) || has(Flag.UPD) ||
        !allAreValues() ? opt(ctx, scp) : func.ret.zeroOrOne() ? item(ctx, info) : value(ctx), ctx);
  }

  /**
   * Performs function specific optimizations.
   * @param ctx query context
   * @param scp variable scope
   * @return evaluated item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  Expr opt(final QueryContext ctx, final VarScope scp) throws QueryException {
    return this;
  }

  @Override
  public final StandardFunc copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final int es = exprs.length;
    final Expr[] arg = new Expr[es];
    for(int e = 0; e < es; e++) arg[e] = exprs[e].copy(ctx, scp, vs);
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
    return ao.toArray();
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
    return !has(Flag.UPD) && type.eq(SeqType.EMP);
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
   * Returns a database instance for the first string argument of the function.
   * This method assumes that the function has at least one argument.
   * @param ctx query context
   * @return data instance
   * @throws QueryException query exception
   */
  protected final Data checkData(final QueryContext ctx) throws QueryException {
    final String name = string(checkStr(exprs[0], ctx));
    if(!Databases.validName(name)) throw INVDB.get(info, name);
    return ctx.resources.database(name, info);
  }

  /**
   * Converts the specified argument to a file path.
   * @param i argument index
   * @param ctx query context
   * @return file instance
   * @throws QueryException query exception
   */
  protected Path checkPath(final int i, final QueryContext ctx) throws QueryException {
    if(i >= exprs.length) return null;
    final String file = string(checkStr(exprs[i], ctx));
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
   * @param ctx query context
   * @return input source, or exception
   * @throws QueryException query exception
   */
  IO checkPath(final Expr path, final QueryContext ctx) throws QueryException {
    return QueryResources.checkPath(new QueryInput(string(checkStr(path, ctx))), sc.baseIO(), info);
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
   * Returns a normalized encoding representation.
   * @param i index of encoding argument
   * @param err error for invalid encoding
   * @param ctx query context
   * @return text entry
   * @throws QueryException query exception
   */
  protected final String encoding(final int i, final Err err, final QueryContext ctx)
      throws QueryException {

    if(i >= exprs.length) return null;
    final String enc = string(checkStr(exprs[i], ctx));
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
   * @param ctx query context
   * @return passed on options
   * @throws QueryException query exception
   */
  protected <E extends Options> E checkOptions(final int i, final QNm qnm, final E opts,
      final QueryContext ctx) throws QueryException {
    if(i < exprs.length) new FuncOptions(qnm, info).parse(exprs[i].item(ctx, info), opts);
    return opts;
  }

  /**
   * Converts the specified dateTime to milliseconds.
   * @param e expression
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  protected final long dateTimeToMs(final Expr e, final QueryContext ctx) throws QueryException {
    final Dtm dtm = (Dtm) checkType(checkItem(e, ctx), AtomType.DTM);
    if(dtm.yea() > 292278993) throw INTRANGE.get(info, dtm);
    return dtm.toJava().toGregorianCalendar().getTimeInMillis();
  }

  /**
   * Returns all keys and values of the specified binding argument.
   * @param i index of argument
   * @param ctx query context
   * @return resulting map
   * @throws QueryException query exception
   */
  protected final HashMap<String, Value> bindings(final int i, final QueryContext ctx)
      throws QueryException {

    final HashMap<String, Value> hm = new HashMap<>();
    final int es = exprs.length;
    if(i < es) {
      final Map map = checkMap(checkItem(exprs[i], ctx));
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
   * @param ctx query context
   * @throws QueryException query exception
   */
  protected final void cache(final Iter iter, final ValueBuilder vb, final QueryContext ctx)
      throws QueryException {

    for(Item it; (it = iter.next()) != null;) {
      ctx.checkStop();
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
