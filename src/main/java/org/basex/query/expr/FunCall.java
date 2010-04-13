package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FunCall extends Arr {
  /** Function id reference. */
  private final int id;
  /** Function reference. */
  private Func func;
  /** Function name. */
  private QNm name;

  /**
   * Function constructor.
   * @param nm function name
   * @param i function id
   * @param a arguments
   */
  public FunCall(final QNm nm, final int i, final Expr[] a) {
    super(a);
    id = i;
    name = nm;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    func = ctx.fun.get(id);
    return super.comp(ctx);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(func == null) comp(ctx);

    final int s = ctx.vars.size();
    for(int a = 0; a < expr.length; a++) {
      ctx.vars.add(func.args[a].bind(ctx.iter(expr[a]).finish(), ctx).copy());
    }
    // evaluate function and reset variable scope
    final Item im = ctx.iter(func).finish();
    ctx.vars.reset(s);
    return im.iter();
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return func.returned(ctx);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD ? (func == null ? ctx.fun.get(id) : func).updating :
      super.uses(u, ctx);
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, name.str());
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return "Function";
  }

  @Override
  public String toString() {
    return "FunCall(" + id + ")";
  }
}
