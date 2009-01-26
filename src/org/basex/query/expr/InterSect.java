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
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * Intersect expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
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
  public Iter iter(final QueryContext ctx) throws QueryException {
    NodIter seq = new NodIter(false);

    Iter iter = ctx.iter(expr[0]);
    Item it;
    while((it = iter.next()) != null) {
      if(!it.node()) Err.nodes(this);
      seq.add((Nod) it);
    }
    
    final IntList il = ctx.ftdata != null ? new IntList() : null;
    for(int e = 1; e != expr.length; e++) {
      final NodIter res = new NodIter(false);
      iter = ctx.iter(expr[e]);
      while((it = iter.next()) != null) {
        if(!it.node()) Err.nodes(this);
        final Nod node = (Nod) it;
        for(int s = 0; s < seq.size; s++) {
          if(CmpN.Comp.EQ.e(seq.list[s], node)) {
            res.add(node);
            if(il != null && it instanceof DBNode)
              il.add(((DBNode) it).pre + 1);
            break;
          } 
        }
      }
      if(ctx.ftdata != null) {
        if(res.size == 0) ctx.ftdata.init();
        else ctx.ftdata.keep(il.finish());
      }
      seq = res;
    }
    return seq;
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
  public String toString() {
    return "(" + toString(" & ") + ")";
  }
}
