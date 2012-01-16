package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.FuncCall;
import org.basex.query.func.Function;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * If expression.
 *
 * @author BaseX Team 2005-12, BSD License
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

    // static condition: return branch in question
    if(expr[0].isValue()) return optPre(eval(ctx).comp(ctx), ctx);

    // compile both branches
    for(int e = 1; e != expr.length; ++e) expr[e] = expr[e].comp(ctx);

    // if A then B else B -> B (errors in A will be ignored)
    if(expr[1].sameAs(expr[2])) return optPre(expr[1], ctx);

    // if not(A) then B else C -> if A then C else B
    if(expr[0].isFunction(Function.NOT)) {
      ctx.compInfo(OPTWRITE, this);
      expr[0] = ((FuncCall) expr[0]).expr[0];
      final Expr tmp = expr[1];
      expr[1] = expr[2];
      expr[2] = tmp;
    }

    // if A then true() else false() -> boolean(A)
    if(expr[1] == Bln.TRUE && expr[2] == Bln.FALSE) {
      ctx.compInfo(OPTWRITE, this);
      return compBln(expr[0]);
    }

    // if A then false() else true() -> not(A)
    // if A then B else true() -> not(A) or B
    if(expr[1].type().eq(SeqType.BLN) && expr[2] == Bln.TRUE) {
      ctx.compInfo(OPTWRITE, this);
      final Expr e = Function.NOT.get(input, expr[0]);
      return expr[1] == Bln.FALSE ? e : new Or(input, e, expr[1]);
    }

    type = expr[1].type().intersect(expr[2].type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(eval(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return ctx.value(eval(ctx));
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return eval(ctx).item(ctx, input);
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
  public boolean isVacuous() {
    return expr[1].isVacuous() || expr[2].isVacuous();
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

  @Override
  Expr markTailCalls() {
    expr[1] = expr[1].markTailCalls();
    expr[2] = expr[2].markTailCalls();
    return this;
  }
}
