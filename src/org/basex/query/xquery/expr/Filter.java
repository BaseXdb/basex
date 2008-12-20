package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;

/**
 * Filter Expression filtering a nodeset. This Expression is invalid for other
 * types as node sets.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class Filter extends Expr {
  /** Expression to be filtered. */
  public Expr expr;
  /** Predicate. */
  public Expr[] preds;

  /**
   * Constructor.
   * @param e Expression (has to result in a nodeset!)
   * @param p Predicate to filter nodeset
   */
  public Filter(final Expr e, final Expr[] p) {
    expr = e;
    preds = p;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    return new Iter() {
      final Iter iter = ctx.iter(expr);
      @Override
      public Item next() throws XQException {
       final Item i = iter.next();
       if (i == null) return i;
       final Item tmp = ctx.item;
       ctx.item = i;
       Item t = null;
       for (int j = 0; j < preds.length; j++) {
         ctx.item = i;
         t = ctx.iter(preds[j]).next();
         if (!t.bool()) return next(); 
       }
       ctx.item = tmp;
       return i;
      }
    };
  }
  
  @Override
  public boolean uses(final Using use) {
    return expr.uses(use);
  }
  
  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    expr = expr.comp(ctx);
    for (int i = 0; i < preds.length; i++)
      preds[i] = preds[i].comp(ctx);
    return this; 
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    expr.plan(ser);
    for (int i = 0; i < preds.length; i++)
      preds[i].plan(ser);
    ser.closeElement();
  }

  @Override
  public String color() {
    return "FF9999";
  }

  /**
   * Build string with preds.
   * @return String
   */
  private String preds() {
    final StringBuilder sb = new StringBuilder();
    for(int p = 0; p < preds.length; p++) {
      sb.append("[" + preds[p] + "]");
    }
    return sb.toString();
  }

  
  @Override
  public String toString() {
    return BaseX.info("%(%%)", name(), expr, preds());
  }
}
