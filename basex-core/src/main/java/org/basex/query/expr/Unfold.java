package org.basex.query.expr;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative evaluation of a recursive function body whose recursive call is replaced by a
 * recursion step.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Unfold extends Arr {
  /** State variables, bound to the initial expressions. */
  private final Var[] vars;
  /** Variable indicating that a recursion step was taken. */
  private final Var flag;
  /** Variable for the results that follow the recursive call (can be {@code null}). */
  private final Var suffix;
  /** Function body with the recursion step. */
  private Expr step;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param init initial expressions
   * @param vars state variables
   * @param flag variable indicating that a recursion step was taken
   * @param suffix variable for the results that follow the recursive call (can be {@code null})
   * @param step function body with the recursion step
   */
  Unfold(final InputInfo info, final Expr[] init, final Var[] vars, final Var flag,
      final Var suffix, final Expr step) {
    super(info, Types.ITEM_ZM, init);
    this.vars = vars;
    this.flag = flag;
    this.suffix = suffix;
    this.step = step;
  }

  @Override
  public void checkUp() throws QueryException {
    super.checkUp();
    checkNoUp(step);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    step = step.compile(cc);
    return super.compile(cc);
  }

  @Override
  public Unfold optimize(final CompileContext cc) {
    final SeqType sst = step.seqType();
    final SeqType st = suffix != null ? sst.union(suffix.seqType()) : sst;
    exprType.assign(st.type, st.zero() ? Occ.ZERO : Occ.get(sst.occ.min, Long.MAX_VALUE));
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    if(suffix != null) return value(qc).iter();

    bind(vars, exprs, 0, qc);
    return new Iter() {
      /** Results of the current step. */
      private Value value = Empty.VALUE;
      /** Position of the next result. */
      private long pos;
      /** Indicates if the last step has been evaluated. */
      private boolean done;

      @Override
      public Item next() throws QueryException {
        while(pos == value.size()) {
          if(done) return null;
          start(qc);
          value = step.value(qc);
          pos = 0;
          done = !stepped(qc);
        }
        return value.itemAt(pos++);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    final ValueBuilder sb = suffix != null ? new ValueBuilder(qc) : null;
    bind(vars, exprs, 0, qc);
    while(true) {
      start(qc);
      vb.add(step.value(qc));
      if(!stepped(qc)) break;
      // suffixes are emitted in reverse order: reverse each, then the whole sequence
      if(sb != null) sb.add(qc.get(suffix).reverse(qc));
    }
    if(sb != null) vb.add(sb.value(this).reverse(qc));
    return vb.value(this);
  }

  /**
   * Evaluates expressions and binds the results to variables.
   * @param vars variables
   * @param exprs expressions
   * @param offset offset of the first expression
   * @param qc query context
   * @throws QueryException query exception
   */
  static void bind(final Var[] vars, final Expr[] exprs, final int offset, final QueryContext qc)
      throws QueryException {
    final int vl = vars.length;
    if(vl == 1) {
      qc.set(vars[0], exprs[offset].value(qc));
    } else {
      final Value[] values = new Value[vl];
      for(int v = 0; v < vl; v++) values[v] = exprs[offset + v].value(qc);
      for(int v = 0; v < vl; v++) qc.set(vars[v], values[v]);
    }
  }

  /**
   * Prepares the evaluation of the next step.
   * @param qc query context
   * @throws QueryException query exception
   */
  private void start(final QueryContext qc) throws QueryException {
    qc.checkStop();
    qc.set(flag, Bln.FALSE);
    if(suffix != null) qc.set(suffix, Empty.VALUE);
  }

  /**
   * Checks if the last step was a recursion step.
   * @param qc query context
   * @return result of check
   */
  private boolean stepped(final QueryContext qc) {
    return qc.get(flag) == Bln.TRUE;
  }

  @Override
  public boolean eager() {
    return suffix != null;
  }

  @Override
  public boolean has(final Flag... flags) {
    return super.has(flags) || step.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return super.inlineable(ic) && step.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(step.count(var).times(2));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr[] st = { step };
    final boolean init = ic.inline(exprs), body = ic.inline(st, true);
    step = st[0];
    return init || body ? optimize(ic.cc) : null;
  }

  @Override
  public Unfold copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    final Expr[] init = copyAll(cc, vm, exprs);
    final int vl = vars.length;
    final Var[] vrs = new Var[vl];
    for(int v = 0; v < vl; v++) vrs[v] = cc.copy(vars[v], vm);
    return copyType(new Unfold(info, init, vrs, cc.copy(flag, vm), cc.copy(suffix, vm),
        step.copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && visitor.declared(vars) && visitor.declared(flag) &&
        (suffix == null || visitor.declared(suffix)) && step.accept(visitor);
  }

  @Override
  public int exprSize() {
    return super.exprSize() + step.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final Unfold unfold && Arrays.equals(vars, unfold.vars) &&
        flag.equals(unfold.flag) && Objects.equals(suffix, unfold.suffix) &&
        step.equals(unfold.step) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, "suffix", suffix != null ? true : null), vars, exprs, step);
  }

  @Override
  public void toString(final QueryString qs) {
    // self-applied closure: equivalent to the original recursive function
    final int vl = vars.length;
    final Var[] params = new Var[vl + 1];
    params[0] = flag;
    System.arraycopy(vars, 0, params, 1, vl);
    qs.open().token(QueryText.LET).token(flag).token(":=").token(QueryText.FN).params(params);
    qs.brace(step).token(QueryText.RETURN);
    Recur.call(qs, info, flag, exprs);
    qs.close();
  }
}
