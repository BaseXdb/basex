package org.basex.query.func;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.java.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Container for user-defined functions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncs extends ExprInfo {
  /** User-defined functions. */
  private final TokenObjMap<FuncCache> funcs = new TokenObjMap<>();
  /** User-defined function calls. */
  private Map<StaticFunc, ArrayList<StaticFuncCall>> callsMap;

  /**
   * Declares a new user-defined function.
   * @param qname function name
   * @param params parameters with variables and optional default values
   * @param expr function body (can be {@code null})
   * @param anns annotations
   * @param doc xqdoc string
   * @param vs variable scope
   * @param ii input info
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final QNm qname, final Params params, final Expr expr,
      final AnnList anns, final String doc, final VarScope vs, final InputInfo ii)
          throws QueryException {

    final byte[] uri = qname.uri();
    if(uri.length == 0) throw FUNNONS_X.get(ii, qname.string());
    if(NSGlobal.reserved(uri) || Functions.builtIn(qname) != null)
      throw FNRESERVED_X.get(ii, qname.string());

    final StaticFunc sf = new StaticFunc(qname, params, expr, anns, doc, vs, ii);
    if(!cache(sf.id()).setFunc(sf)) throw FUNCDEFINED_X.get(sf.info, qname.string());
    return sf;
  }

  /**
   * Registers a function call.
   * @param call name function name
   * @return function call
   */
  StaticFuncCall register(final StaticFuncCall call) {
    return cache(StaticFunc.id(call.name, call.exprs.length)).add(call);
  }

  /**
   * Registers a function literal.
   * @param literal wrapped literal
   * @return literal
   */
  public Closure register(final Closure literal) {
    cache(StaticFunc.id(literal.funcName(), literal.arity())).add(literal);
    return literal;
  }

  /**
   * Checks if all functions have been correctly declared, and initializes all function calls.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext qc) throws QueryException {
    for(final FuncCache cache : funcs.values()) {
      final boolean ok = cache.init(qc);
      // ignore unreferenced functions
      if(cache.calls.isEmpty()) continue;

      final StaticFuncCall call = cache.calls.get(0);
      final QNm name = cache.qname();

      // function not defined
      if(!ok) {
        // function is unknown: raise error
        final IntList arities = new IntList();
        for(final FuncCache fc : caches()) {
          if(name.eq(fc.qname())) arities.add(fc.func.arity());
        }
        throw arities.isEmpty() ? similarError(name, call.info()) :
          Functions.wrongArity(call.exprs.length, arities, name.prefixString(), call.info());
      }

      // function defined, but not implemented
      if(cache.func != null && cache.func.expr == null)
        throw FUNCNOIMPL_X.get(call.info(), name.prefixString());
      // set updating flag
      if(cache.updating) qc.updating = true;
    }
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final FuncCache cache : caches()) cache.func.checkUp();
  }

  /**
   * Compiles all functions.
   * @param cc compilation context
   */
  public void compileAll(final CompileContext cc) {
    for(final FuncCache cache : caches()) cache.func.compile(cc);
  }

  /**
   * Returns the function with the given name and arity.
   * @param qname function name
   * @param arity function arity
   * @return function if found, {@code null} otherwise
   */
  public StaticFunc get(final QNm qname, final long arity) {
    final FuncCache cache = funcs.get(StaticFunc.id(qname, arity));
    return cache != null ? cache.func : null;
  }

  /**
   * Returns the unions of the sequences types for function calls of the specified function.
   * @param func function
   * @return sequence types or {@code null}
   */
  SeqType[] seqTypes(final StaticFunc func) {
    // initialize cache for direct lookups of function calls
    if(callsMap == null) {
      callsMap = new IdentityHashMap<>();
      for(final FuncCache cache : caches()) {
        if(func.params.length > 0 && !cache.calls.isEmpty()) callsMap.put(cache.func, cache.calls);
      }
    }
    if(!callsMap.containsKey(func)) return null;

    final int sl = func.params.length;
    final SeqType[] seqTypes = new SeqType[sl];
    for(final StaticFuncCall call : callsMap.get(func)) {
      for(int s = 0; s < sl; s++) {
        final SeqType st = call.arg(s).seqType(), stOld = seqTypes[s];
        seqTypes[s] = stOld == null ? st : stOld.union(st);
      }
    }
    return seqTypes;
  }

  /**
   * Throws an exception if the name of a function is similar to the specified function name.
   * @param qname function name
   * @param ii input info
   * @return exception
   */
  public QueryException similarError(final QNm qname, final InputInfo ii) {
    // check local functions
    final ArrayList<QNm> list = new ArrayList<>();
    for(final FuncCache cache : caches()) {
      if(cache.func.expr != null) list.add(cache.qname());
    }
    final Object similar = Levenshtein.similar(qname.local(),
        list.toArray(QNm[]::new), o -> ((QNm) o).local());

    // return error for local or global function
    return WHICHFUNC_X.get(ii, similar != null ?
      similar(qname.prefixString(), ((QNm) similar).prefixString()) :
      Functions.similar(qname));
  }

  /**
   * Returns all user-defined functions.
   * @return functions
   */
  public StaticFunc[] funcs() {
    final ArrayList<StaticFunc> list = new ArrayList<>();
    for(final FuncCache cache : caches()) list.add(cache.func);
    return list.toArray(StaticFunc[]::new);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(!funcs.isEmpty()) plan.add(plan.create(this), funcs());
  }

  @Override
  public void toString(final QueryString qs) {
    for(final FuncCache cache : caches()) {
      if(cache.func.compiled()) qs.token(cache.func).token(Text.NL);
    }
  }

  /**
   * Returns all user-defined function caches.
   * @return caches
   */
  private ArrayList<FuncCache> caches() {
    final ArrayList<FuncCache> list = new ArrayList<>();
    for(final FuncCache cache : funcs.values()) {
      if(cache.func != null) list.add(cache);
    }
    return list;
  }

  /**
   * Returns a function cache for the specified function id.
   * @param id function id
   * @return function cache
   */
  private FuncCache cache(final byte[] id) {
    return funcs.computeIfAbsent(id, FuncCache::new);
  }

  /**
   * Function cache.
   *
   * @author BaseX Team 2005-23, BSD License
   * @author Christian Gruen
   */
  private static class FuncCache {
    /** Function calls. */
    final ArrayList<StaticFuncCall> calls = new ArrayList<>(0);
    /** Function literals. */
    final ArrayList<Closure> literals = new ArrayList<>(0);
    /** Static function (can be {@code null}). */
    StaticFunc func;
    /** Updating flag. */
    boolean updating;

    /**
     * Initializes the function calls and literals.
     * @param qc query context
     * @return success flag
     * @throws QueryException query exception
     */
    boolean init(final QueryContext qc) throws QueryException {
      // no user-defined function defined, no literals: try to find Java function
      if(func == null) {
        if(literals.isEmpty()) {
          for(final StaticFuncCall call : calls) {
            final JavaCall java = JavaCall.get(call.name, call.exprs, qc, call.sc, call.info());
            if(java == null) return false;
            call.setExternal(java);
            updating = java.updating;
          }
          return true;
        }
        return false;
      }

      for(final StaticFuncCall call : calls) call.setFunc(func);
      final FuncType ft = func.funcType();
      for(final Closure literal : literals) literal.setSignature(ft);
      updating = func.updating;
      return true;
    }

    /**
     * Assigns the specified function.
     * @param sf function to assign
     * @return success flag
     */
    boolean setFunc(final StaticFunc sf) {
      if(func != null) return false;
      func = sf;
      return true;
    }

    /**
     * Caches a new function call.
     * @param call function call
     * @return function call
     */
    StaticFuncCall add(final StaticFuncCall call) {
      calls.add(call);
      call.setFunc(func);
      return call;
    }

    /**
     * Caches a new function literal.
     * @param literal literal
     * @return literal
     */
    Closure add(final Closure literal) {
      literals.add(literal);
      return literal;
    }

    /**
     * Returns the function's name.
     * @return function name
     */
    QNm qname() {
      return func != null ? func.name : calls.get(0).name;
    }
  }
}
