package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

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
  private final QNm name;

  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param d function id
   * @param a arguments
   */
  public FunCall(final InputInfo ii, final QNm nm, final int d,
      final Expr[] a) {
    super(ii, a);
    id = d;
    name = nm;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    func = ctx.fun.get(id);
    super.comp(ctx);
    final Expr e = func.expr;

    // [CG] XQuery/Functions: check for inlining
    if(e.value()) {
      // inline simple items
      ctx.compInfo(OPTINLINE, this);
      return func.atomic(ctx, input);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    if(func == null) {
      final Expr e = comp(ctx);
      if(e != this) return e.iter(ctx);
    }
    
    final int al = expr.length;
    final Item[] args = new Item[al];
    // evaluate arguments
    for(int a = 0; a < al; a++) args[a] = ctx.iter(expr[a]).finish();
    // move variables to stack
    final int s = ctx.vars.size();
    for(int a = 0; a < al; a++) {
      ctx.vars.add(func.args[a].bind(args[a], ctx).copy());
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
    ser.openElement(this, NAM, name.atom());
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String desc() {
    return "Function";
  }

  @Override
  public String toString() {
    return "FunCall(" + id + ")";
  }
}
