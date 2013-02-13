package org.basex.query.var;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.Iter;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Local Variable Reference expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class VarRef extends ParseExpr {
  /** Variable name. */
  public Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   */
  public VarRef(final InputInfo ii, final Var v) {
    super(ii);
    var = v;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {

    type = var.type();
    size = var.size;

    // constant propagation
    return ctx.isBound(var) ? ctx.get(var) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return ctx.get(var).item(ctx, ii);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.get(var).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return ctx.get(var);
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
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    return v.is(var) ? e : null;
  }

  @Override
  public VarRef copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    final Var nw = vs.get(var.id);
    return new VarRef(info, nw != null ? nw : var);
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
  public void checkUp() throws QueryException {
  }

  @Override
  public boolean databases(final StringList db) {
    return true;
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(var.name.toString()).toString();
  }

  @Override
  public boolean uses(final Use u) {
    return false;
  }

  @Override
  public int exprSize() {
    return 1;
  }
}
