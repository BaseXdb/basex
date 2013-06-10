package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Use;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class provides access to statically available functions.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * Constructor, registering statically available XQuery functions.
   */
  private Functions() {
    for(final Function def : Function.VALUES) {
      final String dsc = def.desc;
      final byte[] ln = token(dsc.substring(0, dsc.indexOf(PAR1)));
      final int i = put(new QNm(ln, def.uri()).id());
      if(funcs[i] != null) Util.notexpected("Function defined twice:" + def);
      funcs[i] = def;
    }
  }

  /**
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param arity number of arguments
   * @param name function name
   * @param ii input info
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Type getCast(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {

    final byte[] ln = name.local();
    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);

    // no constructor function found, or abstract type specified
    if(type != null && type != AtomType.NOT && type != AtomType.AAT) {
      if(arity == 1) return type;
      (arity == 1 ? FUNCTYPESG : FUNCTYPEPL).thrw(ii, name.string(), arity, 1);
    }

    // include similar function name in error message
    final Levenshtein ls = new Levenshtein();
    for(final AtomType t : AtomType.VALUES) {
      if(t.par == null) continue;
      final byte[] u = t.name.uri();
      if(eq(u, XSURI) && t != AtomType.NOT && t != AtomType.AAT && ls.similar(
          lc(ln), lc(t.string()))) FUNCSIMILAR.thrw(ii, name.string(), t.string());
    }
    // no similar name: constructor function found, or abstract type specified
    throw FUNCUNKNOWN.thrw(ii, name.string());
  }

  /**
   * Tries to resolve the specified function as a built-in one.
   * @param name function name
   * @param arity number of arguments
   * @param ii input info
   * @return function spec if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private Function getBuiltIn(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {
    final int id = id(name.id());
    if(id == 0) return null;
    final Function fl = funcs[id];
    if(!eq(fl.uri(), name.uri())) return null;
    // check number of arguments
    if(arity < fl.min || arity > fl.max) FUNCARGS.thrw(ii, fl, arity);
    return fl;
  }

  /**
   * Returns the specified function.
   * @param name function qname
   * @param args optional arguments
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public StandardFunc get(final QNm name, final Expr[] args, final InputInfo ii)
      throws QueryException {
    final Function fl = getBuiltIn(name, args.length, ii);
    return fl == null ? null : fl.get(ii, args);
  }

  /**
   * Gets a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param ctx query context
   * @param ii input info
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr getLiteral(final QNm name, final int arity,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    // parse data type constructors
    if(eq(name.uri(), XSURI)) {
      final Type type = getCast(name, arity, ii);
      final VarScope scp = new VarScope();
      final Var[] args = { scp.uniqueVar(ctx, SeqType.AAT_ZO, true) };
      final Expr e = new Cast(ii, new VarRef(ii, args[0]), type.seqType());
      final FuncType tp = FuncType.get(e.type(), SeqType.AAT_ZO);
      return new FuncItem(name, args, e, tp, scp, ctx.sc, null);
    }

    // pre-defined functions
    final Function fn = get().getBuiltIn(name, arity, ii);
    if(fn != null) {
      final VarScope scp = new VarScope();
      final FuncType ft = fn.type(arity);
      final QNm[] names = new QNm[arity];
      for(int a = 0; a < arity; a++) names[a] = new QNm("value" + (a + 1));
      final Var[] args = new Var[arity];
      final Expr[] calls = ft.args(args, ctx, scp, ii);

      final StandardFunc f = fn.get(calls);
      if(!f.uses(Use.CTX) && !f.uses(Use.POS))
        return new FuncItem(name, args, f, ft, scp, ctx.sc, null);

      return new FuncLit(name, args, f, ft, scp, ctx.sc, ii);
    }

    // user-defined function
    final StaticFunc sf = ctx.funcs.get(name, arity, ii);
    if(sf != null) {
      final FuncType ft = sf.funcType();
      final VarScope scp = new VarScope();
      final QNm[] names = new QNm[arity];
      for(int a = 0; a < arity; a++) names[a] = sf.args[a].name;
      final Var[] args = new Var[arity];
      final Expr[] calls = ft.args(args, ctx, scp, ii);
      final TypedFunc tf = ctx.funcs.getFuncRef(name, calls, ctx.sc, ii);
      return new FuncItem(name, args, tf.fun, ft, scp, ctx.sc, sf);
    }

    // Java function (only allowed with administrator permissions)
    final VarScope scp = new VarScope();
    final FuncType jt = FuncType.arity(arity);
    final Var[] vs = new Var[arity];
    final Expr[] refs = jt.args(vs, ctx, scp, ii);
    final Expr jm = JavaMapping.get(name, refs, ctx, ii);
    if(jm != null) return new FuncLit(name, vs, jm, jt, scp, ctx.sc, ii);

    return null;
  }

  /**
   * Returns an instance of a with the specified name and number of arguments,
   * or {@code null}.
   * @param name name of the function
   * @param args optional arguments
   * @param dyn compile-/run-time flag
   * @param ctx query context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public static TypedFunc get(final QNm name, final Expr[] args, final boolean dyn,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    // get namespace and local name
    // parse data type constructors
    if(eq(name.uri(), XSURI)) {
      final Type type = getCast(name, args.length, ii);
      final SeqType to = SeqType.get(type, Occ.ZERO_ONE);
      return TypedFunc.constr(new Cast(ii, args[0], to), to);
    }

    // pre-defined functions
    final StandardFunc fun = Functions.get().get(name, args, ii);
    if(fun != null) {
      if(!ctx.sc.xquery3() && fun.xquery3()) FUNC30.thrw(ii);
      for(final Function f : Function.UPDATING) {
        if(fun.sig == f) {
          ctx.updating(true);
          break;
        }
      }
      // [LW] correct annotations
      return new TypedFunc(fun, new Ann(), fun.sig.type(args.length));
    }

    // user-defined function
    final TypedFunc tf = ctx.funcs.getRef(name, args, ctx.sc, ii);
    if(tf != null) return tf;

    // Java function (only allowed with administrator permissions)
    final JavaMapping jf = JavaMapping.get(name, args, ctx, ii);
    if(jf != null) return TypedFunc.java(jf);

    // add user-defined function that has not been declared yet
    if(!dyn && FuncType.find(name) == null) {
      return ctx.funcs.getFuncRef(name, args, ctx.sc, ii);
    }

    // no function found
    return null;
  }

  /**
   * Throws an error if the name of a pre-defined functions is similar to the
   * specified function name.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  void errorIfSimilar(final QNm name, final InputInfo ii) throws QueryException {
    // compare specified name with names of predefined functions
    final byte[] ln = name.local();
    final Levenshtein ls = new Levenshtein();
    for(int k = 1; k < size; ++k) {
      final int i = indexOf(keys[k], '}');
      final byte[] u = substring(keys[k], 2, i);
      final byte[] l = substring(keys[k], i + 1);
      if(eq(ln, l)) {
        final byte[] ur = name.uri();
        FUNCSIMILAR.thrw(ii,
            new TokenBuilder(NSGlobal.prefix(ur)).add(':').add(l),
            new TokenBuilder(NSGlobal.prefix(u)).add(':').add(l));
      } else if(ls.similar(ln, l)) {
        FUNCSIMILAR.thrw(ii, name.string(), l);
      }
    }
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    funcs = Array.copy(funcs, new Function[s]);
  }
}
