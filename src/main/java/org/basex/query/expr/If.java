package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Fun;
import org.basex.query.func.FunDef;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * If expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param t then clause
   * @param s else clause
   */
  public If(final InputInfo ii, final Expr e, final Expr t, final Expr s) {
    super(ii, e, t, s);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // check for updating expressions
    expr[0] = checkUp(expr[0], ctx).comp(ctx).compEbv(ctx);
    checkUp(ctx, expr[1], expr[2]);

    // static result: return branch in question
    if(expr[0].value()) return optPre(eval(ctx).comp(ctx), ctx);

    // compile both branches
    for(int e = 1; e != expr.length; ++e) expr[e] = expr[e].comp(ctx);

    // if A then B else B -> B (errors in A will be ignored)
    if(expr[1] == expr[2]) return optPre(expr[1], ctx);

    // if A then true() else false() -> boolean(A)
    if(expr[1] == Bln.TRUE && expr[2] == Bln.FALSE) {
      ctx.compInfo(OPTWRITE, this);
      return FunDef.BOOLEAN.newInstance(input, expr[0]);
    }

    // if A then false() else true() -> not(A)
    if(expr[1] == Bln.FALSE && expr[2] == Bln.TRUE) {
      ctx.compInfo(OPTWRITE, this);
      return FunDef.NOT.newInstance(input, expr[0]);
    }

    // if not(A) then B else C -> if A then C else B
    if(expr[0] instanceof Fun) {
      final Fun fun = (Fun) expr[0];
      if(fun.def == FunDef.NOT) {
        ctx.compInfo(OPTWRITE, this);
        expr[0] = fun.expr[0];
        final Expr tmp = expr[1];
        expr[1] = expr[2];
        expr[2] = tmp;
      }
    }

    type = expr[1].type().intersect(expr[1].type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(eval(ctx));
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return eval(ctx).atomic(ctx, input);
  }

  /**
   * Evaluates the condition and returns the correct expression.
   * @param ctx query context
   * @return resulting expression
   * @throws QueryException query exception
   */
  private Expr eval(final QueryContext ctx) throws QueryException {
    return expr[expr[0].ebv(ctx, input).bool(input) ? 1 : 2];
  }

  @Override
  public boolean vacuous() {
    return expr[1].vacuous() || expr[2].vacuous();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr[0].plan(ser);
    ser.openElement(THN);
    expr[1].plan(ser);
    ser.closeElement();
    ser.openElement(ELS);
    expr[2].plan(ser);
    ser.closeElement();
    ser.closeElement();
  }

  @Override
  public String toString() {
    return IF + '(' + expr[0] + ") " + THEN + ' ' + expr[1] + ' ' +
      ELSE + ' ' + expr[2];
  }
}
