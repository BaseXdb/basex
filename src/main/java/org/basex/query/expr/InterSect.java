package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
      if(!e.empty()) continue;
      ctx.compInfo(OPTSIMPLE, this, e);
      return Seq.EMPTY;
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
   * Evaluates the iterators.
   * @param iter iterators
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private NodIter eval(final Iter[] iter) throws QueryException {
    NodIter ni = new NodIter(true);

    Item it;
    while((it = iter[0].next()) != null) {
      if(!it.node()) Err.nodes(this, it);
      ni.add((Nod) it);
    }
    final boolean db = ni.dbnodes();

    for(int e = 1; e != expr.length && ni.size() != 0; e++) {
      final NodIter res = new NodIter(true);
      final Iter ir = iter[e];
      while((it = ir.next()) != null) {
        if(!it.node()) Err.nodes(this, it);
        final Nod node = (Nod) it;

        if(db && node instanceof DBNode) {
          // optimization: use binary search
          if(ni.contains((DBNode) node)) res.add(node);
        } else {
          for(int n = 0; n < ni.size(); n++) {
            if(ni.get(n).is(node)) {
              res.add(node);
              break;
            }
          }
        }
      }
      ni = res;
    }
    return ni;
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
        if(!it.node()) Err.nodes(InterSect.this, it);
        items[i] = (Nod) it;
        return true;
      }
    };
  }

  @Override
  public String toString() {
    return "(" + toString(" & ") + ")";
  }
}
