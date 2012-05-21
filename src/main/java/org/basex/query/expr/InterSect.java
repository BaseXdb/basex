package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Intersect expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class InterSect extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  public InterSect(final InputInfo ii, final Expr[] l) {
    super(ii, l);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return oneIsEmpty() ? optPre(null, ctx) : this;
  }

  @Override
  protected NodeSeqBuilder eval(final Iter[] iter) throws QueryException {
    NodeSeqBuilder nc = new NodeSeqBuilder();

    for(Item it; (it = iter[0].next()) != null;) nc.add(checkNode(it));
    final boolean db = nc.dbnodes();

    for(int e = 1; e != expr.length && nc.size() != 0; ++e) {
      final NodeSeqBuilder nt = new NodeSeqBuilder().check();
      final Iter ir = iter[e];
      for(Item it; (it = ir.next()) != null;) {
        final ANode n = checkNode(it);
        final int i = nc.indexOf(n, db);
        if(i != -1) nt.add(n);
      }
      nc = nt;
    }
    return nc;
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public ANode next() throws QueryException {
        if(item == null) item = new ANode[iter.length];

        for(int i = 0; i != iter.length; ++i) if(!next(i)) return null;

        for(int i = 1; i != item.length;) {
          final int d = item[0].diff(item[i]);
          if(d > 0) {
            if(!next(i)) return null;
          } else if(d < 0) {
            if(!next(0)) return null;
            i = 1;
          } else {
            ++i;
          }
        }
        return item[0];
      }
    };
  }
}
