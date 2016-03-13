package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Container for user-defined functions.
 *
 * @author BaseX Team 2005-16, BSD License
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
  static byte[] sig(final QNm name, final int arity) {
    return new TokenBuilder(name.prefixId()).add('#').addInt(arity).finish();
  }

  /**
   * Declares a new user-defined function.
   * @param anns annotations
   * @param nm function name
   * @param args formal parameters
   * @param type declared return type
   * @param expr function body
   * @param sc static context
   * @param scope variable scope
   * @param doc current xqdoc cache
   * @param ii input info
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final AnnList anns, final QNm nm, final Var[] args, final SeqType type,
      final Expr expr, final StaticContext sc, final VarScope scope, final String doc,
      final InputInfo ii) throws QueryException {

    final byte[] uri = nm.uri();
    if(uri.length == 0) throw FUNNONS_X.get(ii, nm.string());
    if(NSGlobal.reserved(uri)) throw NAMERES_X.get(ii, nm.string());

    final StaticFunc sf = new StaticFunc(anns, nm, args, type, expr, sc, scope, doc, ii);
    final byte[] sig = sf.id();
    final FuncCache fc = funcs.get(sig);
    if(fc != null) fc.setFunc(sf);
    else funcs.put(sig, new FuncCache(sf));
    return sf;
  }

  /**
   * Creates a reference to an already declared or referenced function.
   * @param name name of the function
   * @param args optional arguments
   * @param sc static context
   * @param ii input info
   * @return reference if the function is known, {@code null} otherwise
   * @throws QueryException query exception
   */
  TypedFunc getRef(final QNm name, final Expr[] args, final StaticContext sc, final InputInfo ii)
      throws QueryException {

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
  TypedFunc getFuncRef(final QNm name, final Expr[] args, final StaticContext sc,
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
   * Registers a literal for a function that was not yet encountered during parsing.
   * @param lit the literal
   */
  public void registerFuncLit(final Closure lit) {
    final byte[] sig = sig(lit.funcName(), lit.arity());
    FuncCache cache = funcs.get(sig);
    if(cache == null) {
      cache = new FuncCache(null);
      funcs.put(sig, cache);
    }
    cache.lits.add(lit);
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
          int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
          for(int a = 0; a < as; a++) {
            final int m = al.get(a);
            if(m < min) min = m;
            if(m > max) max = m;
          }
          if(as > 1 && max - min + 1 == as) {
            exp.append(min).append('-').append(max);
          } else {
            for(int a = 0; a < as; a++) {
              if(a != 0) exp.append(a + 1 < as ? ", " : " or ");
              exp.append(al.get(a));
            }
          }
          final int ar = call.exprs.length;
          throw FUNCTYPES_X_X_X_X.get(call.info, call.name.string(), ar, ar == 1 ? "" : "s", exp);
        }

        // if not, indicate that function is unknown
        final QueryException qe = similarError(call.name, call.info);
        throw qe == null ? WHICHFUNC_X.get(call.info, call.name.prefixId()) : qe;
      }

      if(call != null) {
        if(fc.func.expr == null) throw FUNCNOIMPL_X.get(call.info, call.name.string());
        // set updating flag; this will trigger checks in {@link QueryContext#check}
        qc.updating |= fc.func.updating;
      }
      id++;
    }
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final FuncCache fc : funcs.values()) fc.func.checkUp();
  }

  /**
   * Compiles the used functions.
   * @param qc query context
   */
  public void compile(final QueryContext qc) {
    compile(qc, false);
  }

  /**
   * Compiles all functions.
   * @param qc query context
   * @param all compile all functions (not only used ones)
   */
  public void compile(final QueryContext qc, final boolean all) {
    // only compile those functions that are used
    for(final FuncCache fc : funcs.values()) {
      if(all || !fc.calls.isEmpty()) fc.func.compile(qc);
    }
  }

  /**
   * Returns the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @param ii input info
   * @param error raise error if function in reserved namespace is not found
   * @return function if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public StaticFunc get(final QNm name, final int arity, final InputInfo ii, final boolean error)
      throws QueryException {

    final FuncCache fc = funcs.get(sig(name, arity));
    if(fc != null) return fc.func;

    if(error && NSGlobal.reserved(name.uri())) {
      final QueryException qe = similarError(name, ii);
      throw qe == null ? WHICHFUNC_X.get(ii, name.prefixId()) : qe;
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
    // find local functions
    QueryException qe = null;
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.local());
    for(final FuncCache fc : funcs.values()) {
      final StaticFunc sf = fc.func;
      if(sf != null && sf.expr != null && ls.similar(nm, lc(sf.name.local()))) {
        qe = FUNCSIMILAR_X_X.get(ii, name.prefixId(), sf.name.prefixId());
        break;
      }
    }
    // find global function
    if(qe == null) qe = Functions.get().similarError(name, ii);
    return qe;
  }

  @Override
  public void plan(final FElem plan) {
    if(!funcs.isEmpty()) {
      final FElem el = planElem();
      plan.add(el);
      for(final StaticFunc f : funcs()) {
        if(f != null) f.plan(el);
      }
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
    /** Function literals. */
    final ArrayList<Closure> lits = new ArrayList<>(0);
    /** Function. */
    StaticFunc func;

    /**
     * Constructor.
     * @param func function
     */
    FuncCache(final StaticFunc func) {
      this.func = func;
    }

    /**
     * Assigns the given function to all of its references and checks their visibility.
     * @param sf function to assign
     * @throws QueryException query exception
     */
    public void setFunc(final StaticFunc sf) throws QueryException {
      if(func != null) throw FUNCDEFINED_X.get(sf.info, sf.name.string());
      func = sf;
      for(final StaticFuncCall call : calls) call.init(sf);
      final FuncType ft = sf.funcType();
      for(final Closure lit : lits) lit.adoptSignature(ft);
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
        return new TypedFunc(call, new AnnList());
      }
      return new TypedFunc(call.init(func), func.anns);
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
