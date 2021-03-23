package org.basex.query;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Compilation context.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CompileContext {
  /**
   * Compile-time simplifications.
   * @see Expr#simplifyFor(Simplify, CompileContext)
   */
  public enum Simplify {
    /**
     * Simplify EBV checks.
     * Requested by {@link If}, {@link Logical}, {@link Preds}, {@link Condition}, {@link Where},
     * {@link FnBoolean}, {@link FnNot}.
     * Evaluated by {@link Expr} {@link Filter}, {@link List}, {@link SimpleMap}, {@link Path}
     * and others.
     */
    EBV,
    /**
     * Skip redundant atomizations.
     * Requested by {@link FnData}, {@link FnDistinctValues}, {@link Data}, {@link GroupSpec},
     * {@link OrderKey}.
     * Evaluated by {@link FnData}, {@link Cast}, {@link TypeCheck}.
     */
    DATA,
    /**
     * String arguments.
     * Requested by {@link Cast}, {@link CmpG}, {@link StandardFunc} and others.
     * Evaluated by {@link FnData}, {@link Cast}, {@link TypeCheck}.
     */
    STRING,
    /**
     * Numeric arguments.
     * Requested by {@link Arith}, {@link CmpIR}, {@link FTWeight} and others.
     * Evaluated by {@link FnData}, {@link Cast}, {@link TypeCheck},
     * {@link SimpleMap} or {@link FnNumber}.
     */
    NUMBER,
    /**
     * Predicate checks.
     * Requested by {@link Preds}.
     * Evaluated by {@link Expr} , {@link FnData}, {@link Cast}, {@link TypeCheck},
     * {@link SimpleMap} or {@link FnNumber}.
     */
    PREDICATE,
    /**
     * Distinct values.
     * Requested by {@link CmpG} and {@link FnDistinctValues}.
     * Evaluated by {@link Filter}, {@link List}, {@link SimpleMap} and others.
     * and others.
     */
    DISTINCT
  }

  /** Limit for the size of sequences that are pre-evaluated. */
  public static final int MAX_PREEVAL = 1 << 18;

  /** Query context. */
  public final QueryContext qc;
  /** Variable scope list. */
  private final ArrayDeque<VarScope> scopes = new ArrayDeque<>();
  /** Query focus list. */
  private final ArrayDeque<QueryFocus> focuses = new ArrayDeque<>();

  /**
   * Constructor.
   * @param qc query context
   */
  public CompileContext(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void info(final String string, final Object... ext) {
    if(qc.parent == null) qc.info.compInfo(string, ext);
  }

  /**
   * Pushes a new variable scope to the stack.
   * @param vs variable scope
   */
  public void pushScope(final VarScope vs) {
    scopes.add(vs);
  }

  /**
   * Removes and returns the current variable scope.
   * @return the removed element
   */
  public VarScope removeScope() {
    return scopes.removeLast();
  }

  /**
   * Prepares the variable scope for being compiled.
   * This method should be run after compiling a scope.
   * @param scope scope
   */
  public void removeScope(final Scope scope) {
    removeScope().cleanUp(scope);
  }

  /**
   * Pushes the current query focus onto the stack and, if possible, assigns a new dummy item.
   * @param expr focus expression (can be {@code null})
   */
  public void pushFocus(final Expr expr) {
    focuses.add(qc.focus);
    final QueryFocus qf = new QueryFocus();
    if(expr != null) qf.value = dummyItem(expr);
    qc.focus = qf;
  }

  /**
   * Assigns a new dummy item to the query focus.
   * @param expr focus expression
   */
  public void updateFocus(final Expr expr) {
    qc.focus.value = dummyItem(expr);
  }

  /**
   * Evaluates a function in the given focus.
   * @param expr focus expression (can be {@code null})
   * @param func function to evaluate
   * @return resulting expression
   * @throws QueryException query exception
   */
  public Expr get(final Expr expr, final QuerySupplier<Expr> func) throws QueryException {
    pushFocus(expr);
    try {
      return func.get();
    } finally {
      removeFocus();
    }
  }

  /**
   * Evaluates a function in the given focus.
   * @param expr focus expression (can be {@code null})
   * @param func function to evaluate
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean ok(final Expr expr, final QuerySupplier<Boolean> func) throws QueryException {
    pushFocus(expr);
    try {
      return func.get();
    } finally {
      removeFocus();
    }
  }

  /**
   * Returns a dummy item, based on the type of the specified expression and the current context.
   * @param expr expression
   * @return dummy item
   */
  private Item dummyItem(final Expr expr) {
    Data data = expr.data();
    // no data reference: if expression is a step, use data from current focus
    if(data == null && expr instanceof Step) {
      final Value value = qc.focus.value;
      if(value != null) data = value.data();
    }
    return new Dummy(expr.seqType().with(Occ.EXACTLY_ONE), data);
  }

  /**
   * Removes the current query focus from the stack.
   */
  public void removeFocus() {
    qc.focus = focuses.pollLast();
  }

  /**
   * Indicates if the query focus is nested.
   * @return result of check
   */
  public boolean nestedFocus() {
    return !focuses.isEmpty();
  }

  /**
   * Returns the current variable scope.
   * @return variable scope
   */
  public VarScope vs() {
    return scopes.getLast();
  }

  /**
   * Returns the current static context.
   * @return static context
   */
  public StaticContext sc() {
    return vs().sc;
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param var variable to copy (can be {@code null})
   * @param vm variable mapping (can be {@code null})
   * @return new variable, or {@code null} if the supplied variable is {@code null}
   */
  public Var copy(final Var var, final IntObjMap<Var> vm) {
    if(var == null) return null;
    final VarScope vs = vs();
    final Var vr = vs.add(new Var(var, qc, vs.sc));
    if(vm != null) vm.put(var.id, vr);
    return vr;
  }

  /**
   * Pre-evaluates the specified expression.
   * @param expr expression
   * @return optimized expression
   * @throws QueryException query exception
   */
  public Expr preEval(final Expr expr) throws QueryException {
    return replaceWith(expr, expr.value(qc));
  }

  /**
   * Replaces the specified expression with an empty sequence.
   * @param expr original expression
   * @return optimized expression
   */
  public Expr emptySeq(final Expr expr) {
    return replaceWith(expr, Empty.VALUE, false);
  }

  /**
   * Replaces an expression with a simplified one.
   * As the simplified expression may have a different type, no type refinement is performed.
   * @param expr original expression
   * @param result resulting expression
   * @return optimized expression
   */
  public Expr simplify(final Expr expr, final Expr result) {
    return replaceWith(expr, result, false);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr original expression
   * @param result resulting expression
   * @return optimized expression
   */
  public Expr replaceWith(final Expr expr, final Expr result) {
    return replaceWith(expr, result, true);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr original expression
   * @param result resulting expression
   * @param refine refine type
   * @return optimized expression
   */
  private Expr replaceWith(final Expr expr, final Expr result, final boolean refine) {
    // result yields no items and is deterministic: replace with empty sequence
    final Expr res = result.seqType().zero() && !result.has(Flag.NDT) ? Empty.VALUE : result;
    if(res != expr) {
      info("%", (Supplier<String>) () -> {
        final TokenBuilder tb = new TokenBuilder();
        final String exprDesc = expr.description(), resDesc = res.description();
        tb.add(OPTREWRITE).add(' ').add(exprDesc);
        if(!exprDesc.equals(resDesc)) tb.add(" to ").add(resDesc);

        final byte[] exprString = QueryError.normalize(Token.token(expr.toString()), null);
        final byte[] resString = QueryError.normalize(Token.token(res.toString()), null);
        tb.add(": ").add(exprString);
        if(!Token.eq(exprString, resString)) tb.add(" -> ").add(resString);
        return tb.toString();
      });
      if(refine) res.refineType(expr);
    }
    return res;
  }

  /**
   * Creates an error function instance.
   * @param qe exception to be raised
   * @param expr expression that caused the error message
   * @return function
   */
  public StandardFunc error(final QueryException qe, final Expr expr) {
    return FnError.get(qe, expr.seqType(), sc());
  }

  /**
   * Creates and returns an optimized instance of the specified function.
   * @param function function
   * @param ii input info
   * @param exprs expressions
   * @return function
   * @throws QueryException query exception
   */
  public Expr function(final AFunction function, final InputInfo ii, final Expr... exprs)
      throws QueryException {
    return function.get(sc(), ii, exprs).optimize(this);
  }

  /**
   * Creates a list expression from a condition and a return expression.
   * @param cond condition (an optional result will be swallowed)
   * @param rtrn return expression
   * @param ii input info
   * @return function
   * @throws QueryException query exception
   */
  public Expr merge(final Expr cond, final Expr rtrn, final InputInfo ii) throws QueryException {
    return cond.has(Flag.NDT) ?
      List.get(this, ii, function(Function._PROF_VOID, ii, cond), rtrn) : rtrn;
  }

  /**
   * Replicates an expression.
   * @param expr expression
   * @param count count expression
   * @param ii input info
   * @return function
   * @throws QueryException query exception
   */
  public Expr replicate(final Expr expr, final Expr count, final InputInfo ii)
      throws QueryException {
    final ExprList args = new ExprList().add(expr).add(count);
    if(expr.has(Flag.NDT, Flag.CNS)) args.add(Bln.TRUE);
    return function(Function._UTIL_REPLICATE, ii, args.finish());
  }
}
