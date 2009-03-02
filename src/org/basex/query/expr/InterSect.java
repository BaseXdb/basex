package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.Seq;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;

/**
 * Intersect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Dennis Stratmann
 */
public final class InterSect extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public InterSect(final Expr[] l) {
    super(l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    for(final Expr e : expr) {
      if(e.e()) {
        ctx.compInfo(OPTSIMPLE, this, e);
        return Seq.EMPTY;
      }
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] iter = new Iter[expr.length];
    for(int e = 0; e != expr.length; e++) iter[e] = ctx.iter(expr[e]);
    return duplicates(ctx) ? eval(iter) : iter(iter);
  }
  
  /**
   * Creates a intersect iterator.
   * @param iter iterators
   * @return resulting iterator
   */
  private NodeIter iter(final Iter[] iter) {
    return new NodeIter() {
      final Nod[] items = new Nod[iter.length];

      @Override
      public Nod next() throws QueryException {
        for(int i = 0; i != iter.length; i++) if(!next(i)) return null;

        for(int i = 1; i != items.length; i++) {
          final int d = items[0].diff(items[i]);
          if(d < 0) {
            if(!next(0)) return null;
            i = 0;
          } else if(d > 0) {
            if(!next(i--)) return null;
          }
        }
        return items[0];
      }
      
      private boolean next(final int i) throws QueryException {
        final Item it = iter[i].next();
        if(it == null) return false;
        if(!it.node()) Err.nodes(InterSect.this);
        items[i] = (Nod) it;
        return true;
      }
    };
  }
  
  /**
   * Evaluates the iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private NodIter eval(final Iter[] iter) throws QueryException {
    NodIter seq = new NodIter(true);

    Item it;
    while((it = iter[0].next()) != null) {
      if(!it.node()) Err.nodes(this);
      seq.add((Nod) it);
    }
    
    for(int e = 1; e != expr.length; e++) {
      final NodIter res = new NodIter(true);
      final Iter ir = iter[e];
      while((it = ir.next()) != null) {
        if(!it.node()) Err.nodes(this);
        final Nod node = (Nod) it;
        for(int s = 0; s < seq.size(); s++) {
          if(seq.list[s].is(node)) {
            res.add(node);
            break;
          } 
        }
      }
      seq = res;
    }
    
    /* Intersection of fulltext data for visualization.
     * Problem: ftcontains context not known anymore at this stage
     * Example: //a[b/text() ftcontains 'c' and d/text() ftcontains 'e']
     * 
    if(ftd != null) {
      final IntList il = new IntList();
      for(int i = 0; i < seq.size(); i++) {
        it = seq.list[i];
        if(it instanceof DBNode) {
          NodeMore ci = ((DBNode) it).child();
          while(ci.more()) {
            Item child = ci.next();
            if (child instanceof DBNode) {
              final DBNode dbn = (DBNode) child;
              if (dbn.type == Type.TXT) il.add(dbn.pre); 
            }
          }
        }
      }
      if(il.size == 0) ftd.init();
      else ftd.keep(il.finish());
    }*/

    return seq;
  }

  @Override
  public String toString() {
    return "(" + toString(" & ") + ")";
  }
}
