package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;

/**
 * FLWOR Clause.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class If extends Arr {
  /**
   * Constructor.
   * @param e expression
   * @param t then clause
   * @param s else clause
   */
  public If(final Expr e, final Expr t, final Expr s) {
    super(e, t, s);
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return ctx.iter(expr[ctx.iter(expr[0]).ebv().bool() ? 1 : 2]);
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    if(!expr[0].i()) return this;
    // static result: return then or else branch
    final Expr e = expr[((Item) expr[0]).bool() ? 1 : 2];
    ctx.compInfo(OPTSIMPLE, this, e);
    return e;
  }

  @Override
  public Type returned(final XQContext ctx) {
    final Type t1 = expr[1].returned(ctx);
    return t1 == expr[2].returned(ctx) ? t1 : null;
  }

  @Override
  public String toString() {
    return "if " + expr[0] + " then " + expr[1] + " else " + expr[2];
  }
}
