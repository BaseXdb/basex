package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;
import org.basex.util.Array;

/**
 * Union Expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Union extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Union(final Expr[] e) {
    super(e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    final int el = expr.length;
    for(int e = 0; e != expr.length; e++) {
      if(expr[e].e()) expr = Array.delete(expr, e--);
    }
    if(el != expr.length) ctx.compInfo(OPTEMPTY);
    return this;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[expr.length];
    for(int e = 0; e != expr.length; e++) iter[e] = ctx.iter(expr[e]);
    return duplicates(ctx) ? eval(iter) : iter(iter);
  }

  /**
   * Creates a union iterator.
   * @param iter iterators
   * @return resulting iterator
   */
  private NodeIter iter(final Iter[] iter) {
    return new NodeIter() {
      Nod[] items;

      @Override
      public Nod next() throws QueryException {
        if(items == null) { 
          items = new Nod[iter.length];
          for(int i = 0; i != iter.length; i++) next(i);
        }

        int m = -1;
        for(int i = 0; i != items.length; i++) {
          if(items[i] == null) continue;
          final int d = m == -1 ? 1 : items[m].diff(items[i]);
          if(d == 0) {
            next(i--);
          } else if(d > 0) {
            m = i;
          }
        }
        if(m == -1) return null;
        
        final Nod it = items[m];
        next(m);
        return it;
      }
      
      private void next(final int i) throws QueryException {
        final Item it = iter[i].next();
        if(it != null && !it.node()) Err.nodes(Union.this);
        items[i] = (Nod) it;
      }
    };
  }

  /**
   * Evaluates the iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private NodeIter eval(final Iter[] iter) throws QueryException {
    final NodIter ni = new NodIter(true);
    for(final Iter ir : iter) {
      Item it;
      while((it = ir.next()) != null) {
        if(!it.node()) Err.nodes(this);
        ni.add((Nod) it);
      }
    }
    return ni;
  }
  
  @Override
  public String toString() {
    return "(" + toString(" | ") + ")";
  }
}
