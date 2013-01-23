package org.basex.query.var;

import static org.basex.query.QueryText.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.UserFuncCall;
import org.basex.query.iter.Iter;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.InputInfo;
import org.basex.util.list.*;

/**
 * Local Variable Reference expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class LocalVarRef extends VarRef {
  /** Variable name. */
  public Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   */
  public LocalVarRef(final InputInfo ii, final Var v) {
    super(v.name, ii);
    var = v;
  }

  @SuppressWarnings({ "unused", "null"})
  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {

    type = var.type();
    size = var.size;

    // constant propagation
    if(ctx.isBound(var)) return ctx.get(var);

    Expr e = null; // [LW] ctx.getExpr(var);
    // return if variable expression has not yet been assigned
    if(e == null) return this;

    /* Choose expressions to be pre-evaluated.
     * If a variable is pre-evaluated, it may not be available for further
     * optimizations (index access, count, ...). On the other hand, repeated
     * evaluation of the same expression is avoided.
     *
     * [CG][LW] Variables are currently pre-evaluated if...
     * - namespaces are used
     * - they are given a type
     * - they contain an element constructor (mandatory)
     * - they contain a function call
     */
    if(var.checksType() || e.uses(Use.CNS) || e instanceof UserFuncCall) {
      e = ctx.get(var);
    }

    return e;
  }

  /**
   * Sets the referenced variable.
   * @param v new variable
   */
  public void setVar(final Var v) {
    var = v;
    name = v.name;
    type = var.type();
    size = var.size;
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
  public boolean uses(final Use u) {
    return u == Use.VAR;
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return var.is(v) ? new Context(info) : this;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof LocalVarRef && var.is(((LocalVarRef) cmp).var);
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
  public boolean visitVars(final VarVisitor visitor) {
    return visitor.used(this);
  }

  @Override
  public void checkUp() throws QueryException {
  }

  @Override
  public boolean databases(final StringList db) {
    return true;
  }
}
