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
  private TokenObjMap<StaticFunc> funcs = new TokenObjMap<StaticFunc>();
  /** Cached function calls. */
  private TokenObjMap<ArrayList<StaticFuncCall>> calls =
      new TokenObjMap<ArrayList<StaticFuncCall>>();

  /**
   * Returns the specified function.
   * @param name name of the function
   * @param args optional arguments
   * @param ii input info
   * @return function instance
   */
  TypedFunc get(final QNm name, final Expr[] args, final InputInfo ii) {
    // check if function has already been declared
    final byte[] sig = sig(name, args.length);
    final StaticFunc sf = funcs.get(sig);
    if(sf == null) return null;

    final StaticFuncCall call = add(ii, sf.name, sig, args);
    final FuncType type = FuncType.get(sf.args, sf.ret);
    return new TypedFunc(call, sf.ann, type);
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
    final StaticFuncCall call = add(ii, name, add(uf, ii), args);
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
    for(int id = 1; id <= fs; ++id) sf[id - 1] = funcs.value(id);
    return sf;
  }

  /**
   * Registers and returns a new function call.
   * @param ii input info
   * @param nm function name
   * @param sig function signature
   * @param arg arguments
   * @return new function call
   */
  private StaticFuncCall add(final InputInfo ii, final QNm nm, final byte[] sig,
      final Expr[] arg) {

    final StaticFuncCall call = new BaseFuncCall(ii, nm, arg);
    final StaticFunc sf = funcs.get(sig);
    if(sf.declared) call.init(sf);
    calls.get(sig).add(call);
    return call;
  }

  /**
   * Adds a local function.
   * @param fun function instance
   * @param ii input info
   * @return function id
   * @throws QueryException query exception
   */
  public byte[] add(final StaticFunc fun, final InputInfo ii) throws QueryException {
    final QNm name = fun.name;
    final byte[] uri = name.uri();
    if(uri.length == 0) FUNNONS.thrw(ii, name.string());

    if(NSGlobal.reserved(uri)) {
      if(fun.declared) NAMERES.thrw(ii, name.string());
      funError(name, ii);
    }

    final byte[] sig = sig(name, fun.args.length);
    final StaticFunc sf = funcs.get(sig);
    if(sf != null) {
      // declare function that has been called before
      if(!sf.declared) {
        funcs.add(sig, fun);
        return sig;
      }
      // duplicate declaration
      FUNCDEFINED.thrw(ii, fun.name.string());
    }

    // add function skeleton
    funcs.add(sig, fun);
    calls.add(sig, new ArrayList<StaticFuncCall>(0));
    return sig;
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
      final StaticFunc sf = funcs.value(id);
      final ArrayList<StaticFuncCall> sfc = calls.value(id);
      qc.updating |= sf.updating && !sfc.isEmpty();
      for(final StaticFuncCall c : sfc) c.init(sf);
    }

    for(int id = 1; id <= fs; ++id) {
      final StaticFunc sf = funcs.value(id);
      if(!sf.declared || sf.expr == null) {
        // function has not been declared yet
        for(int i = 1; i <= fs; ++i) {
          final StaticFunc uf = funcs.value(i);
          // check if another function with same name exists
          if(sf != uf && sf.name.eq(uf.name)) FUNCTYPE.thrw(sf.info, uf.name.string());
        }
        // if not, indicate that function is unknown
        FUNCUNKNOWN.thrw(sf.info, sf.name.string());
      }
    }
  }

  /**
   * Checks if the function performs updates.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) funcs.value(id).checkUp();
  }

  /**
   * Compiles the functions.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void compile(final QueryContext ctx) throws QueryException {
    // only compile those functions that are used
    final int fs = funcs.size();
    for(int id = 0; id < fs; id++) {
      if(!calls.value(id).isEmpty()) funcs.value(id).compile(ctx);
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
      final StaticFunc sf = funcs.value(id);
      if(ls.similar(nm, lc(sf.name.local()), 0)) {
        FUNSIMILAR.thrw(ii, name.string(), sf.name.string());
      }
    }
  }

  @Override
  public void plan(final FElem plan) {
    final int fs = funcs.size();
    if(fs != 0) addPlan(plan, planElem(), funcs());
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
}
