package org.basex.query.var;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Local Variable Reference expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarRef extends ParseExpr {
  /** Variable name. */
  public final Var var;

  /**
   * Constructor.
   * @param info input info
   * @param var variable
   */
  public VarRef(final InputInfo info, final Var var) {
    super(info);
    this.var = var;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) {
    return optimize(qc, scp);
  }

  @Override
  public VarRef optimize(final QueryContext qc, final VarScope scp) {
    seqType = var.seqType();
    size = var.size;
    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return qc.get(var).item(qc, ii);
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return qc.get(var).iter();
  }

  @Override
  public Value value(final QueryContext qc) {
    return qc.get(var);
  }

  @Override
  public Data data() {
    return var.data;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return var.is(v) ? VarUsage.ONCE : VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var v, final Expr ex) {
    // [LW] Is copying always necessary?
    return var.is(v) ? ex.isValue() ? ex : ex.copy(qc, scp) : null;
  }

  @Override
  public VarRef copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Var nw = vs.get(var.id);
    return new VarRef(info, nw != null ? nw : var).optimize(qc, scp);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof VarRef && var.is(((VarRef) cmp).var);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    var.plan(e);
    plan.add(e);
  }

  @Override
  public String description() {
    return VARBL;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.used(this);
  }

  @Override
  public void checkUp() {
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(var.name.toString()).add('_').addInt(var.id).toString();
  }

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public int exprSize() {
    return 1;
  }
}
