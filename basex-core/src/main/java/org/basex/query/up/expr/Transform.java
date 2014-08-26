package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  /**
   * Constructor.
   * @param info input info
   * @param copies copy expressions
   * @param mod modify expression
   * @param ret return expression
   */
  public Transform(final InputInfo info, final Let[] copies, final Expr mod, final Expr ret) {
    super(info, mod, ret);
    this.copies = copies;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Let c : copies) c.checkUp();
    final Expr m = exprs[0];
    m.checkUp();
    if(!m.isVacuous() && !m.has(Flag.UPD)) throw UPMODIFY.get(info);
    checkNoUp(exprs[1]);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    for(final Let c : copies) c.expr = c.expr.compile(qc, scp);
    super.compile(qc, scp);
    return this;
  }

  @Override
  public ValueIter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final int o = (int) qc.resources.output.size();
    final Updates updates = qc.resources.updates();
    final ContextModifier tmp = updates.mod;
    final TransformModifier pu = new TransformModifier();
    updates.mod = pu;

    try {
      for(final Let fo : copies) {
        final Iter ir = qc.iter(fo.expr);
        Item i = ir.next();
        if(!(i instanceof ANode) || ir.next() != null) throw UPCOPYMULT_X.get(fo.info, fo.var.name);

        // copy node to main memory data instance
        i = ((ANode) i).dbCopy(qc.context.options);
        // add resulting node to variable
        qc.set(fo.var, i, info);
        pu.addData(i.data());
      }
      final Value v = qc.value(exprs[0]);
      if(!v.isEmpty()) throw BASEX_MOD.get(info);

      updates.prepare();
      updates.apply();
      return qc.value(exprs[1]);
    } finally {
      qc.resources.output.size(o);
      updates.mod = tmp;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag != Flag.UPD && super.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    for(final Let c : copies) if(!c.removable(var)) return false;
    return super.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, copies).plus(super.count(var));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {
    final boolean cp = inlineAll(qc, scp, copies, var, ex);
    return inlineAll(qc, scp, exprs, var, ex) || cp ? optimize(qc, scp) : null;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Transform(info, copyAll(qc, scp, vs, copies), exprs[0].copy(qc, scp, vs),
        exprs[1].copy(qc, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), copies, exprs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(COPY + ' ');
    for(final Let t : copies)
      sb.append(t.var).append(' ').append(ASSIGN).append(' ').append(t.expr).append(' ');
    return sb.append(MODIFY + ' ' + exprs[0] + ' ' + RETURN + ' ' + exprs[1]).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, copies) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Let lt : copies) sz += lt.exprSize();
    for(final Expr e : exprs) sz += e.exprSize();
    return sz;
  }
}
