package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Variable Reference expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
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
  public void checkUp() throws QueryException {
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    type = var.type();
    size = var.size();

    // return if variable expression has not yet been assigned
    Expr e = var.expr();
    if(e == null) return this;

    /* Choose expressions to be pre-evaluated.
     * If a variable is pre-evaluated, it may not be available for further
     * optimizations (index access, count, ...). On the other hand, repeated
     * evaluation of the same expression is avoided.
     *
     * [CG][LW] Variables are currently pre-evaluated if...
     * - they are global (mandatory)
     * - they are given a type
     * - they contain an element constructor (mandatory)
     * - they contain a function call
     */
    if(var.global || var.type != null || e.uses(Use.CNS) || e instanceof UserFuncCall) {
      e = var.value(ctx);
    }
    return e;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    var = ctx.vars.get(var);
    return var.item(ctx, ii);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    return ctx.iter(var);
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    return ctx.value(var);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || u != Use.CTX && u != Use.NDT &&
      var.expr() != null && var.expr().uses(u);
  }

  @Override
  public int count(final Var v) {
    return var.is(v) ? 1 : 0;
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
    return cmp instanceof VarRef && var.sameAs(((VarRef) cmp).var);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), var);
  }

  @Override
  public String description() {
    return VARBL;
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(var.name.string()).toString();
  }
}
