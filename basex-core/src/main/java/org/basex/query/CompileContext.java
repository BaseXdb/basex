package org.basex.query;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.Function;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class CompileContext {
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
   * Assigns a new dummy item.
   * @param expr context expression (can be {@code null})
   */
  public void updateFocus(final Expr expr) {
    if(expr != null) qc.focus.value = dummyItem(expr);
  }

  /**
   * Returns a dummy item, based on the type of the specified expression and the current context.
   * @param expr expression
   * @return dummy item
   */
  public Item dummyItem(final Expr expr) {
    final Value value = qc.focus.value;
    final Data data = (value != null && expr instanceof Step ? value : expr).data();
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
   * @param result resulting expression
   * @return optimized expression
   */
  public Expr emptySeq(final Expr result) {
    return replaceWith(result, null);
  }

  /**
   * Replaces an EBV expression.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @return optimized expression
   */
  public Expr replaceEbv(final Expr expr, final Expr result) {
    return replaceWith(expr, result, false);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @return optimized expression
   */
  public Expr replaceWith(final Expr expr, final Expr result) {
    return replaceWith(expr, result, true);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @param refine refine type
   * @return optimized expression
   */
  private Expr replaceWith(final Expr expr, final Expr result, final boolean refine) {
    final Expr res = result == null ? Empty.SEQ : result;
    if(res != expr) {
      final Supplier<String> f  = () -> {
        final TokenBuilder tb = new TokenBuilder();
        final String exprDesc = expr.description(), resDesc = res.description();
        tb.add(res instanceof ParseExpr ? OPTREWRITE : OPTPRE).add(' ').add(exprDesc);
        if(!exprDesc.equals(resDesc)) tb.add(" to ").add(resDesc);

        final byte[] exprString = QueryError.normalize(Token.token(expr.toString()), null);
        final byte[] resString = QueryError.normalize(Token.token(res.toString()), null);
        tb.add(": ").add(exprString);
        if(!Token.eq(exprString, resString)) tb.add(" -> ").add(resString);
        return tb.toString();
      };
      info("%", f);

      if(res instanceof ParseExpr) {
        // refine type. required mostly for {@link Filter} rewritings
        if(refine) {
          final ParseExpr re = (ParseExpr) res;
          final SeqType et = expr.seqType(), rt = re.seqType();
          if(et.refinable(rt)) {
            final SeqType st = et.intersect(rt);
            if(st != null) re.exprType.assign(st);
          }
        }
      } else if(res != Empty.SEQ && refine) {
        // refine type. required because original type might have got lost in new sequence
        if(res instanceof Seq) {
          final Seq seq = (Seq) res;
          final Type et = expr.seqType().type, rt = seq.type;
          if(!et.eq(rt) && et.instanceOf(rt)) {
            final Type type = et.intersect(rt);
            if(type != null) {
              seq.type = type;
              // intersected type may not be exact anymore: invalidate homogeneous flag
              seq.homo = false;
            }
          }
        } else if(res instanceof FItem) {
          // refine type of function items (includes maps and arrays)
          final FItem fitem = (FItem) res;
          final SeqType et = expr.seqType(), rt = res.seqType();
          if(et.refinable(rt)) {
            final Type type = et.type.intersect(rt.type);
            if(type != null) fitem.type = type;
          }
        }
      }
    }
    return res;
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
   * @param func function
   * @param info input info
   * @param exprs expressions
   * @return function
   * @throws QueryException query exception
   */
  public Expr function(final Function func, final InputInfo info, final Expr... exprs)
      throws QueryException {
    return func.get(sc(), info, exprs).optimize(this);
  }
}
