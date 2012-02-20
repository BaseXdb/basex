package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.User;
import org.basex.data.ExprInfo;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Cast;
import org.basex.query.expr.Expr;
import org.basex.query.expr.VarRef;
import org.basex.query.item.AtomType;
import org.basex.query.item.FItem;
import org.basex.query.item.FuncItem;
import org.basex.query.item.FuncType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.item.Type;
import org.basex.query.item.Types;
import org.basex.query.util.NSGlobal;
import org.basex.query.util.TypedFunc;
import org.basex.query.util.Var;
import org.basex.util.Array;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;

/**
 * Container for a user-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class UserFuncs extends ExprInfo {
  /** Cached function calls. */
  private UserFuncCall[][] calls = { };
  /** Local functions. */
  private UserFunc[] func = { };

  /**
   * Returns the number of functions.
   * @return function
   */
  public int size() {
    return func.length;
  }

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @param dyn compile-/run-time flag
   * @param ctx query context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  public TypedFunc get(final QNm name, final Expr[] args, final boolean dyn,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    // get namespace and local name
    final byte[] uri = name.uri();
    final byte[] ln = name.local();

    // parse data type constructors
    if(eq(uri, XSURI)) {
      final Type type = AtomType.find(name, true);
      if(type == null || type == AtomType.NOT || type == AtomType.AAT) {
        final Levenshtein ls = new Levenshtein();
        for(final AtomType t : AtomType.values()) {
          if(t.par != null && ls.similar(lc(ln), lc(t.string()), 0))
            FUNSIMILAR.thrw(ii, name.string(), t.string());
        }
        FUNCUNKNOWN.thrw(ii, name.string());
      }
      if(args.length != 1) FUNCTYPE.thrw(ii, name.string());
      final SeqType to = SeqType.get(type, Occ.ZO);
      return TypedFunc.constr(new Cast(ii, args[0], to), to);
    }

    // Java function (only allowed with administrator permissions)
    if(startsWith(uri, JAVAPRE) && ctx.context.user.perm(User.ADMIN)) {
      return TypedFunc.java(JavaMapping.get(name, args, ctx, ii));
    }

    // predefined functions
    final StandardFunc fun = Functions.get().get(ln, uri, args, ctx, ii);
    if(fun != null) {
      for(final Function f : Function.UPDATING) {
        if(fun.sig == f) {
          ctx.updating(true);
          break;
        }
      }
      return new TypedFunc(fun, fun.sig.type(args.length));
    }

    // local function
    for(int l = 0; l < func.length; ++l) {
      final QNm qn = func[l].name;
      if(eq(ln, qn.local()) && eq(uri, qn.uri()) && args.length ==
        func[l].args.length) return new TypedFunc(
            add(ii, qn, l, args), FuncType.get(func[l]));
    }

    // add function call for function that has not been defined yet
    if(!dyn && Types.find(name, false) == null) {
      return new TypedFunc(add(ii, name, add(new UserFunc(ii, name,
          new Var[args.length], null, null, false), ii), args),
          FuncType.arity(args.length));
    }
    return null;
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

    final TypedFunc f = ctx.funcs.get(name, args, dyn, ctx, ii);
    if(f == null) {
      if(!dyn) FUNCUNKNOWN.thrw(ii, name + "#" + arity);
      return null;
    }

    // compile the function if it hasn't been done statically
    if(dyn && f.fun instanceof UserFuncCall) {
      final UserFunc usf = ((UserFuncCall) f.fun).func();
      if(usf != null && usf.declared) usf.comp(ctx);
    }

    final FuncType ft = f.type;
    return new FuncItem(name, vars, f.fun, ft, false);
  }

  /**
   * Registers and returns a new function call.
   * @param ii input info
   * @param nm function name
   * @param id function id
   * @param arg arguments
   * @return new function call
   */
  private UserFuncCall add(final InputInfo ii, final QNm nm, final int id,
      final Expr[] arg) {
    final UserFuncCall call = new BaseFuncCall(ii, nm, arg);
    // for dynamic calls
    if(func[id].declared) call.init(func[id]);
    calls[id] = Array.add(calls[id], call);
    return call;
  }

  /**
   * Adds a local function.
   * @param fun function instance
   * @param ii input info
   * @return function id
   * @throws QueryException query exception
   */
  public int add(final UserFunc fun, final InputInfo ii) throws QueryException {
    final QNm name = fun.name;

    final byte[] uri = name.uri();
    if(uri.length == 0) FUNNONS.thrw(ii, name.string());

    if(NSGlobal.reserved(uri)) {
      if(fun.declared) NAMERES.thrw(ii, name.string());
      funError(name, ii);
    }

    final byte[] ln = name.local();
    for(int l = 0; l < func.length; ++l) {
      final QNm qn = func[l].name;
      final byte[] u = qn.uri();
      final byte[] nm = qn.local();

      if(eq(ln, nm) && eq(uri, u) && fun.args.length == func[l].args.length) {
        // declare function that has been called before
        if(!func[l].declared) {
          func[l] = fun;
          return l;
        }
        // duplicate declaration
        FUNCDEFINED.thrw(ii, fun);
      }
    }
    // add function skeleton
    func = Array.add(func, fun);
    calls = Array.add(calls, new UserFuncCall[0]);
    return func.length - 1;
  }

  /**
   * Checks if all functions have been correctly declared, and initializes
   * all function calls.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    // initialize function calls
    for(int i = 0; i < func.length; ++i) {
      for(final UserFuncCall c : calls[i]) c.init(func[i]);
    }
    for(final UserFunc f : func) {
      if(!f.declared || f.expr == null) {
        // function has not been declare yet
        for(final UserFunc uf : func) {
          // check if another function with same name exists
          if(f != uf && f.name.eq(uf.name)) {
            FUNCTYPE.thrw(f.input, uf.name.string());
          }
        }
        // if not, indicate that function is unknown
        FUNCUNKNOWN.thrw(f.input, f.name.string());
      }
      f.checkUp();
    }
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void comp(final QueryContext ctx) throws QueryException {
    for(int i = 0; i < func.length; i++) {
      // only compile those functions that are used
      if(calls[i].length != 0) func[i].comp(ctx);
    }
  }

  /**
   * Finds similar function names and throws an error message.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  public void funError(final QNm name, final InputInfo ii)
      throws QueryException {

    // find global function
    Functions.get().error(name, ii);

    // find similar local function
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.local());
    for(final UserFunc f : func) {
      if(ls.similar(nm, lc(f.name.local()), 0)) {
        FUNSIMILAR.thrw(ii, name.string(), f.name.string());
      }
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(func.length == 0) return;
    ser.openElement(this);
    for(final UserFunc f : func) f.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final UserFunc f : func) sb.append(f.toString());
    return sb.toString();
  }
}
