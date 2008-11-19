package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.NSLocal;
import org.basex.util.Token;

/**
 * User defined function.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FunCall extends Arr {
  /** Function id reference. */
  public int id;

  /**
   * Function constructor.
   * @param i function id
   * @param e expressions
   */
  public FunCall(final int i, final Expr[] e) {
    super(e);
    id = i;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final int s = ctx.vars.size();
    final NSLocal lc = ctx.ns;
    ctx.ns = lc.copy();

    // add function variables
    final Func func = ctx.fun.get(id);
    for(int a = 0; a < func.args.length; a++) {
      ctx.vars.add(func.args[a].item(ctx.iter(expr[a]).finish(), ctx).clone());
    }

    // evaluate function and reset variable scope
    final Item it = ctx.item;
    ctx.item = null;
    final Item i = ctx.iter(func.expr).finish();
    ctx.vars.reset(s);
    ctx.item = it;
    ctx.ns = lc;

    // cast return type
    final SeqType t = func.var.type;
    return (t != null ? t.cast(i, ctx) : i).iter();
  }

  @Override
  public String info() {
    return "Function";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, ID, Token.token(id));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + id + ")";
  }
}
