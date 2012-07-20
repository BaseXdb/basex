package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Container for a user-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class UserFuncs extends ExprInfo {
  /** User-defined functions. */
  private UserFunc[] funcs = { };
  /** Cached function calls. */
  private UserFuncCall[][] calls = { };

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @param ii input info
   * @return function instance
   */
  TypedFunc get(final QNm name, final Expr[] args, final InputInfo ii) {
    final int id = indexOf(name, args);
    if(id == -1) return null;

    // function has already been declared
    final UserFuncCall call = add(ii, funcs[id].name, id, args);
    final FuncType type = FuncType.get(funcs[id]);
    return new TypedFunc(call, type);
  }

  /**
   * Adds and returns a user-defined function that has not been defined yet.
   * @param name name of the function
   * @param args optional arguments
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  TypedFunc add(final QNm name, final Expr[] args, final InputInfo ii)
      throws QueryException {

    // add function call for function that has not been declared yet
    final int al = args.length;
    final UserFunc uf = new UserFunc(ii, name, new Var[al], null, null, false);
    final UserFuncCall call = add(ii, name, add(uf, ii), args);
    final FuncType type = FuncType.arity(al);
    return new TypedFunc(call, type);
  }

  /**
   * Returns an index to the specified function, or {@code -1}.
   * @param name name of the function
   * @param args optional arguments
   * @return function instance
   */
  private int indexOf(final QNm name, final Expr[] args) {
    for(int id = 0; id < funcs.length; ++id) {
      if(name.eq(funcs[id].name) && args.length == funcs[id].args.length) return id;
    }
    return -1;
  }

  /**
   * Returns all user-defined functions.
   * @return function array
   */
  public UserFunc[] funcs() {
    return funcs;
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
    if(funcs[id].declared) call.init(funcs[id]);
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
    for(int l = 0; l < funcs.length; ++l) {
      final QNm qn = funcs[l].name;
      final byte[] u = qn.uri();
      final byte[] nm = qn.local();

      if(eq(ln, nm) && eq(uri, u) && fun.args.length == funcs[l].args.length) {
        // declare function that has been called before
        if(!funcs[l].declared) {
          funcs[l] = fun;
          return l;
        }
        // duplicate declaration
        FUNCDEFINED.thrw(ii, fun.name.string());
      }
    }
    // add function skeleton
    funcs = Array.add(funcs, fun);
    calls = Array.add(calls, new UserFuncCall[0]);
    return funcs.length - 1;
  }

  /**
   * Checks if all functions have been correctly declared, and initializes
   * all function calls.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    // initialize function calls
    for(int i = 0; i < funcs.length; ++i) {
      for(final UserFuncCall c : calls[i]) c.init(funcs[i]);
    }

    for(final UserFunc f : funcs) {
      if(!f.declared || f.expr == null) {
        // function has not been declared yet
        for(final UserFunc uf : funcs) {
          // check if another function with same name exists
          if(f != uf && f.name.eq(uf.name)) FUNCTYPE.thrw(f.info, uf.name.string());
        }
        // if not, indicate that function is unknown
        FUNCUNKNOWN.thrw(f.info, f.name.string());
      }
    }
  }

  /**
   * Checks if the function performs updates.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final UserFunc f : funcs) f.checkUp();
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void compile(final QueryContext ctx) throws QueryException {
    for(int i = 0; i < funcs.length; i++) {
      // only compile those functions that are used
      if(calls[i].length != 0) funcs[i].compile(ctx);
    }
  }

  /**
   * Finds similar function names and throws an error message.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  public void funError(final QNm name, final InputInfo ii) throws QueryException {
    // find global function
    Functions.get().error(name, ii);

    // find similar local function
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.local());
    for(final UserFunc f : funcs) {
      if(ls.similar(nm, lc(f.name.local()), 0)) {
        FUNSIMILAR.thrw(ii, name.string(), f.name.string());
      }
    }
  }

  @Override
  public void plan(final FElem plan) {
    if(funcs.length != 0) addPlan(plan, planElem(), funcs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final UserFunc f : funcs) sb.append(f.toString());
    return sb.toString();
  }
}
