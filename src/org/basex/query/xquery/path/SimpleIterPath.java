package org.basex.query.xquery.path;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;

/**
 * Path Expression when the axis is a descendant.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Dennis Stratmann
 */
public final class SimpleIterPath extends Path {
  /** Step. */
  NodeIter step = null;
  /** First call of class. */
  boolean first = true;

  /**
   * Constructor.
   * @param r root expression
   * @param p expression list
   */
  public SimpleIterPath(final Expr r, final Expr[] p) {
    super(r, p);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    
    return new Iter() {
      final Item item = ctx.iter(root).finish();  
      
      @Override
      public Item next() throws XQException  {
        if (first) {
          System.out.println("*** " + ctx.query);
          ctx.item = item;
          step = (NodeIter) (ctx.iter(expr[0]));
          first = false;
        }
        Nod it = step.next();
        ctx.item = it;
        return it;
      }
    };
  }
}
