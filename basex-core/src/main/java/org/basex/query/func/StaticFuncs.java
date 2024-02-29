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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncs extends ExprInfo {
  /** Function caches. */
  private final TokenObjMap<FuncCache> caches = new TokenObjMap<>();
  /** Function calls. */
  private Map<StaticFunc, ArrayList<StaticFuncCall>> callsMap;

  /**
   * Declares a new user-defined function.
   * @param name function name
   * @param params parameters with variables and optional default values
   * @param expr function body (can be {@code null})
   * @param anns annotations
   * @param doc xqdoc string
   * @param vs variable scope
   * @param info input info (can be {@code null})
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final QNm name, final Params params, final Expr expr,
      final AnnList anns, final String doc, final VarScope vs, final InputInfo info)
          throws QueryException {

    final byte[] uri = name.uri();
    if(uri.length == 0) throw FUNNONS_X.get(info, name.string());
    if(NSGlobal.reserved(uri) || Functions.builtIn(name) != null)
      throw FNRESERVED_X.get(info, name.string());

    final StaticFunc sf = new StaticFunc(name, params, expr, anns, vs, info, doc);
    if(!cache(name.prefixId()).register(sf)) throw DUPLFUNC_X.get(sf.info, name.string());
    return sf;
  }

  /**
   * Registers a function call.
   * @param call name function name
   * @throws QueryException query exception
   */
  void register(final StaticFuncCall call) throws QueryException {
    cache(call.name.prefixId()).add(call);
  }

  /**
   * Registers a function literal.
   * @param literal wrapped literal
   */
  public void register(final Closure literal) {
    cache(literal.funcName().prefixId()).add(literal);
  }

  /**
   * Checks if all functions have been correctly declared, and initializes all function calls.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext qc) throws QueryException {
    for(final FuncCache cache : caches.values()) cache.init(qc);
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final FuncCache cache : caches()) {
      for(final StaticFunc func : cache.funcs) func.checkUp();
    }
  }

  /**
   * Compiles all functions.
   * @param cc compilation context
   */
  public void compileAll(final CompileContext cc) {
    for(final FuncCache cache : caches()) {
      for(final StaticFunc func : cache.funcs) func.compile(cc);
    }
  }

  /**
   * Returns the function with the given name and arity.
   * @param qname function name
   * @param arity function arity
   * @return function if found, {@code null} otherwise
   */
  public StaticFunc get(final QNm qname, final long arity) {
    final FuncCache cache = caches.get(qname.prefixId());
    if(cache != null) {
      for(final StaticFunc func : cache.funcs) {
        if(arity >= func.min && arity <= func.arity()) return func;
      }
    }
    return null;
  }

  /**
   * Returns the unions of the sequences types for function calls of the specified function.
   * @param func function
   * @return sequence types, or {@code null} if function is not referenced
   */
  SeqType[] seqTypes(final StaticFunc func) {
    // initialize cache for direct lookups of function calls
    if(callsMap == null) {
      callsMap = new IdentityHashMap<>();
      for(final FuncCache cache : caches()) {
        for(final StaticFuncCall call : cache.calls) {
          callsMap.computeIfAbsent(call.func, k -> new ArrayList<>(1)).add(call);
        }
      }
    }
    final ArrayList<StaticFuncCall> calls = callsMap.get(func);
    final int sl = func.arity();
    if(calls == null || calls.isEmpty() || sl == 0) return null;

    final SeqType[] seqTypes = new SeqType[sl];
    for(final StaticFuncCall call : calls) {
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
   * @param info input info (can be {@code null})
   * @return exception
   */
  public QueryException similarError(final QNm qname, final InputInfo info) {
    // check local functions
    final ArrayList<QNm> list = new ArrayList<>();
    for(final FuncCache cache : caches()) {
      for(final StaticFunc func : cache.funcs) {
        if(func.expr != null) {
          list.add(cache.qname());
          break;
        }
      }
    }
    final Object similar = Levenshtein.similar(qname.local(), list.toArray(QNm[]::new),
        o -> ((QNm) o).local());

    // return error for local or global function
    return WHICHFUNC_X.get(info, similar != null ?
      similar(qname.prefixString(), ((QNm) similar).prefixString()) :
      Functions.similar(qname));
  }

  /**
   * Returns all user-defined functions.
   * @return functions
   */
  public StaticFunc[] funcs() {
    final ArrayList<StaticFunc> list = new ArrayList<>();
    for(final FuncCache cache : caches()) list.addAll(cache.funcs);
    return list.toArray(StaticFunc[]::new);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(!caches.isEmpty()) plan.add(plan.create(this), funcs());
  }

  @Override
  public void toString(final QueryString qs) {
    for(final FuncCache cache : caches()) {
      for(final StaticFunc func : cache.funcs) {
        if(func.compiled()) qs.token(func).token(Text.NL);
      }
    }
  }

  /**
   * Returns all user-defined function caches.
   * @return caches
   */
  private ArrayList<FuncCache> caches() {
    final ArrayList<FuncCache> list = new ArrayList<>();
    for(final FuncCache cache : caches.values()) {
      if(!cache.funcs.isEmpty()) list.add(cache);
    }
    return list;
  }

  /**
   * Returns a function cache for the specified function id.
   * @param id function id
   * @return function cache
   */
  private FuncCache cache(final byte[] id) {
    return caches.computeIfAbsent(id, FuncCache::new);
  }

  /**
   * Function cache.
   *
   * @author BaseX Team 2005-24, BSD License
   * @author Christian Gruen
   */
  private static final class FuncCache {
    /** Functions. */
    final ArrayList<StaticFunc> funcs = new ArrayList<>(1);
    /** Function calls. */
    final ArrayList<StaticFuncCall> calls = new ArrayList<>(0);
    /** Function literals. */
    final ArrayList<Closure> literals = new ArrayList<>(0);

    /**
     * Initializes the function calls and literals.
     * @param qc query context
     * @throws QueryException query exception
     */
    void init(final QueryContext qc) throws QueryException {
      // assign functions to function calls
      for(final StaticFuncCall call : calls) {
        if(call.func == null && !setFunc(call) && !setJava(call, qc)) {
          // function is unknown: raise error
          if(!calls.isEmpty()) {
            final IntList arities = new IntList();
            for(final StaticFunc func : funcs) arities.add(func.min).add(func.arity());
            final InputInfo info = call.info();
            throw arities.isEmpty() ? qc.functions.similarError(qname(), info) :
              Functions.wrongArity(call.arity(), arities, qname().prefixString(), false, info);
          }
        } else {
          // check if all implementations exist for all functions, set updating flag
          final StaticFunc func = call.func;
          if(func != null) {
            if(func.expr == null) throw FUNCNOIMPL_X.get(func.info, func.name.prefixString());
            if(func.updating) qc.updating();
          } else if(((JavaCall) call.external).updating) qc.updating();
        }
      }

      // assign function signatures to function literals
      for(final Closure literal : literals) {
        final int arity = literal.arity();
        for(final StaticFunc func : funcs) {
          if(arity < func.min || arity > func.arity()) {
            literal.setSignature(func.funcType());
            break;
          }
        }
      }
    }

    /**
     * Registers the specified function.
     * @param func function to assign
     * @return success flag
     */
    boolean register(final StaticFunc func) {
      /* Reject a function with a conflicting arity range. Examples:
       * f($a), f($b)
       * f($a), f($a, $b := ()) */
      for(final StaticFunc sf : funcs) {
        if(func.arity() >= sf.min && func.min <= sf.arity()) return false;
      }
      funcs.add(func);
      return true;
    }

    /**
     * Adds a function call.
     * @param call function call
     * @throws QueryException query exception
     */
    void add(final StaticFuncCall call) throws QueryException {
      calls.add(call);
      setFunc(call);
    }

    /**
     * Tries to assign a function to a static function call.
     * @param call static function call
     * @return success flag
     * @throws QueryException query exception
     */
    boolean setFunc(final StaticFuncCall call) throws QueryException {
      final int arity = call.arity();
      for(final StaticFunc func : funcs) {
        if(arity >= func.min && arity <= func.arity()) {
          call.setFunc(func);
          return true;
        }
      }
      return false;
    }

    /**
     * Tries to assign a Java function to a static function call.
     * @param call static function call
     * @param qc query context
     * @return success flag
     * @throws QueryException query exception
     */
    boolean setJava(final StaticFuncCall call, final QueryContext qc) throws QueryException {
      final JavaCall java = literals.isEmpty() ?
        JavaCall.get(call.name, call.exprs, qc, call.sc, call.info()) : null;
      call.setExternal(java);
      return java != null;
    }

    /**
     * Adds a function literal.
     * @param literal literal
     */
    void add(final Closure literal) {
      literals.add(literal);
    }

    /**
     * Returns the function's name.
     * @return function name
     */
    QNm qname() {
      return funcs.isEmpty() ? calls.get(0).name : funcs.get(0).name;
    }
  }
}
