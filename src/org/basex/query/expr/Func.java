package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * User defined function.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Func extends Single {
  /** Function name, including return type. */
  public Var var;
  /** Arguments. */
  public Var[] args;
  /** Declaration flag. */
  public boolean decl;
  /** Updating flag. */
  public boolean updating;

  /**
   * Function constructor.
   * @param v function name
   * @param a arguments
   * @param d declaration flag
   */
  public Func(final Var v, final Var[] a, final boolean d) {
    super(null);
    var = v;
    args = a;
    decl = d;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    for(final Var v : args) ctx.vars.add(v);
    expr = expr.comp(ctx);
    final boolean u = expr.uses(Use.UPD, ctx);
    if(updating) {
      if(var.type != null) Err.or(UPFUNCTYPE);
      if(!u && !expr.v()) Err.or(UPEXPECTF);
    } else if(u) {
      Err.or(UPNOT);
    }
    ctx.vars.reset(s);
    return this;
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    // evaluate function and reset variable scope
    final Item ci = ctx.item;
    ctx.item = null;
    final Item i = ctx.iter(expr).finish();
    ctx.item = ci;
    return var.type != null ? var.type.cast(i, ctx) : i;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(NAM, var.name.str());
    for(int i = 0; i < args.length; i++) {
      ser.attribute(Token.token(ARG + i), args[i].name.str());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(var.name.str()).add("(...)");
    if(var.type != null) tb.add(" " + AS + " " + var.type);
    if(expr != null) tb.add(" { " + expr + " }");
    return tb.toString();
  }
}
