package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class TypeSwitch extends ParseExpr {
  /** Cases. */
  private final TypeCase[] cs;
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
    cs = c;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    ts = checkUp(ts, ctx).comp(ctx);
    final Expr[] tmp = new Expr[cs.length];
    for(int i = 0; i < cs.length; ++i) tmp[i] = cs[i].expr;
    checkUp(ctx, tmp);

    // static condition: return branch in question
    if(ts.value()) {
      for(final TypeCase c : cs) {
        if(c.var.type == null || c.var.type.instance(ts.iter(ctx)))
          return optPre(c.comp(ctx, (Value) ts).expr, ctx);
      }
    }

    // compile branches
    for(final TypeCase c : cs) c.comp(ctx);

    // return result if all branches are equal (e.g., empty)
    boolean eq = true;
    for(int i = 1; i < cs.length; ++i) eq &= cs[i - 1].expr.sameAs(cs[i].expr);
    if(eq) return optPre(Empty.SEQ, ctx);

    // evaluate return type
    type = cs[0].type();
    for(int c = 1; c < cs.length; ++c) type = type.intersect(cs[c].type());
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter seq = ItemIter.get(ctx.iter(ts));
    for(final TypeCase c : cs) {
      seq.reset();
      final Iter iter = c.iter(ctx, seq);
      if(iter != null) return iter;
    }
    // will never happen
    return null;
  }

  @Override
  public boolean vacuous() {
    for(final TypeCase c : cs) if(!c.expr.vacuous()) return false;
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    if(u == Use.VAR) return true;
    for(final TypeCase c : cs) if(c.uses(u)) return true;
    return ts.uses(u);
  }

  @Override
  public boolean uses(final Var v) {
    for(final TypeCase c : cs) if(c.uses(v)) return true;
    return ts.uses(v);
  }

  @Override
  public boolean removable(final Var v) {
    for(final TypeCase c : cs) if(!c.removable(v)) return false;
    return ts.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    for(int c = 0; c < cs.length; ++c) cs[c].remove(v);
    ts = ts.remove(v);
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final TypeCase c : cs) c.plan(ser);
    ts.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(TYPESWITCH + PAR1 + ts + PAR2 + ' ').addSep(
        cs, SEP).toString();
  }
}
