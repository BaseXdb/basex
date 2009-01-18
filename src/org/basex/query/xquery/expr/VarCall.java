package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.NSLocal;
import org.basex.query.xquery.util.Var;

/**
 * Variable expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class VarCall extends Expr {
  /** Variable name. */
  public Var var;

  /**
   * Constructor.
   * @param v variable
   */
  public VarCall(final Var v) {
    var = v;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    var = ctx.vars.get(var);
    if(var.expr == null) return this;

    // pre-assign static variables
    final NSLocal lc = ctx.ns;
    ctx.ns = lc.copy();
    if(ctx.nsElem.length != 0) ctx.ns.add(new QNm(EMPTY, Uri.uri(ctx.nsElem)));
    //final Expr it = lc.size() != 0 || ctx.nsElem.length != 0 || var.type !=
    //  null || var.expr instanceof FunCall ? var.item(ctx) : var.expr;
    final Item it = var.item(ctx);
    ctx.ns = lc;
    return it;
  }

  /**
   * Checks VarCall Expression for equality.
   * @param v variable
   * @return result of check
   */
  public boolean eq(final Var v) {
    //var.global == v.global
    return var.name.eq(v.name) && var.type == v.type;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    var = ctx.vars.get(var);
    return var.iter(ctx);
  }

  @Override
  public boolean usesVar(final Var v) {
    return v == null || var.eq(v);
  }

  @Override
  public Expr removeVar(final Var v) {
    return var.eq(v) ? new Context() : this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, VAR, var.name.str());
  }

  @Override
  public String color() {
    return "66DDAA";
  }

  @Override
  public String info() {
    return "Variable";
  }

  @Override
  public String toString() {
    return var.toString();
  }
}
