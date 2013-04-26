package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Container for a user-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class UserFuncs extends ExprInfo {
  /** User-defined functions. */
  private final TokenObjMap<FuncCache> funcs = new TokenObjMap<FuncCache>();

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @param ii input info
   * @return function instance
   */
  TypedFunc get(final QNm name, final Expr[] args, final InputInfo ii) {
    // check if function has already been declared
    final FuncCache fc = funcs.get(sig(name, args.length));
    if(fc == null) return null;

    final StaticFuncCall call = add(ii, fc, args);
    final FuncType type = FuncType.get(fc.func.args, fc.func.ret);
    return new TypedFunc(call, fc.func.ann, type);
  }

  /**
   * Adds and returns a user-defined function that has not been defined yet.
   * @param name name of the function
   * @param args optional arguments
   * @param ii input info
   * @param ctx query context
   * @return function instance
   * @throws QueryException query exception
   */
  TypedFunc add(final QNm name, final Expr[] args, final InputInfo ii,
      final QueryContext ctx) throws QueryException {

    // add function call for function that has not been declared yet
    final int al = args.length;
    final StaticFunc uf = new StaticFunc(ii, name, new Var[al], null, null, false,
        ctx.sc, new VarScope());
    final FuncCache fc = add(uf, ii);
    final StaticFuncCall call = add(ii, fc, args);
    final FuncType type = FuncType.arity(al);
    return new TypedFunc(call, new Ann(), type);
  }

  /**
   * Returns all user-defined functions.
   * @return functions
   */
  public StaticFunc[] funcs() {
    final int fs = funcs.size();
    final StaticFunc[] sf = new StaticFunc[fs];
    for(int id = 1; id <= fs; ++id) sf[id - 1] = funcs.value(id).func;
    return sf;
  }

  /**
   * Adds a local function and returns a function cache.
   * @param fun function instance
   * @param ii input info
   * @return function cache
   * @throws QueryException query exception
   */
  public FuncCache add(final StaticFunc fun, final InputInfo ii) throws QueryException {
    final QNm name = fun.name;
    final byte[] uri = name.uri();
    if(uri.length == 0) FUNNONS.thrw(ii, name.string());

    if(NSGlobal.reserved(uri)) {
      if(fun.declared) NAMERES.thrw(ii, name.string());
      funError(name, ii);
    }

    final byte[] sig = sig(name, fun.args.length);
    FuncCache fc = funcs.get(sig);
    if(fc != null) {
      // declare function that has been called before
      if(!fc.func.declared) {
        fc.func = fun;
        return fc;
      }
      // duplicate declaration
      FUNCDEFINED.thrw(ii, fun.name.string());
    }

    // add function skeleton
    fc = new FuncCache(fun);
    funcs.add(sig, fc);
    return fc;
  }

  /**
   * Checks if all functions have been correctly declared, and initializes
   * all function calls.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext qc) throws QueryException {
    // initialize function calls
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) {
      final FuncCache fc = funcs.value(id);
      final ArrayList<StaticFuncCall> sfc = fc.calls;
      qc.updating |= fc.func.updating && !sfc.isEmpty();
      for(final StaticFuncCall c : sfc) c.init(fc.func);
    }

    // check if all functions have been declared
    for(int id = 1; id <= fs; ++id) {
      final StaticFunc sf = funcs.value(id).func;
      if(sf.declared && sf.expr != null) continue;

      // check if another function with same name exists
      for(int i = 1; i <= fs; ++i) {
        final StaticFunc uf = funcs.value(i).func;
        if(sf != uf && sf.name.eq(uf.name)) FUNCTYPE.thrw(sf.info, sf.name.string());
      }
      // if not, indicate that function is unknown
      FUNCUNKNOWN.thrw(sf.info, sf.name.string());
    }
  }

  /**
   * Checks if the function performs updates.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) funcs.value(id).func.checkUp();
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void compile(final QueryContext ctx) throws QueryException {
    // only compile those functions that are used
    final int fs = funcs.size();
    for(int id = 1; id <= fs; id++) {
      final FuncCache fc = funcs.value(id);
      if(!fc.calls.isEmpty()) fc.func.compile(ctx);
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
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) {
      final StaticFunc sf = funcs.value(id).func;
      if(ls.similar(nm, lc(sf.name.local()), 0)) {
        FUNSIMILAR.thrw(ii, name.string(), sf.name.string());
      }
    }
  }

  @Override
  public void plan(final FElem plan) {
    if(!funcs.isEmpty()) addPlan(plan, planElem(), funcs());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) {
      sb.append(funcs.value(id).toString()).append(Text.NL);
    }
    return sb.toString();
  }

  /**
   * Registers and returns a new function call.
   * @param ii input info
   * @param fc function cache
   * @param arg arguments
   * @return new function call
   */
  private StaticFuncCall add(final InputInfo ii, final FuncCache fc, final Expr[] arg) {
    final StaticFuncCall call = new BaseFuncCall(ii, fc.func.name, arg);
    if(fc.func.declared) call.init(fc.func);
    fc.calls.add(call);
    return call;
  }

  /**
   * Creates a function signature.
   * @param name name
   * @param n number of arguments
   * @return signature
   */
  private byte[] sig(final QNm name, final int n) {
    return new TokenBuilder(name.id()).add('#').add(Token.token(n)).finish();
  }

  /** Function cache. */
  static class FuncCache {
    /** Function calls. */
    ArrayList<StaticFuncCall> calls = new ArrayList<StaticFuncCall>(0);
    /** Function. */
    StaticFunc func;

    /**
     * Constructor.
     * @param sf function
     */
    FuncCache(final StaticFunc sf) {
      func = sf;
    }
  }
}
