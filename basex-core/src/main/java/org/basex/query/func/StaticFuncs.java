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
import org.basex.util.list.*;

/**
 * Container for a user-defined function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncs extends ExprInfo {
  /** User-defined functions. */
  private final TokenObjMap<FuncCache> funcs = new TokenObjMap<>();

  /**
   * returns the signature of the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @return the function's signature
   */
  static byte[] sig(final QNm name, final long arity) {
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
  public StaticFunc declare(final Ann ann, final QNm nm, final Var[] args, final SeqType ret,
      final Expr body, final StaticContext sc, final VarScope scp, final String xqdoc,
      final InputInfo ii) throws QueryException {

    final byte[] uri = nm.uri();
    if(uri.length == 0) throw FUNNONS.get(ii, nm.string());
    if(NSGlobal.reserved(uri)) throw NAMERES.get(ii, nm.string());

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

    if(NSGlobal.reserved(name.uri())) {
      final QueryException qe = similarError(name, ii);
      if(qe != null) throw qe;
    }
    final byte[] sig = sig(name, args.length);
    if(!funcs.contains(sig)) funcs.put(sig, new FuncCache(null));
    return getRef(name, args, sc, ii);
  }

  /**
   * Checks if all functions have been correctly declared, and initializes all function calls.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext qc) throws QueryException {
    // initialize function calls
    int id = 0;
    for(final FuncCache fc : funcs.values()) {
      final StaticFuncCall call = fc.calls.isEmpty() ? null : fc.calls.get(0);
      if(fc.func == null) {
        // check if another function with same name exists
        int oid = 0;
        final IntList al = new IntList();
        for(final FuncCache ofc : funcs.values()) {
          if(oid++ == id) continue;
          if(ofc.func != null && call.name.eq(ofc.name())) al.add(ofc.func.arity());
        }
        if(!al.isEmpty()) {
          final StringBuilder exp = new StringBuilder();
          final int as = al.size();
          for(int a = 0; a < as; a++) {
            if(a != 0) exp.append(a + 1 < as ? "," : " or ");
            exp.append(al.get(a));
          }
          final int a = call.exprs.length;
          throw (a == 1 ? FUNCTYPESG : FUNCTYPEPL).get(call.info, call.name.string(), a, exp);
        }

        // if not, indicate that function is unknown
        final QueryException qe = similarError(call.name, call.info);
        throw qe == null ? FUNCUNKNOWN.get(call.info, call.name.string()) : qe;
      }

      if(call != null) {
        if(fc.func.expr == null) throw FUNCNOIMPL.get(call.info, call.name.string());
        // set updating flag; this will trigger checks in {@link QueryContext#check}
        qc.updating |= fc.func.updating;
      }
      id++;
    }
  }

  /**
   * Checks if the functions perform updates.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final FuncCache fc : funcs.values()) fc.func.checkUp();
  }

  /**
   * Compiles the functions.
   * @param qc query context
   */
  public void compile(final QueryContext qc) {
    // only compile those functions that are used
    for(final FuncCache fc : funcs.values()) {
      if(!fc.calls.isEmpty()) fc.func.compile(qc);
    }
  }

  /**
   * Returns the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @param ii input info
   * @param error raise error if function is not found
   * @return function if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public StaticFunc get(final QNm name, final long arity, final InputInfo ii, final boolean error)
      throws QueryException {

    final FuncCache fc = funcs.get(sig(name, arity));
    if(fc != null) return fc.func;

    if(error && NSGlobal.reserved(name.uri())) {
      final QueryException qe = similarError(name, ii);
      if(qe != null) throw qe;
    }
    return null;
  }

  /**
   * Throws an exception if the name of a function is similar to the specified function name.
   * @param name function name
   * @param ii input info
   * @return exception
   */
  public QueryException similarError(final QNm name, final InputInfo ii) {
    // find global function
    QueryException qe = Functions.get().similarError(name, ii);
    if(qe == null) {
      // find local functions
      final Levenshtein ls = new Levenshtein();
      final byte[] nm = lc(name.local());
      for(final FuncCache fc : funcs.values()) {
        final StaticFunc sf = fc.func;
        if(sf != null && sf.expr != null && ls.similar(nm, lc(sf.name.local()))) {
          qe = FUNCSIMILAR.get(ii, name.string(), sf.name.string());
          break;
        }
      }
    }
    return qe;
  }

  @Override
  public void plan(final FElem plan) {
    if(!funcs.isEmpty()) {
      final FElem el = planElem();
      plan.add(el);
      for(final StaticFunc f : funcs())
        if(f != null && f.compiled()) f.plan(el);
    }
  }

  /**
   * Returns all user-defined functions.
   * @return functions
   */
  public StaticFunc[] funcs() {
    final int fs = funcs.size();
    final StaticFunc[] sf = new StaticFunc[fs];
    int i = 0;
    for(final FuncCache fc : funcs.values()) sf[i++] = fc.func;
    return sf;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final FuncCache fc : funcs.values()) {
      if(fc.func != null && fc.func.compiled()) {
        sb.append(fc.func).append(Text.NL);
      }
    }
    return sb.toString();
  }

  /** Function cache. */
  private static class FuncCache {
    /** Function calls. */
    final ArrayList<StaticFuncCall> calls = new ArrayList<>(0);
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
      if(func != null) throw FUNCDEFINED.get(fn.info, fn.name.string());
      func = fn;
      for(final StaticFuncCall call : calls) call.init(fn);
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
      final StaticFuncCall call = new StaticFuncCall(nm, args, sc, ii);
      calls.add(call);

      if(func == null) {
        // [LW] should be deferred until the actual types are known (i.e. compile time)
        return new TypedFunc(call, new Ann());
      }
      return new TypedFunc(call.init(func), func.ann);
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
