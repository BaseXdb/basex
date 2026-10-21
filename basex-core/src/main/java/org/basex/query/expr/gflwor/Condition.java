package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A window {@code start} or {@code end} condition.
 *
 * @author BaseX Team, BSD License
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
   * @param expr condition
   * @param info input info (can be {@code null})
   */
  public Condition(final boolean start, final Var item, final Var pos, final Var prev,
      final Var next, final Expr expr, final InputInfo info) {
    super(info, expr, Types.ITEM_ZM);
    this.start = start;
    this.item = item;
    this.pos = pos;
    this.prev = prev;
    this.next = next;
  }

  @Override
  public Condition optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.EBV, cc);
    return this;
  }

  /**
   * Compiles the condition.
   * @param ex iterated expression
   * @param cc compilation context
   * @throws QueryException query exception
   */
  void compile(final Expr ex, final CompileContext cc) throws QueryException {
    final SeqType st = ex.seqType();
    if(item != null) item.refineType(st.with(Occ.EXACTLY_ONE), 1, cc);
    if(pos  != null) pos.refineType(Types.INTEGER_O, 1, cc);
    if(prev != null) prev.refineType(st.with(Occ.ZERO_OR_ONE), -1, cc);
    if(next != null) next.refineType(st.with(Occ.ZERO_OR_ONE), -1, cc);
    compile(cc);
  }

  @Override
  public Condition inline(final InlineContext ic) throws QueryException {
    return (Condition) super.inline(ic);
  }

  @Override
  public Condition copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Condition(start, cc.copy(item, vm), cc.copy(pos, vm), cc.copy(prev, vm),
        cc.copy(next, vm), expr.copy(cc, vm), info));
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
   * Returns the position variable if it is the only variable of this condition.
   * @return position variable or {@code null}
   */
  Var onlyPos() {
    return item == null && prev == null && next == null ? pos : null;
  }

  /**
   * Returns the fixed distance between the start and end position of a window.
   * @param spos position variable of the start condition
   * @return distance, or {@code -1} if this end condition specifies no fixed distance
   */
  long distance(final Var spos) {
    final Var epos = onlyPos();
    if(epos == null || !(expr instanceof final CmpG cmp) || cmp.cmpOp() != CmpOp.EQ) return -1;

    // $e - $s = K, $e = $s + K, $s + K = $e, $e = $s
    final Expr op1 = cmp.arg(0), op2 = cmp.arg(1);
    final Expr dist;
    if(op1 instanceof final Arith arith && arith.calc == Calc.SUBTRACT &&
        ref(arith.arg(0), epos) && ref(arith.arg(1), spos)) {
      dist = op2;
    } else {
      dist = ref(op1, epos) ? offset(op2, spos) : ref(op2, epos) ? offset(op1, spos) : null;
    }
    final long d = dist instanceof final Itr itr ? itr.itr() : -1;
    return d >= 0 && d < Long.MAX_VALUE ? d : -1;
  }

  /**
   * Returns the offset of an expression relative to the specified variable.
   * @param ex expression
   * @param var variable
   * @return offset, or {@code null} if the expression is no such reference or addition
   */
  private static Expr offset(final Expr ex, final Var var) {
    return ref(ex, var) ? Itr.ZERO :
      ex instanceof final Arith arith && arith.calc == Calc.ADD && ref(arith.arg(0), var) ?
      arith.arg(1) : null;
  }

  /**
   * Checks if the expression is a reference to the specified variable.
   * @param ex expression
   * @param var variable
   * @return result of check
   */
  private static boolean ref(final Expr ex, final Var var) {
    return ex instanceof final VarRef vr && vr.var == var;
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
   * @param ps position in the input sequence
   * @param pr previous item (can be {@code null})
   * @param nx next item (can be {@code null})
   * @return {@code true} if {@code it} matches the condition, {@code false} otherwise
   * @throws QueryException query exception
   */
  boolean matches(final QueryContext qc, final Item it, final long ps, final Item pr, final Item nx)
      throws QueryException {
    // bind variables
    bind(qc, it, ps, pr, nx);
    // evaluate as effective boolean value
    return expr.ebv(qc, info);
  }

  /**
   * Binds this condition's variables to the given values.
   * @param qc query context
   * @param it current item
   * @param ps position
   * @param pr previous item (can be {@code null})
   * @param nx next item (can be {@code null})
   * @throws QueryException query exception
   */
  void bind(final QueryContext qc, final Item it, final long ps, final Item pr, final Item nx)
      throws QueryException {
    if(item != null) qc.set(item, it);
    if(pos  != null) qc.set(pos,  Itr.get(ps));
    if(prev != null) qc.set(prev, pr == null ? Empty.VALUE : pr);
    if(next != null) qc.set(next, nx == null ? Empty.VALUE : nx);
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
    return this == obj || obj instanceof final Condition c && Objects.equals(item, c.item) &&
        Objects.equals(pos, c.pos) && Objects.equals(prev, c.prev) &&
        Objects.equals(next, c.next) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final FBuilder elem = plan.create(start ? START : END, item);
    if(pos  != null) elem.node(plan.create(AT, pos));
    if(prev != null) elem.node(plan.create(PREVIOUS, prev));
    if(next != null) elem.node(plan.create(NEXT, next));
    plan.add(elem, expr);
  }
  @Override
  public void toString(final QueryString qs) {
    qs.token(start ? START : END);
    if(item != null) qs.token(item);
    if(pos  != null) qs.token(AT).token(pos);
    if(prev != null) qs.token(PREVIOUS).token(prev);
    if(next != null) qs.token(NEXT).token(next);
    qs.token(WHEN).single(expr);
  }
}

