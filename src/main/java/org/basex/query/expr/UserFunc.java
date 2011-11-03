package org.basex.query.expr;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.AtomType;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * User-defined function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class UserFunc extends Single {
  /** Function name. */
  public final QNm name;
  /** Return type. */
  public SeqType ret;
  /** Arguments. */
  public final Var[] args;
  /** Declaration flag. */
  public final boolean declared;
  /** Updating flag. */
  public boolean updating;
  /** Cast flag. */
  private boolean cast;

  /** Compilation flag. */
  private boolean compiled;

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param a arguments
   * @param r return type
   * @param d declaration flag
   */
  public UserFunc(final InputInfo ii, final QNm n, final Var[] a,
      final SeqType r, final boolean d) {
    super(ii, null);
    name = n;
    ret = r;
    args = a;
    declared = d;
    cast = r != null;
  }

  /**
   * Checks the function for updating behavior.
   * @throws QueryException query exception
   */
  public final void check() throws QueryException {
    if(!declared || expr == null) FUNCUNKNOWN.thrw(input, name.atom());

    final boolean u = expr.uses(Use.UPD);
    if(updating) {
      // updating function
      if(ret != null) UPFUNCTYPE.thrw(input);
      if(!u && !expr.vacuous()) UPEXPECTF.thrw(input);
    } else if(u) {
      // uses updates, but is not declared as such
      UPNOT.thrw(input, desc());
    }
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(compiled) return this;
    compiled = true;

    final int s = ctx.vars.size();
    for(final Var v : args) ctx.vars.add(v);
    expr = expr.comp(ctx);
    ctx.vars.reset(s);

    // convert all function calls in tail position to proper tail calls
    if(tco()) expr = expr.markTailCalls();

    // remove redundant cast
    if(ret != null && (ret.type == AtomType.BLN || ret.type == AtomType.FLT ||
        ret.type == AtomType.DBL || ret.type == AtomType.QNM ||
        ret.type == AtomType.URI) && ret.eq(expr.type())) {
      ctx.compInfo(OPTCAST, ret);
      cast = false;
    }
    // returned expression will be ignored
    return this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // evaluate function and reset variable scope
    final Value cv = ctx.value;
    ctx.value = null;
    final Item it = expr.item(ctx, ii);
    ctx.value = cv;
    // optionally promote return value to target type
    return cast ? ret.cast(it, this, false, ctx, input) : it;
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    // evaluate function and reset variable scope
    final Value cv = ctx.value;
    ctx.value = null;
    final Value v = expr.value(ctx);
    ctx.value = cv;
    // optionally promote return value to target type
    return cast ? ret.promote(v, ctx, input) : v;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.attribute(NAM, name.atom());
    for(int i = 0; i < args.length; ++i) {
      ser.attribute(Token.token(ARG + i), args[i].name.atom());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(name.atom());
    tb.add(PAR1).addSep(args, SEP).add(PAR2);
    if(ret != null) tb.add(' ' + AS + ' ' + ret);
    if(expr != null) tb.add(" { " + expr + " }; ");
    return tb.toString();
  }

  /**
   * Checks if this function is tail-call optimizable.
   * @return {@code true} if it is optimizable, {@code false} otherwise
   */
  boolean tco() {
    return true;
  }
}
