package org.basex.query.xpath.expr;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.locpath.LocPath;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Item;
import org.basex.util.Token;

/**
 * Abstract Compare Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Comparison extends DualExpr {
  /** Expression Type. */
  public Comp type;
  
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to compare with first
   */
  public Comparison(final Expr e1, final Expr e2) {
    super(e1, e2);
  }

  @Override
  public final Bool eval(final XPContext ctx) throws QueryException {
    final Item v1 = ctx.eval(expr1);
    final Item v2 = ctx.eval(expr2);
   
    // don't evaluate empty node sets
    return Bool.get(v1.size() != 0 && v2.size() != 0 && type.eval(v1, v2));
  }
  
  /**
   * Checks if this equality expression is a simple one (LocationPath +
   * XPathValue and equals check),
   * accessing the indexes.
   * @return result of check
   */
  public final boolean simple() {
    return expr1 instanceof LocPath && expr2 instanceof Item;
  }
  
  @Override
  public final String toString() {
    return expr1 + " " + type + " " + expr2;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token(TYPE), Token.token(type.toString()));
    expr1.plan(ser);
    expr2.plan(ser);
    ser.closeElement(this);
  }
}
