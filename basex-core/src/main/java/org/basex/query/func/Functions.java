package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
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
 * @author BaseX Team 2005-14, BSD License
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
      if(funcs[i] != null) throw Util.notExpected("Function defined twice: " + def);
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
      throw FUNCTYPEPL.get(ii, name.string(), arity, 1);
    }

    // include similar function name in error message
    final Levenshtein ls = new Levenshtein();
    for(final AtomType t : AtomType.VALUES) {
      if(t.par == null) continue;
      final byte[] u = t.name.uri();
      if(eq(u, XSURI) && t != AtomType.NOT && t != AtomType.AAT && ls.similar(
          lc(ln), lc(t.string()))) throw FUNCSIMILAR.get(ii, name.string(), t.string());
    }
    // no similar name: constructor function found, or abstract type specified
    throw FUNCUNKNOWN.get(ii, name.string());
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
    if(arity >= fl.min && arity <= fl.max) return fl;
    throw (arity == 1 ? FUNCARGSG : FUNCARGPL).get(ii, fl, arity);
  }

  /**
   * Returns the specified function.
   * @param name function qname
   * @param args optional arguments
   * @param sc static context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public StandardFunc get(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final Function fl = getBuiltIn(name, args.length, ii);
    return fl == null ? null : fl.get(sc, ii, args);
  }

  /**
   * Gets a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param ctx query context
   * @param sc static context
   * @param ii input info
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr getLiteral(final QNm name, final int arity, final QueryContext ctx,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    // parse data type constructors
    if(eq(name.uri(), XSURI)) {
      final Type type = getCast(name, arity, ii);
      final VarScope scp = new VarScope(sc);
      final Var[] args = { scp.newLocal(ctx, new QNm(QueryText.ITEMM, ""), SeqType.AAT_ZO, true) };
      final Expr e = new Cast(sc, ii, new VarRef(ii, args[0]), type.seqType());
      final FuncType tp = FuncType.get(e.type(), SeqType.AAT_ZO);
      return new FuncItem(sc, new Ann(), name, args, tp, e, scp.stackSize());
    }

    // pre-defined functions
    final Function fn = get().getBuiltIn(name, arity, ii);
    if(fn != null) {
      final Ann a = new Ann();
      if(fn.has(Flag.UPD)) {
        throw UPFUNCITEM.get(ii);
        //a.add(Ann.Q_UPDATING, Empty.SEQ, ii);
      }
      final VarScope scp = new VarScope(sc);
      final FuncType ft = fn.type(arity);
      final QNm[] argNames = fn.argNames(arity);

      final Var[] args = new Var[arity];
      final Expr[] calls = new Expr[arity];
      for(int i = 0; i < arity; i++) {
        args[i] = scp.newLocal(ctx, argNames[i], ft.args[i], true);
        calls[i] = new VarRef(ii, args[i]);
      }

      final StandardFunc f = fn.get(sc, calls);
      if(!f.has(Flag.CTX) && !f.has(Flag.FCS))
        return new FuncItem(sc, a, name, args, ft, f, scp.stackSize());

      return new FuncLit(a, name, args, f, ft, scp, sc, ii);
    }

    // user-defined function
    final StaticFunc sf = ctx.funcs.get(name, arity, ii, true);
    if(sf != null) {
      final FuncItem fi = getUser(sf, ctx, sc, ii);
      if(fi.annotations().contains(Ann.Q_UPDATING)) throw UPFUNCITEM.get(ii);
      return fi;
    }

    // Java function (only allowed with administrator permissions)
    final VarScope scp = new VarScope(sc);
    final FuncType jt = FuncType.arity(arity);
    final Var[] vs = new Var[arity];
    final Expr[] refs = new Expr[vs.length];
    for(int i = 0; i < vs.length; i++) {
      vs[i] = scp.newLocal(ctx, new QNm(ARG + (i + 1), ""), SeqType.ITEM_ZM, true);
      refs[i] = new VarRef(ii, vs[i]);
    }
    final Expr jm = JavaMapping.get(name, refs, ctx, sc, ii);
    if(jm != null) return new FuncLit(new Ann(), name, vs, jm, jt, scp, sc, ii);

    return null;
  }


  /**
   * Returns a function item for a user-defined function.
   * @param sf static function
   * @param ctx query context
   * @param sc static context
   * @param info input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public static FuncItem getUser(final StaticFunc sf, final QueryContext ctx,
      final StaticContext sc, final InputInfo info) throws QueryException {

    final FuncType ft = sf.funcType();
    final VarScope scp = new VarScope(sc);
    final int arity = sf.args.length;
    final Var[] args = new Var[arity];
    final Expr[] calls = new Expr[args.length];
    for(int i = 0; i < args.length; i++) {
      args[i] = scp.newLocal(ctx, sf.argName(i), ft.args[i], true);
      calls[i] = new VarRef(info, args[i]);
    }
    final TypedFunc tf = ctx.funcs.getFuncRef(sf.name, calls, sc, info);
    return new FuncItem(sc, tf.ann, sf.name, args, ft, tf.fun, scp.stackSize());
  }

  /**
   * Returns a function with the specified name and number of arguments,
   * or {@code null}.
   * @param name name of the function
   * @param args optional arguments
   * @param dyn compile-/run-time flag
   * @param ctx query context
   * @param sc static context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public static TypedFunc get(final QNm name, final Expr[] args, final boolean dyn,
      final QueryContext ctx, final StaticContext sc, final InputInfo ii) throws QueryException {

    // get namespace and local name
    // parse data type constructors
    if(eq(name.uri(), XSURI)) {
      final Type type = getCast(name, args.length, ii);
      final SeqType to = SeqType.get(type, Occ.ZERO_ONE);
      return TypedFunc.constr(new Cast(sc, ii, args[0], to), to);
    }

    // pre-defined functions
    final StandardFunc fun = get().get(name, args, sc, ii);
    if(fun != null) {
      if(!sc.xquery3() && fun.has(Flag.X30)) throw FUNC30.get(ii);
      if(fun.sig.has(Flag.UPD)) ctx.updating(true);
      // [LW] correct annotations
      return new TypedFunc(fun, new Ann(), fun.sig.type(args.length));
    }

    // user-defined function
    final TypedFunc tf = ctx.funcs.getRef(name, args, sc, ii);
    if(tf != null) return tf;

    // Java function (only allowed with administrator permissions)
    final JavaMapping jf = JavaMapping.get(name, args, ctx, sc, ii);
    if(jf != null) return TypedFunc.java(jf);

    // add user-defined function that has not been declared yet
    if(!dyn && FuncType.find(name) == null) {
      return ctx.funcs.getFuncRef(name, args, sc, ii);
    }

    // no function found
    return null;
  }

  /**
   * Returns an exception if the name of a pre-defined functions is similar to the
   * specified function name.
   * @param name function name
   * @param ii input info
   * @return query exception
   */
  QueryException similarError(final QNm name, final InputInfo ii) {
    // compare specified name with names of predefined functions
    final byte[] ln = name.local();
    final Levenshtein ls = new Levenshtein();
    for(int k = 1; k < size; ++k) {
      final int i = indexOf(keys[k], '}');
      final byte[] u = substring(keys[k], 2, i);
      final byte[] l = substring(keys[k], i + 1);
      if(eq(ln, l)) {
        final byte[] prefix = NSGlobal.prefix(name.uri());
        return FUNCSIMILAR.get(ii, prefix.length != 0 ? concat(prefix, token(":"), l) : name.id(),
          new TokenBuilder(NSGlobal.prefix(u)).add(':').add(l));
      } else if(ls.similar(ln, l)) {
        return FUNCSIMILAR.get(ii, name.string(), l);
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
