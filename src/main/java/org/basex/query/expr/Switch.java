package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Switch expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Switch extends ParseExpr {
  /** Cases. */
  private final SwitchCase[] cases;
  /** Condition. */
  private Expr cond;

  /**
   * Constructor.
   * @param ii input info
   * @param c condition
   * @param sc cases (last one is default case)
   */
  public Switch(final InputInfo ii, final Expr c, final SwitchCase[] sc) {
    super(ii);
    cases = sc;
    cond = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    // operands may not be updating
    cond = checkUp(cond, ctx).comp(ctx);
    for(final SwitchCase sc : cases) sc.comp(ctx);

    // check if none or all return expressions are updating
    final Expr[] tmp = new Expr[cases.length];
    for(int i = 0; i < tmp.length; ++i) tmp[i] = cases[i].expr[0];
    checkUp(ctx, tmp);

    // check if expression can be pre-evaluated
    Expr ex = this;
    if(cond.isValue()) {
      final Item it = cond.item(ctx, input);
      LOOP:
      for(final SwitchCase sc : cases) {
        final int sl = sc.expr.length;
        for(int e = 1; e < sl; e++) {
          if(!sc.expr[e].isValue()) break LOOP;

          // includes check for empty sequence (null reference)
          final Item cs = sc.expr[e].item(ctx, input);
          if(it == cs || cs != null && it != null && it.equiv(input, cs)) {
            ex = sc.expr[0];
            break LOOP;
          }
        }
        if(sl == 1) ex = sc.expr[0];
      }
    }
    if(ex != this) return optPre(ex, ctx);

    // expression could not be pre-evaluated
    type = cases[0].expr[0].type();
    for(int c = 1; c < cases.length; c++) {
      type = type.intersect(cases[c].expr[0].type());
    }
    return ex;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return ctx.iter(getCase(ctx));
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    return ctx.value(getCase(ctx));
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return getCase(ctx).item(ctx, ii);
  }

  @Override
  public boolean uses(final Use u) {
    for(final SwitchCase sc : cases) if(sc.uses(u)) return true;
    return cond.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = cond.count(v);
    for(final SwitchCase sc : cases) c += sc.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    for(final SwitchCase sc : cases) if(!sc.removable(v)) return false;
    return cond.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(final SwitchCase sc : cases) sc.remove(v);
    cond = cond.remove(v);
    return this;
  }

  /**
   * Chooses the selected {@code case} expression.
   * @param ctx query context
   * @return case expression
   * @throws QueryException query exception
   */
  private Expr getCase(final QueryContext ctx) throws QueryException {
    final Item it = cond.item(ctx, input);
    for(final SwitchCase sc : cases) {
      final int sl = sc.expr.length;
      for(int e = 1; e < sl; e++) {
        // includes check for empty sequence (null reference)
        final Item cs = sc.expr[e].item(ctx, input);
        if(it == cs || it != null && cs != null && it.equiv(input, cs))
          return sc.expr[0];
      }
      if(sl == 1) return sc.expr[0];
    }
    // will never be evaluated
    return null;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    cond.plan(ser);
    for(final SwitchCase sc : cases) sc.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(SWITCH + PAR1 + cond + PAR2);
    for(final SwitchCase sc : cases) sb.append(sc.toString());
    return sb.toString();
  }

  @Override
  Expr markTailCalls() {
    for(final SwitchCase sc : cases) sc.markTailCalls();
    return this;
  }
}
