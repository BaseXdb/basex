package org.basex.query.xpath.internal;

import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;

/**
 * Abstract class for internal (optimized) XPath expressions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class InternalExpr extends Expr {
  @Override
  public final Expr comp(final XPContext ctx) {
    return this;
  }

  @Override
  public final boolean usesSize() {
    return false;
  }

  @Override
  public final boolean usesPos() {
    return false;
  }
}
