package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * User-defined function.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Func extends Single {
  /** Function name, including return type. */
  public final Var var;
  /** Arguments. */
  public final Var[] args;
  /** Declaration flag. */
  public final boolean declared;
  /** Updating flag. */
  public boolean updating;

  /**
   * Function constructor.
   * @param ii input info
   * @param v function name
   * @param a arguments
   * @param d declaration flag
   */
  public Func(final InputInfo ii, final Var v, final Var[] a, final boolean d) {
    super(ii, null);
    var = v;
    args = a;
    declared = d;
  }

  /**
   * Checks the function for updating behavior.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    if(!declared) Err.or(input, FUNCUNKNOWN, var.name.atom());

    final boolean u = expr.uses(Use.UPD);
    if(updating) {
      // updating function
      if(var.type != null) Err.or(input, UPFUNCTYPE);
      if(!u && !expr.vacuous()) Err.or(input, UPEXPECTF);
    } else if(u) {
      // uses updates, but is not declared as such
      Err.or(input, UPNOT, desc());
    }
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.vars.size();
    for(final Var v : args) ctx.vars.add(v);
    expr = expr.comp(ctx);
    ctx.vars.reset(s);

    // returned expression will be ignored
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    // evaluate function and reset variable scope
    final Value cv = ctx.value;
    ctx.value = null;
    final Value v = ctx.iter(expr).finish();
    ctx.value = cv;
    return (var.type != null ? var.type.cast(v, ctx, input) : v).iter(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(NAM, var.name.atom());
    for(int i = 0; i < args.length; ++i) {
      ser.attribute(Token.token(ARG + i), args[i].name.atom());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(var.name.atom());
    tb.add('(').add(args, ", ").add(')');
    if(var.type != null) tb.add(' ' + AS + ' ' + var.type);
    if(expr != null) tb.add(" { " + expr + " }; ");
    return tb.toString();
  }
}
