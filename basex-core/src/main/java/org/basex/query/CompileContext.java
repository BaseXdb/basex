package org.basex.query;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
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
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class CompileContext {
  /** Compile-time optimizations. */
  public enum Simplify {
    /** Effective boolean value. */ EBV,
    /** Untyped atomic.          */ ATOM,
    /** Numbers.                 */ NUMBER,
    /** Distinct values.         */ DISTINCT
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
    qc.info.compInfo(string, ext);
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
   * @param expr context expression (can be {@code null})
   */
  public void pushFocus(final Expr expr) {
    focuses.add(qc.focus);
    final QueryFocus focus = new QueryFocus();
    if(expr != null) focus.value = dummyItem(expr);
    qc.focus = focus;
  }

  /**
   * Assigns a new dummy item to the query focus.
   * @param expr context expression
   */
  public void updateFocus(final Expr expr) {
    qc.focus.value = dummyItem(expr);
  }

  /**
   * Returns a dummy item, based on the type of the specified expression and the current context.
   * @param expr expression
   * @return dummy item
   */
  public Item dummyItem(final Expr expr) {
    Data data = expr.data();
    // no data reference: if expression is a step, use data from current focus
    if(data == null && expr instanceof Step) {
      final Value value = qc.focus.value;
      if(value != null) data = value.data();
    }
    return new Dummy(expr.seqType().type, data);
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
   * Adds an optimization info for pre-evaluating the specified expression to an empty sequence.
   * @param expr original expression
   * @return optimized expression
   */
  public Expr emptySeq(final Expr expr) {
    return replaceWith(expr, Empty.VALUE);
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
    if(result != expr) {
      final Supplier<String> f  = () -> {
        final TokenBuilder tb = new TokenBuilder();
        final String exprDesc = expr.description(), resDesc = result.description();
        tb.add(OPTREWRITE).add(' ').add(exprDesc);
        if(!exprDesc.equals(resDesc)) tb.add(" to ").add(resDesc);

        final byte[] exprString = QueryError.normalize(Token.token(expr.toString()), null);
        final byte[] resString = QueryError.normalize(Token.token(result.toString()), null);
        tb.add(": ").add(exprString);
        if(!Token.eq(exprString, resString)) tb.add(" -> ").add(resString);
        return tb.toString();
      };
      info("%", f);

      if(refine) {
        if(result instanceof Value) {
          ((Value) result).refineType(expr);
        } else {
          // refine type. required mostly for {@link Filter} rewritings
          final ParseExpr re = (ParseExpr) result;
          final SeqType et = expr.seqType(), rt = re.seqType();
          if(et.refinable(rt)) {
            final SeqType st = et.intersect(rt);
            if(st != null) re.exprType.assign(st);
          }
        }
      }
    }
    return result;
  }

  /**
   * Creates an error function instance.
   * @param qe exception to be raised
   * @param expr expression
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
}
