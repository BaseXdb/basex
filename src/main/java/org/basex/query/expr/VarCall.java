package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.util.NSLocal;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Variable expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class VarCall extends ParseExpr {
  /** Variable name. */
  Var var;

  /**
   * Constructor.
   * @param ii input info
   * @param v variable
   */
  public VarCall(final InputInfo ii, final Var v) {
    super(ii);
    var = v;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    // return if variable expression has not yet been assigned
    if(var.expr == null) return this;

    // pre-assign static variables
    final NSLocal lc = ctx.ns;
    ctx.ns = lc.copy();
    if(ctx.nsElem.length != 0)
      ctx.ns.add(new QNm(EMPTY, Uri.uri(ctx.nsElem)), input);

    /* Choose variables to be pre-evaluated.
     * If a variable is pre-evaluated, it may not be available for further
     * optimizations (IndexAceess, count, ...). On the other hand, multiple
     * evaluations of the same expression can be avoided here. */
    Expr e = var.expr;
    if(ctx.nsElem.length != 0 || lc.size() != 0 || var.type != null ||
        var.global || e.uses(Use.FRG, ctx) || e instanceof FunCall) {
      e = var.item(ctx);
    }
    ctx.ns = lc;
    return e;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    var = ctx.vars.get(var);
    return ctx.iter(var);
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.VAR || (u == Use.POS ? var.returned(ctx).mayBeNum() :
        u != Use.CTX && var.expr != null && var.expr.uses(u, ctx));
  }

  @Override
  public boolean removable(final Var v, final QueryContext ctx) {
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return var.eq(v) ? new Context(input) : this;
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return var.returned(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, VAR, var.name.atom());
  }

  @Override
  public String color() {
    return "66DDAA";
  }

  @Override
  public String desc() {
    return "Variable";
  }

  @Override
  public String toString() {
    return var.toString();
  }
}
