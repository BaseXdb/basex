package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TypeSwitch extends ParseExpr {
  /** Cases. */
  private final TypeCase[] cases;
  /** Condition. */
  private Expr ts;

  /**
   * Constructor.
   * @param ii input info
   * @param t typeswitch expression
   * @param c case expressions
   */
  public TypeSwitch(final InputInfo ii, final Expr t, final TypeCase[] c) {
    super(ii);
    ts = t;
    cases = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    ts = checkUp(ts, ctx).comp(ctx);
    final Expr[] tmp = new Expr[cases.length];
    for(int i = 0; i < cases.length; ++i) tmp[i] = cases[i].expr;
    checkUp(ctx, tmp);

    // static condition: return branch in question
    if(ts.value()) {
      for(final TypeCase c : cases) {
        if(c.var.type == null || c.var.type.instance(ts.value(ctx)))
          return optPre(c.comp(ctx, (Value) ts).expr, ctx);
      }
    }

    // compile branches
    for(final TypeCase c : cases) c.comp(ctx);

    // return result if all branches are equal (e.g., empty)
    boolean eq = true;
    for(int i = 1; i < cases.length; ++i) {
      eq &= cases[i - 1].expr.sameAs(cases[i].expr);
    }
    if(eq) return optPre(null, ctx);

    // evaluate return type
    type = cases[0].type();
    for(int c = 1; c < cases.length; ++c) {
      type = type.intersect(cases[c].type());
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Value seq = ctx.value(ts);
    for(final TypeCase c : cases) {
      final Iter iter = c.iter(ctx, seq);
      if(iter != null) return iter;
    }
    // will never happen
    throw Util.notexpected();
  }

  @Override
  public boolean vacuous() {
    for(final TypeCase c : cases) if(!c.expr.vacuous()) return false;
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR) return true;
    for(final TypeCase c : cases) if(c.uses(u)) return true;
    return ts.uses(u);
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final TypeCase t : cases) c += t.count(v);
    return c + ts.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final TypeCase c : cases) if(!c.removable(v)) return false;
    return ts.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(int c = 0; c < cases.length; ++c) cases[c].remove(v);
    ts = ts.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final TypeCase c : cases) c.plan(ser);
    ts.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(TYPESWITCH + PAR1 + ts + PAR2 + ' ').addSep(
        cases, SEP).toString();
  }

  @Override
  Expr markTailCalls() {
    for(int i = 0; i < cases.length; i++) cases[i] = cases[i].markTailCalls();
    return this;
  }
}
