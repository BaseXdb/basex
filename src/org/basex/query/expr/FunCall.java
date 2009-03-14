package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.Iter;
import org.basex.query.util.NSLocal;
import org.basex.util.Token;

/**
 * Function call.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FunCall extends Arr {
  /** Function id reference. */
  private final int id;
  /** Function reference. */
  private Func func;

  /**
   * Function constructor.
   * @param i function id
   * @param a arguments
   */
  public FunCall(final int i, final Expr[] a) {
    super(a);
    id = i;
  }
  
  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    func = ctx.fun.get(id);
    return super.comp(ctx);
  }
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    final NSLocal lc = ctx.ns;
    ctx.ns = lc.copy();
    if(func == null) func = ctx.fun.get(id);

    for(int a = 0; a < expr.length; a++) {
      ctx.vars.add(func.args[a].bind(ctx.iter(expr[a]).finish(), ctx).clone());
    }
    // evaluate function and reset variable scope
    final Iter ir = ctx.iter(func);
    ctx.vars.reset(s);
    ctx.ns = lc;
    return ir;
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return func != null ? func.returned(ctx) : super.returned(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, ID, Token.token(id));
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
