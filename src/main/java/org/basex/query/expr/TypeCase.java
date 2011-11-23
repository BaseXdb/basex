package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Case expression for typeswitch.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TypeCase extends Single {
  /** Variable. */
  final Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   * @param r return expression
   */
  public TypeCase(final InputInfo ii, final Var v, final Expr r) {
    super(ii, r);
    var = v;
  }

  @Override
  public TypeCase comp(final QueryContext ctx) throws QueryException {
    return comp(ctx, null);
  }

  /**
   * Compiles the expression.
   * @param ctx query context
   * @param v value to be bound
   * @return resulting item
   * @throws QueryException query exception
   */
  TypeCase comp(final QueryContext ctx, final Value v) throws QueryException {
    if(var.name == null) {
      super.comp(ctx);
    } else {
      final int s = ctx.vars.size();
      ctx.vars.add(v == null ? var : var.bind(v, ctx).copy());
      super.comp(ctx);
      ctx.vars.size(s);
    }
    type = expr.type();
    return this;
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || super.uses(u);
  }

  /**
   * Evaluates the expression.
   * @param ctx query context
   * @param seq sequence to be checked
   * @return resulting item
   * @throws QueryException query exception
   */
  Iter iter(final QueryContext ctx, final Value seq)
      throws QueryException {
    if(var.type != null && !var.type.instance(seq)) return null;
    if(var.name == null) return ctx.iter(expr);

    final int s = ctx.vars.size();
    ctx.vars.add(var.bind(seq, ctx).copy());
    final ValueIter ic = ctx.value(expr).iter();
    ctx.vars.size(s);
    return ic;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAR, var.name != null ? var.name.string() :
      Token.EMPTY);
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(var.type == null ? DEFAULT : CASE);
    if(var.name != null) tb.add(' ');
    return tb.add(var + " " + RETURN + ' ' + expr).toString();
  }

  @Override
  TypeCase markTailCalls() {
    expr = expr.markTailCalls();
    return this;
  }
}
