package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.Var;

/**
 * Variable expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class VarCall extends Expr {
  /** Variable name. */
  private Var var;
  
  /**
   * Constructor.
   * @param v variable
   */
  public VarCall(final Var v) {
    var = v;
  }
  
  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Var v = ctx.vars.get(var);
    if(v == null) Err.or(VARNOTDEFINED, var);
    var = v;
    return v.iter(ctx);
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, VAR, var.name.str());
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
