package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
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
  private Function[] funcs = new Function[CAP];

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
    for(final Function def : Function.values()) {
      final String dsc = def.desc;
      final byte[] ln = token(dsc.substring(0, dsc.indexOf(PAR1)));
      final int i = add(new QNm(ln, def.uri()).id());
      if(i < 0) Util.notexpected("Function defined twice:" + def);
      funcs[i] = def;
    }
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

    final int id = id(name.id());
    if(id == 0) return null;

    // create function
    final Function fl = funcs[id];
    if(!eq(fl.uri(), name.uri())) return null;

    final StandardFunc f = fl.get(ii, args);
    // check number of arguments
    if(args.length < fl.min || args.length > fl.max) XPARGS.thrw(ii, fl);
    return f;
  }

  /**
   * Returns the specified function literal.
   * @param name function name
   * @param arity number of arguments
   * @param dyn dynamic invocation flag
   * @param ctx query context
   * @param ii input info
   * @return literal function expression
   * @throws QueryException query exception
   */
  public static FItem get(final QNm name, final long arity, final boolean dyn,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    final Expr[] args = new Expr[(int) arity];
    final Var[] vars = new Var[args.length];
    for(int i = 0; i < args.length; i++) {
      vars[i] = ctx.uniqueVar(ii, null);
      args[i] = new VarRef(ii, vars[i]);
    }

    final TypedFunc f = get(name, args, dyn, ctx, ii);
    if(f == null) {
      if(!dyn) FUNCUNKNOWN.thrw(ii, name + "#" + arity);
      return null;
    }

    // compile the function if it hasn't been done statically
    if(dyn && f.fun instanceof UserFuncCall) {
      final UserFunc usf = ((UserFuncCall) f.fun).func();
      if(usf != null && usf.declared) usf.compile(ctx);
    }

    final FuncType ft = f.type;
    return new FuncItem(name, vars, f.fun, ft, false);
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
      final byte[] ln = name.local();
      final AtomType type = AtomType.find(name, false);
      if(type == null) {
        final Levenshtein ls = new Levenshtein();
        for(final AtomType t : AtomType.values()) {
          if(t.par != null && t != AtomType.NOT && t != AtomType.AAT &&
              ls.similar(lc(ln), lc(t.string()), 0))
            FUNSIMILAR.thrw(ii, name.string(), t.string());
        }
      }
      // no constructor function found, or abstract type specified
      if(type == null || type == AtomType.NOT || type == AtomType.AAT) {
        FUNCUNKNOWN.thrw(ii, name.string());
      }

      if(args.length != 1) FUNCTYPE.thrw(ii, name.string());
      final SeqType to = SeqType.get(type, Occ.ZERO_ONE);
      return TypedFunc.constr(new Cast(ii, args[0], to), to);
    }

    // pre-defined functions
    final StandardFunc fun = Functions.get().get(name, args, ii);
    if(fun != null) {
      if(!ctx.sc.xquery3 && fun.xquery3()) FEATURE30.thrw(ii);
      for(final Function f : Function.UPDATING) {
        if(fun.sig == f) {
          ctx.updating(true);
          break;
        }
      }
      return new TypedFunc(fun, fun.sig.type(args.length));
    }

    // user-defined function
    final TypedFunc tf = ctx.funcs.get(name, args, ii);
    if(tf != null) return tf;

    // Java function (only allowed with administrator permissions)
    final JavaMapping jf = JavaMapping.get(name, args, ctx, ii);
    if(jf != null) return TypedFunc.java(jf);

    // add user-defined function that has not been declared yet
    if(!dyn && FuncType.find(name) == null) return ctx.funcs.add(name, args, ii, ctx);

    // no function found
    return null;
  }

  /**
   * Throws an error if one of the pre-defined functions is similar to the
   * specified function name.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  public void error(final QNm name, final InputInfo ii) throws QueryException {
    // compare specified name with names of predefined functions
    final byte[] ln = name.local();
    final Levenshtein ls = new Levenshtein();
    for(int k = 1; k < size; ++k) {
      final int i = indexOf(keys[k], '}');
      final byte[] u = substring(keys[k], 2, i);
      final byte[] l = substring(keys[k], i + 1);
      if(eq(ln, l)) {
        final byte[] ur = name.uri();
        FUNSIMILAR.thrw(ii,
            new TokenBuilder(NSGlobal.prefix(ur)).add(':').add(l),
            new TokenBuilder(NSGlobal.prefix(u)).add(':').add(l));
      } else if(ls.similar(ln, l, 0)) {
        FUNSIMILAR.thrw(ii, name.string(), l);
      }
    }
  }

  @Override
  protected void rehash() {
    super.rehash();
    funcs = Arrays.copyOf(funcs, size << 1);
  }
}
