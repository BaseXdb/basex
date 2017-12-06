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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncs extends ExprInfo {
  /** User-defined functions. */
  private final TokenObjMap<FuncCache> funcs = new TokenObjMap<>();

  /**
   * Returns the signature of the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @return the function's signature
   */
  static byte[] signature(final QNm name, final long arity) {
    return new TokenBuilder(name.prefixId()).add('#').addLong(arity).finish();
  }

  /**
   * Declares a new user-defined function.
   * @param anns annotations
   * @param nm function name
   * @param params formal parameters
   * @param type declared return type (can be {@code null})
   * @param expr function body (can be {@code null})
   * @param doc xqdoc string
   * @param vs variable scope
   * @param info input info
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final AnnList anns, final QNm nm, final Var[] params,
      final SeqType type, final Expr expr, final String doc, final VarScope vs,
      final InputInfo info) throws QueryException {

    final byte[] uri = nm.uri();
    if(uri.length == 0) throw FUNNONS_X.get(info, nm.string());
    if(NSGlobal.reserved(uri) || Functions.get().getBuiltIn(nm) != null)
      throw FNRESERVED_X.get(info, nm.string());

    final StaticFunc sf = new StaticFunc(anns, nm, params, type, expr, doc, vs, info);
    final byte[] sig = sf.id();
    final FuncCache fc = funcs.get(sig);
    if(fc != null) fc.setFunc(sf);
    else funcs.put(sig, new FuncCache(sf));
    return sf;
  }

  /**
   * Creates a call to an already declared or referenced function.
   * @param name name of the function
   * @param args optional arguments
   * @param sc static context
   * @param info input info
   * @return reference if the function is known, {@code null} otherwise
   * @throws QueryException query exception
   */
  TypedFunc funcCall(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo info) throws QueryException {

    // check if function has already been declared
    final FuncCache fc = funcs.get(signature(name, args.length));
    return fc == null ? null : fc.newCall(name, args, sc, info);
  }

  /**
   * Returns a function call to the function with the given name and arity.
   * @param name function name
   * @param args arguments
   * @param sc static context of the function call
   * @param info input info
   * @return function call
   * @throws QueryException query exception
   */
  TypedFunc undeclaredFuncCall(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo info) throws QueryException {

    if(NSGlobal.reserved(name.uri())) {
      final QueryException qe = similarError(name, info);
      if(qe != null) throw qe;
    }
    final byte[] sig = signature(name, args.length);
    if(!funcs.contains(sig)) funcs.put(sig, new FuncCache(null));
    return funcCall(name, args, sc, info);
  }

  /**
   * Registers a literal for a function that was not yet encountered during parsing.
   * @param literal the literal
   */
  public void registerFuncLiteral(final Closure literal) {
    final byte[] sig = signature(literal.funcName(), literal.arity());
    FuncCache cache = funcs.get(sig);
    if(cache == null) {
      cache = new FuncCache(null);
      funcs.put(sig, cache);
    }
    cache.literals.add(literal);
  }

  /**
   * Checks if all functions have been correctly declared, and initializes all function calls.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext qc) throws QueryException {
    // initialize function calls
    for(final FuncCache fc : funcs.values()) {
      final StaticFuncCall call = fc.calls.isEmpty() ? null : fc.calls.get(0);
      if(fc.func == null) {
        // raise error (no function found)...
        final IntList arities = new IntList();
        for(final FuncCache ofc : funcs.values()) {
          if(fc != ofc && ofc.func != null && call.name.eq(ofc.name()))
            arities.add(ofc.func.arity());
        }
        // known function, wrong number of arguments
        if(!arities.isEmpty())
          throw Functions.wrongArity(call.name.string(), call.exprs.length, arities, call.info);

        // function is unknown: find function with similar name
        final QNm nm = call.name;
        final QueryException qe = similarError(nm, call.info);
        throw qe != null ? qe : WHICHFUNC_X.get(call.info, nm.prefixString());
      }

      if(call != null) {
        if(fc.func.expr == null) throw FUNCNOIMPL_X.get(call.info, call.name.prefixString());
        // set updating flag; this will trigger checks in {@link QueryContext#check}
        qc.updating |= fc.func.updating;
      }
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
   * @param cc compilation context
   */
  public void compile(final CompileContext cc) {
    compile(cc, false);
  }

  /**
   * Compiles all functions.
   * @param cc compilation context
   * @param all compile all functions (not only used ones)
   */
  public void compile(final CompileContext cc, final boolean all) {
    for(final FuncCache fc : funcs.values()) {
      if(all || !fc.calls.isEmpty()) fc.func.comp(cc);
    }
  }

  /**
   * Returns the function with the given name and arity.
   * @param name function name
   * @param arity function arity
   * @return function if found, {@code null} otherwise
   */
  public StaticFunc get(final QNm name, final long arity) {
    final FuncCache fc = funcs.get(signature(name, arity));
    return fc != null ? fc.func : null;
  }

  /**
   * Throws an exception if the name of a function is similar to the specified function name.
   * @param name function name
   * @param info input info
   * @return exception
   */
  public QueryException similarError(final QNm name, final InputInfo info) {
    // find local functions
    QueryException qe = null;
    final Levenshtein ls = new Levenshtein();
    final byte[] nm = lc(name.local());
    for(final FuncCache fc : funcs.values()) {
      final StaticFunc sf = fc.func;
      if(sf != null && sf.expr != null && ls.similar(nm, lc(sf.name.local()))) {
        qe = FUNCSIMILAR_X_X.get(info, name.prefixString(), sf.name.prefixString());
        break;
      }
    }
    // find global function
    if(qe == null) qe = Functions.get().similarError(name, info);
    return qe;
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
  public void plan(final FElem plan) {
    if(!funcs.isEmpty()) {
      final FElem elem = planElem();
      plan.add(elem);
      for(final StaticFunc f : funcs()) {
        if(f != null) f.plan(elem);
      }
    }
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
    final ArrayList<Closure> literals = new ArrayList<>(0);
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
      for(final Closure literal : literals) literal.adoptSignature(ft);
    }

    /**
     * Creates a new call to this function.
     * @param name function name
     * @param args arguments
     * @param sc static context
     * @param info input info
     * @return function call
     * @throws QueryException query exception
     */
    public TypedFunc newCall(final QNm name, final Expr[] args, final StaticContext sc,
        final InputInfo info) throws QueryException {

      final StaticFuncCall call = new StaticFuncCall(name, args, sc, info);
      calls.add(call);

      // [LW] should be deferred until the actual types are known (i.e. compile time)
      return func == null ? new TypedFunc(call, new AnnList()) :
        new TypedFunc(call.init(func), func.anns);
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
