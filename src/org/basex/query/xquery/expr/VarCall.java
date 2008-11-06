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
    if(!var.global) return this;
    final NSLocal lc = ctx.ns;
    ctx.ns = lc.copy();
    if(ctx.nsElem.length != 0) ctx.ns.add(new QNm(EMPTY, Uri.uri(ctx.nsElem)));
    final Item it = var.item(ctx);
    ctx.ns = lc;
    return it;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    var = ctx.vars.get(var);
    return var.iter(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, VAR, var.name.str(), NS, timer());
  }

  @Override
  public String color() {
    return "CC99FF";
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
