package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides access to built-in and user-defined functions.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Functions extends TokenSet {
  /** Singleton instance. */
  private static final Functions INSTANCE = new Functions();
  /** Function classes. */
  private Function[] funcs = new Function[Array.CAPACITY];

  /**
   * Returns the singleton instance.
   * @return instance
   */
  public static Functions get() {
    return INSTANCE;
  }

  /**
   * Constructor, registering built-in XQuery functions.
   */
  private Functions() {
    for(final Function sig : Function.VALUES) {
      final String desc = sig.desc;
      final byte[] ln = token(desc.substring(0, desc.indexOf('(')));
      final int i = put(new QNm(ln, sig.uri()).id());
      if(funcs[i] != null) throw Util.notExpected("Function defined twice: " + sig);
      funcs[i] = sig;
    }
  }

  /**
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param arity number of arguments
   * @param name function name
   * @param info input info
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Type getCast(final QNm name, final long arity, final InputInfo info)
      throws QueryException {

    final byte[] ln = name.local();
    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);

    // no constructor function found, or abstract type specified
    if(type != null && type != AtomType.NOT && type != AtomType.AAT) {
      if(arity == 1) return type;
      throw FUNCTYPES_X_X_X.get(info, name.string(), arguments(arity), 1);
    }

    // include similar function name in error message
    final Levenshtein ls = new Levenshtein();
    for(final AtomType t : AtomType.VALUES) {
      if(t.parent == null) continue;
      final byte[] u = t.name.uri();
      if(eq(u, XS_URI) && t != AtomType.NOT && t != AtomType.AAT && ls.similar(
          lc(ln), lc(t.string()))) throw FUNCSIMILAR_X_X.get(info, name.prefixId(), t.string());
    }
    // no similar name: constructor function found, or abstract type specified
    throw WHICHFUNC_X.get(info, name.prefixId());
  }

  /**
   * Returns a built-in function with the specified name.
   * @param name function name
   * @return function if found, {@code null} otherwise
   */
  public Function getBuiltIn(final QNm name) {
    final int id = id(name.id());
    if(id == 0) return null;
    final Function fn = funcs[id];
    return eq(fn.uri(), name.uri()) ? fn : null;
  }

  /**
   * Returns a built-in function with the specified name and arity.
   * Raises an error if the function is found, but has a different arity.
   * @param name function name
   * @param arity number of arguments
   * @param info input info
   * @return function if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private Function getBuiltIn(final QNm name, final long arity, final InputInfo info)
      throws QueryException {

    final Function fn = getBuiltIn(name);
    if(fn == null) return null;

    final int min = fn.minMax[0], max = fn.minMax[1];
    if(arity >= min && arity <= max) return fn;

    final IntList arities = new IntList();
    if(max != Integer.MAX_VALUE) {
      for(int m = min; m <= max; m++) arities.add(m);
    }
    throw wrongArity(fn, arity, arities, info);
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param func function
   * @param arity number of arguments
   * @param arities expected arities
   * @param info input info
   * @return error
   */
  public static QueryException wrongArity(final Object func, final long arity,
      final IntList arities, final InputInfo info) {

    final int as = arities.sort().size();
    if(as == 0) return FUNCARGNUM_X_X.get(info, func, arguments(arity));

    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for(int a = 0; a < as; a++) {
      final int m = arities.get(a);
      if(m < min) min = m;
      if(m > max) max = m;
    }

    final StringBuilder ext = new StringBuilder();
    if(as > 2 && max - min + 1 == as) {
      ext.append(min).append('-').append(max);
    } else {
      for(int a = 0; a < as; a++) {
        if(a != 0) ext.append(a + 1 < as ? ", " : " or ");
        ext.append(arities.get(a));
      }
    }
    return FUNCTYPES_X_X_X.get(info, func, arguments(arity), ext);
  }

  /**
   * Returns the specified function.
   * @param name function qname
   * @param args optional arguments
   * @param sc static context
   * @param info input info
   * @return function instance
   * @throws QueryException query exception
   */
  public StandardFunc get(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo info) throws QueryException {
    final Function fn = getBuiltIn(name, args.length, info);
    return fn == null ? null : fn.get(sc, info, args);
  }

  /**
   * Creates either a {@link FuncItem} or a {@link Closure} depending on when the method is called.
   * At parse and compile time a closure is generated to enable inlining and compilation, at
   * runtime we directly generate a function item.
   * @param anns function annotations
   * @param name function name, may be {@code null}
   * @param params formal parameters
   * @param ft function type
   * @param expr function body
   * @param scp variable scope
   * @param info input info
   * @param runtime run-time flag
   * @param updating flag for updating functions
   * @return the function expression
   */
  private static Expr closureOrFItem(final AnnList anns, final QNm name, final Var[] params,
      final FuncType ft, final Expr expr, final VarScope scp, final InputInfo info,
      final boolean runtime, final boolean updating) {
    return runtime ? new FuncItem(scp.sc, anns, name, params, ft, expr, scp.stackSize()) :
      new Closure(info, name, updating ? null : ft.valueType, params, expr, anns, null, scp);
  }

  /**
   * Gets a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @param runtime {@code true} if this method is called at run-time, {@code false} otherwise
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr getLiteral(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo info, final boolean runtime) throws QueryException {

    // parse type constructors
    if(eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, arity, info);
      final VarScope scp = new VarScope(sc);
      final Var[] args = { scp.addNew(new QNm(ITEMM, ""), SeqType.AAT_ZO, true, qc, info) };
      final Expr e = new Cast(sc, info, new VarRef(info, args[0]), type.seqType());
      final AnnList anns = new AnnList();
      final FuncType ft = FuncType.get(anns, e.seqType(), args);
      return closureOrFItem(anns, name, args, ft, e, scp, info, runtime, false);
    }

    // built-in functions
    final Function fn = get().getBuiltIn(name, arity, info);
    if(fn != null) {
      final AnnList anns = new AnnList();
      final VarScope scp = new VarScope(sc);
      final FuncType ft = fn.type(arity, anns);
      final QNm[] argNames = fn.argNames(arity);

      final Var[] args = new Var[arity];
      final Expr[] calls = new Expr[arity];
      for(int i = 0; i < arity; i++) {
        args[i] = scp.addNew(argNames[i], ft.argTypes[i], true, qc, info);
        calls[i] = new VarRef(info, args[i]);
      }

      final StandardFunc sf = fn.get(sc, info, calls);
      final boolean upd = sf.has(Flag.UPD);
      if(upd) {
        anns.add(new Ann(info, Annotation.UPDATING));
        qc.updating();
      }

      return sf.has(Flag.CTX) || sf.has(Flag.POS)
          ? new FuncLit(anns, name, args, sf, ft, scp, info)
          : closureOrFItem(anns, name, args, fn.type(arity, anns), sf, scp, info, runtime, upd);
    }

    // user-defined function
    final StaticFunc sf = qc.funcs.get(name, arity, info, false);
    if(sf != null) {
      final FuncType ft = sf.funcType();
      final VarScope scp = new VarScope(sc);
      final Var[] args = new Var[arity];
      final Expr[] calls = new Expr[arity];
      for(int a = 0; a < arity; a++) {
        args[a] = scp.addNew(sf.argName(a), ft.argTypes[a], true, qc, info);
        calls[a] = new VarRef(info, args[a]);
      }

      final boolean upd = sf.updating;
      final TypedFunc tf = qc.funcs.getFuncRef(sf.name, calls, sc, info);
      final Expr f = closureOrFItem(tf.anns, sf.name, args, ft, tf.fun, scp, info, runtime, upd);
      if(upd) qc.updating();
      return f;
    }

    // Java function
    final VarScope scp = new VarScope(sc);
    final FuncType jt = FuncType.arity(arity);
    final Var[] vs = new Var[arity];
    final Expr[] args = new Expr[vs.length];
    final int vl = vs.length;
    for(int v = 0; v < vl; v++) {
      vs[v] = scp.addNew(new QNm(ARG + (v + 1), ""), null, true, qc, info);
      args[v] = new VarRef(info, vs[v]);
    }
    final Expr jf = JavaFunction.get(name, args, qc, sc, info);
    return jf == null ? null : new FuncLit(new AnnList(), name, vs, jf, jt, scp, info);
  }

  /**
   * Returns a function item for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public static FuncItem getUser(final StaticFunc sf, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {

    final FuncType ft = sf.funcType();
    final VarScope scp = new VarScope(sc);
    final int arity = sf.args.length;
    final Var[] args = new Var[arity];
    final int al = args.length;
    final Expr[] calls = new Expr[al];
    for(int a = 0; a < al; a++) {
      args[a] = scp.addNew(sf.argName(a), ft.argTypes[a], true, qc, info);
      calls[a] = new VarRef(info, args[a]);
    }
    final TypedFunc tf = qc.funcs.getFuncRef(sf.name, calls, sc, info);
    return new FuncItem(sc, tf.anns, sf.name, args, ft, tf.fun, scp.stackSize());
  }

  /**
   * Returns a function with the specified name and number of arguments.
   * @param name name of the function
   * @param args optional arguments
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @return function instance, or {@code null}
   * @throws QueryException query exception
   */
  public static TypedFunc get(final QNm name, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {

    // get namespace and local name
    // parse type constructors
    if(eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, args.length, info);
      final SeqType to = SeqType.get(type, Occ.ZERO_ONE);
      return TypedFunc.constr(new Cast(sc, info, args[0], to));
    }

    // built-in functions
    final StandardFunc fun = get().get(name, args, sc, info);
    if(fun != null) {
      final AnnList anns = new AnnList();
      if(fun.sig.has(Flag.UPD)) {
        anns.add(new Ann(info, Annotation.UPDATING));
        qc.updating();
      }
      return new TypedFunc(fun, anns);
    }

    // user-defined function
    final TypedFunc tf = qc.funcs.getRef(name, args, sc, info);
    if(tf != null) {
      if(tf.anns.contains(Annotation.UPDATING)) qc.updating();
      return tf;
    }

    // Java function
    final JavaFunction jf = JavaFunction.get(name, args, qc, sc, info);
    if(jf != null) return TypedFunc.java(jf);

    // add user-defined function that has not been declared yet
    if(FuncType.find(name) == null) return qc.funcs.getFuncRef(name, args, sc, info);

    // no function found
    return null;
  }

  /**
   * Returns an exception if the name of a built-in function is similar to the specified name.
   * @param name name of input function
   * @param info input info
   * @return query exception or {@code null}
   */
  QueryException similarError(final QNm name, final InputInfo info) {
    // find similar function in three runs
    final byte[] local = name.local(), uri = name.uri();
    final Levenshtein ls = new Levenshtein();
    for(int mode = 0; mode < 3; mode++) {
      for(final byte[] key : this) {
        final int i = indexOf(key, '}');
        final byte[] slocal = substring(key, i + 1), suri = substring(key, 2, i);
        if(mode == 0 ?
          // find functions with identical URIs and similar local names
          eq(uri, suri) && ls.similar(local, slocal) : mode == 1 ?
          // find functions with identical local names
          eq(local, substring(key, i + 1)) :
          // find functions with identical URIs and local names that start with the specified name
          eq(uri, substring(key, 2, i)) && startsWith(substring(key, i + 1), local)) {
          final QNm sim = new QNm(slocal, suri);
          return FUNCSIMILAR_X_X.get(info, name.prefixId(), sim.prefixId());
        }
      }
    }
    return null;
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    funcs = Array.copy(funcs, new Function[s]);
  }
}
