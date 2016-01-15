package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
 * @author BaseX Team 2005-15, BSD License
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
    return super.compile(qc, scp);
  }

  @Override
  public ValueIter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final int o = qc.resources.cache.size();
    final Updates updates = qc.resources.updates();
    final ContextModifier tmp = updates.mod;
    final TransformModifier pu = new TransformModifier();
    updates.mod = pu;

    try {
      for(final Let c : copies) {
        final Iter ir = qc.iter(c.expr);
        Item i = ir.next();
        if(!(i instanceof ANode)) throw UPCOPYMULT_X_X.get(c.info, c.var.name, i);
        final Item i2 = ir.next();
        if(i2 != null) throw UPCOPYMULT_X_X.get(c.info, c.var.name, ValueBuilder.concat(i, i2));

        // copy node to main memory data instance
        i = ((ANode) i).dbNodeCopy(qc.context.options);
        // add resulting node to variable
        qc.set(c.var, i, info);
        pu.addData(i.data());
      }
      final Value v = qc.value(exprs[0]);
      if(!v.isEmpty()) throw BASX_UPMODIFY.get(info);

      updates.prepare(qc);
      updates.apply(qc);
    } finally {
      qc.resources.cache.size(o);
      updates.mod = tmp;
    }
    return qc.value(exprs[1]);
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
