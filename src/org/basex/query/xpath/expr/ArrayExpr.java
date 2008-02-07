package org.basex.query.xpath.expr;

import org.basex.data.Serializer;

/**
 * This is an abstract class for array expressions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class ArrayExpr extends Expr {
  /** Array with arguments. */
  public Expr[] exprs;

  @Override
  public final boolean usesPos() {
    for(final Expr expr : exprs) if(expr.usesPos()) return true;
    return false;
  }

  @Override
  public final boolean usesSize() {
    for(final Expr expr : exprs) if(expr.usesSize()) return true;
    return false;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(cmp.getClass() != getClass()) return false;
    final ArrayExpr ex = (ArrayExpr) cmp;
    if(exprs.length != ex.exprs.length) return false;
    for(final Expr expr : exprs) {
      if(!expr.sameAs(ex)) return false;
    }
    return true;
  }

  @Override
  public final String toString() {
    // <cg> i have changed name() to new String(name()), 
    // because name() doesn't return a string in every case SG
    final StringBuilder sb = new StringBuilder(new String(name()) + "(");
    for(int i = 0; i != exprs.length; i++) {
      if(i != 0) sb.append(", ");
      sb.append(exprs[i]);
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    for(final Expr expr : exprs) expr.plan(ser);
    ser.closeElement(this);
  }
}
