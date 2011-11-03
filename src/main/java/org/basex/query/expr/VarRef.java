package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.NSLocal;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Variable Reference expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class VarRef extends ParseExpr {
  /** Variable name. */
  Var var;

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
  public Expr comp(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    type = var.type();
    size = var.size();

    // return if variable expression has not yet been assigned
    Expr e = var.expr();
    if(e == null) return this;

    // pre-assign static variables
    final NSLocal ns = ctx.ns;
    ctx.ns = ns.copy();
    if(ctx.nsElem.length != 0) ctx.ns.add(new QNm(EMPTY, ctx.nsElem), input);

    /* Choose variables to be pre-evaluated.
     * If a variable is pre-evaluated, it may not be available for further
     * optimizations (index access, count, ...). On the other hand, repeated
     * evaluation of the same expression is avoided. */
    // [CG][LW] document / clean up the logic here
    if(var.global || ctx.nsElem.length != 0 || ns.size() != 0 ||
        var.type != null || e.uses(Use.CNS) || e instanceof UserFuncCall) {
      e = var.value(ctx);
    }

    ctx.ns = ns;
    return e;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
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
    return var.is(v) ? new Context(input) : this;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof VarRef  && var.sameAs(((VarRef) cmp).var);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, token(var.toString()));
  }

  @Override
  public String desc() {
    return VARBL;
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(var.name.atom()).toString();
  }
}
