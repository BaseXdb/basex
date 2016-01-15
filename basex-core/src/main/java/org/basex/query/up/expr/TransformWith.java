package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
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
 * @author Christian Gruen
 */
public final class TransformWith extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param src source expression
   * @param mod modify expression
   */
  public TransformWith(final InputInfo info, final Expr src, final Expr mod) {
    super(info, src, mod);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final Value v = qc.value;
    try {
      qc.value = null;
      return super.compile(qc, scp);
    } finally {
      qc.value = v;
    }
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(exprs[0]);
    final Expr m = exprs[1];
    m.checkUp();
    if(!m.isVacuous() && !m.has(Flag.UPD)) throw UPMODIFY.get(info);
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

    final Value cv = qc.value;
    try {
      final Iter ir = qc.iter(exprs[0]);
      Item i = ir.next();
      if(!(i instanceof ANode)) throw UPSOURCE_X.get(info, i);
      final Item i2 = ir.next();
      if(i2 != null) throw UPSOURCE_X.get(info, ValueBuilder.concat(i, i2));

      // copy node to main memory data instance
      i = ((ANode) i).dbNodeCopy(qc.context.options);
      // set resulting node as context
      qc.value = i;
      pu.addData(i.data());

      final Value v = qc.value(exprs[1]);
      if(!v.isEmpty()) throw BASX_UPMODIFY.get(info);

      updates.prepare(qc);
      updates.apply(qc);
      return qc.value;
    } finally {
      qc.resources.cache.size(o);
      updates.mod = tmp;
      qc.value = cv;
    }
  }

  @Override
  public boolean has(final Flag flag) {
    return flag != Flag.UPD && super.has(flag);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new TransformWith(info, exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  @Override
  public String toString() {
    return toString(' ' + QueryText.UPDATE + ' ');
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr e : exprs) sz += e.exprSize();
    return sz;
  }
}
