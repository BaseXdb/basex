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
public final class StaticFuncs extends ExprInfo {
  /** User-defined functions. */
  private final TokenObjMap<FuncCache> funcs = new TokenObjMap<FuncCache>();

  /**
   * returns the signature of the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @return the function's signature
   */
  protected static byte[] sig(final QNm name, final long arity) {
    return new TokenBuilder(name.id()).add('#').add(Token.token(arity)).finish();
  }

  /**
   * Declares a new user-defined function.
   * @param ann annotations
   * @param nm function name
   * @param args formal parameters
   * @param ret return type
   * @param body function body
   * @param sc static context
   * @param scp variable scope
   * @param xqdoc current xqdoc cache
   * @param ii input info
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final Ann ann, final QNm nm, final Var[] args,
      final SeqType ret, final Expr body, final StaticContext sc, final VarScope scp,
      final String xqdoc, final InputInfo ii) throws QueryException {

    final byte[] uri = nm.uri();
    if(uri.length == 0) FUNNONS.thrw(ii, nm.string());
    if(NSGlobal.reserved(uri)) NAMERES.thrw(ii, nm.string());

    final StaticFunc fn = new StaticFunc(ann, nm, args, ret, body, sc, scp, xqdoc, ii);
    final byte[] sig = fn.id();
    final FuncCache fc = funcs.get(sig);
    if(fc != null) fc.setFunc(fn);
    else funcs.put(sig, new FuncCache(fn));
    return fn;
  }

  /**
   * Creates a reference to an already declared or referenced function.
   * @param name name of the function
   * @param args optional arguments
   * @param sc static context
   * @param ii input info
   * @return reference if the function is known, {@code false} otherwise
   * @throws QueryException query exception
   */
  TypedFunc getRef(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    // check if function has already been declared
    final FuncCache fc = funcs.get(sig(name, args.length));
    return fc == null ? null : fc.newCall(name, args, sc, ii);
  }

  /**
   * Returns a new reference to the function with the given name and arity.
   * @param name function name
   * @param args argument expressions
   * @param sc static context of the function call
   * @param ii input info
   * @return function call
   * @throws QueryException query exception
   */
  public TypedFunc getFuncRef(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    if(NSGlobal.reserved(name.uri())) errorIfSimilar(name, ii);
    final byte[] sig = sig(name, args.length);
    if(!funcs.contains(sig)) funcs.put(sig, new FuncCache(null));
    return getRef(name, args, sc, ii);
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
      final StaticFuncCall call = fc.calls.isEmpty() ? null : fc.calls.get(0);
      if(fc.func == null) {
        // check if another function with same name exists
        for(int i = 1; i <= fs; ++i) {
          if(i == id) continue;
          final FuncCache ofc = funcs.value(i);
          if(call.name.eq(ofc.name())) FUNCTYPE.thrw(call.info, call.name.string(),
              call.expr.length);
        }
        // if not, indicate that function is unknown
        FUNCUNKNOWN.thrw(call.info, call.name.string());
      }
      if(call != null) {
        if(fc.func.expr == null) FUNCNOIMPL.thrw(call.info, call.name.string());
        qc.updating |= fc.func.updating;
      }
    }
  }

  /**
   * Checks if the functions perform updates.
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
   * Returns the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @param ii input info
   * @return function if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public StaticFunc get(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {

    if(NSGlobal.reserved(name.uri())) errorIfSimilar(name, ii);
    final FuncCache fc = funcs.get(sig(name, arity));
    return fc == null ? null : fc.func;
  }

  /**
   * Throws an error if the name of a function is similar to the specified function name.
   * @param name function name
   * @param ii input info
   * @throws QueryException query exception
   */
  public void errorIfSimilar(final QNm name, final InputInfo ii) throws QueryException {
    // find global function
    Functions.get().errorIfSimilar(name, ii);
    // find local functions
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.local());
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) {
      final StaticFunc sf = funcs.value(id).func;
      if(sf != null && sf.expr != null && ls.similar(nm, lc(sf.name.local()))) {
        FUNCSIMILAR.thrw(ii, name.string(), sf.name.string());
      }
    }
  }

  @Override
  public void plan(final FElem plan) {
    if(!funcs.isEmpty()) addPlan(plan, planElem(), funcs());
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final int fs = funcs.size();
    for(int id = 1; id <= fs; ++id) {
      sb.append(funcs.value(id).func.toString()).append(Text.NL);
    }
    return sb.toString();
  }

  /** Function cache. */
  private static class FuncCache {
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

    /**
     * Assigns the given function to all of its references and checks their visibility.
     * @param fn function to assign
     * @throws QueryException query exception
     */
    public void setFunc(final StaticFunc fn) throws QueryException {
      if(func != null) throw FUNCDEFINED.thrw(fn.info, fn.name.string());
      func = fn;
      for(final StaticFuncCall call : calls) {
        call.init(fn);
      }
    }

    /**
     * Creates a new call to this function.
     * @param nm function name
     * @param args argument expressions
     * @param sc static context
     * @param ii input info
     * @return function call
     * @throws QueryException query exception
     */
    public TypedFunc newCall(final QNm nm, final Expr[] args, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      final StaticFuncCall call = new BaseFuncCall(nm, args, sc, ii);
      calls.add(call);

      if(func == null) {
        // [LW] should be deferred until the actual types are known (i.e. compile time)
        return new TypedFunc(call, new Ann(), FuncType.arity(args.length));
      }
      call.init(func);
      return new TypedFunc(call, func.ann, func.funcType());
    }

    /**
     * Returns the function's name.
     * @return function name
     */
    public QNm name() {
      return func != null ? func.name : calls.get(0).name;
    }
  }
}
