package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A window {@code start} of {@code end} condition.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Condition extends Single {
  /** Start condition flag. */
  private final boolean start;
  /** Item variable (can be {@code null}). */
  private final Var item;
  /** Position variable (can be {@code null}). */
  private final Var pos;
  /** Previous item (can be {@code null}). */
  private final Var prev;
  /** Next item (can be {@code null}). */
  private final Var next;

  /**
   * Constructor.
   * @param start start condition flag
   * @param item item variable (can be {@code null})
   * @param pos position variable (can be {@code null})
   * @param prev previous variable (can be {@code null})
   * @param next next variable (can be {@code null})
   * @param cond condition
   * @param info input info
   */
  public Condition(final boolean start, final Var item, final Var pos, final Var prev,
      final Var next, final Expr cond, final InputInfo info) {
    super(info, cond);
    this.start = start;
    this.item = item;
    this.pos = pos;
    this.prev = prev;
    this.next = next;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    expr = expr.compile(cc);
    return optimize(cc);
  }

  @Override
  public Condition optimize(final CompileContext cc) throws QueryException {
    expr = expr.optimizeEbv(cc);
    return this;
  }

  @Override
  public Condition inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
    return (Condition) super.inline(var, ex, cc);
  }

  @Override
  public Condition copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Condition(start, cc.copy(item, vm), cc.copy(pos, vm), cc.copy(prev, vm),
        cc.copy(next, vm), expr.copy(cc, vm), info);
  }

  /**
   * Number of non-{@code null} variables in this condition.
   * @return number of variables
   */
  int nVars() {
    int i = 0;
    if(item != null) i++;
    if(pos  != null) i++;
    if(prev != null) i++;
    if(next != null) i++;
    return i;
  }

  /**
   * Checks if this condition binds the item following the current one in the input.
   * @return result of check
   */
  boolean usesNext() {
    return next != null;
  }

  /**
   * Write all non-{@code null} variables in this condition to the given array.
   *
   * @param arr array to write to
   * @param p start position
   */
  void writeVars(final Var[] arr, final int p) {
    int i = p;
    if(item != null) arr[i++] = item;
    if(pos  != null) arr[i++] = pos;
    if(prev != null) arr[i++] = prev;
    if(next != null) arr[i]   = next;
  }

  /**
   * Binds the variables and checks if the item satisfies this condition.
   * @param qc query context for variable binding
   * @param it current item
   * @param p position in the input sequence
   * @param pr previous item
   * @param nx next item
   * @return {@code true} if {@code it} matches the condition, {@code false} otherwise
   * @throws QueryException query exception
   */
  boolean matches(final QueryContext qc, final Item it, final long p, final Item pr, final Item nx)
      throws QueryException {
    // bind variables
    bind(qc, it, p, pr, nx);
    // evaluate as effective boolean value
    return expr.ebv(qc, info).bool(info);
  }

  /**
   * Binds this condition's variables to the given values.
   * @param qc query context
   * @param it current item
   * @param p position
   * @param pr previous item
   * @param nx next item
   * @throws QueryException query exception
   */
  void bind(final QueryContext qc, final Item it, final long p, final Item pr, final Item nx)
      throws QueryException {
    if(item != null) qc.set(item, it == null ? Empty.SEQ : it);
    if(pos  != null) qc.set(pos,  Int.get(p));
    if(prev != null) qc.set(prev, pr == null ? Empty.SEQ : pr);
    if(next != null) qc.set(next, nx == null ? Empty.SEQ : nx);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (item == null || visitor.declared(item))
        && (pos  == null || visitor.declared(pos))
        && (prev == null || visitor.declared(prev))
        && (next == null || visitor.declared(next))
        && expr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Condition)) return false;
    final Condition c = (Condition) obj;
    return Objects.equals(item, c.item) && Objects.equals(pos, c.pos) &&
           Objects.equals(prev, c.prev) && Objects.equals(next, c.next) && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = new FElem(start ? START : END);

    // mapping variable names to roles
    if(item != null) e.add(planAttr(VAR, token(item.toString())));
    if(pos  != null) e.add(planAttr(token(AT), token(pos.toString())));
    if(prev != null) e.add(planAttr(token(PREVIOUS), token(prev.toString())));
    if(next != null) e.add(planAttr(token(NEXT), token(next.toString())));

    // IDs and stack slots
    if(item != null) item.plan(e);
    if(pos  != null) pos.plan(e);
    if(prev != null) prev.plan(e);
    if(next != null) next.plan(e);

    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(start ? START : END);
    if(item != null) sb.append(' ').append(item);
    if(pos  != null) sb.append(' ').append(AT).append(' ').append(pos);
    if(prev != null) sb.append(' ').append(PREVIOUS).append(' ').append(prev);
    if(next != null) sb.append(' ').append(NEXT).append(' ').append(next);
    return sb.append(' ').append(WHEN).append(' ').append(expr).toString();
  }
}

