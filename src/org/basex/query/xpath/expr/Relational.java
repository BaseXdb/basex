package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.XPOptimizer;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Comp;
import org.basex.query.xpath.values.Item;
import static org.basex.query.xpath.XPText.*;

/**
 * XPath Relational Expression. Can express: LESS (&lt;) LESS_OR_EQUALS (&lt;=)
 * GREATER (&gt;) GREATER_OR_EQUALS (&gt;=)
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Relational extends Comparison {
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to be compared with
   * @param t see class description
   */
  public Relational(final Expr e1, final Expr e2, final Comp t) {
    super(e1, e2);
    type = t;
  }

  @Override
  public Expr comp(final XPContext ctx) throws QueryException {
    expr1 = expr1.comp(ctx);
    expr2 = expr2.comp(ctx);

    if(expr1 instanceof Item && ((Item) expr1).size() == 0 ||
       expr2 instanceof Item && ((Item) expr2).size() == 0) {
      ctx.compInfo(OPTEQ1);
      return Bool.FALSE;
    }

    XPOptimizer.addText(expr1, ctx);
    XPOptimizer.addText(expr2, ctx);

    if(expr1 instanceof Item && expr2 instanceof Item) {
      ctx.compInfo(OPTRELATIONAL);
      return Bool.get(type.eval((Item) expr1, (Item) expr2));
    }
    return this;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Relational)) return false;
    final Relational ex = (Relational) cmp;
    if(expr1.getClass() != ex.expr1.getClass()) return false;
    if(expr2.getClass() != ex.expr2.getClass()) return false;
    return expr1.sameAs(ex.expr1) && expr2.sameAs(ex.expr2);
  }
}
