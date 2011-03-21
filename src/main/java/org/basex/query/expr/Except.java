package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.AtomType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.util.Array;
import org.basex.util.InputInfo;

/**
 * Except expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Except extends Set {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Except(final InputInfo ii, final Expr[] e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(expr[0].empty()) return optPre(Empty.SEQ, ctx);

    for(int e = 1; e < expr.length; ++e) {
      if(expr[e].empty()) {
        expr = Array.delete(expr, e--);
        ctx.compInfo(OPTREMOVE, desc(), AtomType.EMP);
      }
    }
    // results must always be sorted
    return expr.length == 1 && !dupl ? expr[0] : this;
  }

  @Override
  protected NodeCache eval(final Iter[] iter) throws QueryException {
    final NodeCache ni = new NodeCache().random();

    Item it;
    while((it = iter[0].next()) != null) ni.add(checkNode(it));
    final boolean db = ni.dbnodes();

    for(int e = 1; e != expr.length && ni.size() != 0; ++e) {
      final Iter ir = iter[e];
      while((it = ir.next()) != null) {
        final int i = ni.indexOf(checkNode(it), db);
        if(i != -1) ni.delete(i);
      }
    }
    return ni;
  }

  @Override
  protected NodeIter iter(final Iter[] iter) {
    return new SetIter(iter) {
      @Override
      public ANode next() throws QueryException {
        if(item == null) {
          item = new ANode[iter.length];
          for(int i = 0; i != iter.length; ++i) next(i);
        }

        for(int i = 1; i != item.length; ++i) {
          if(item[0] == null) return null;
          if(item[i] == null) continue;
          final int d = item[0].diff(item[i]);

          if(d < 0) {
            if(i + 1 == item.length) {
              break;
            }
          }
          if(d == 0) {
            next(0);
            i = 0;
          }
          if(d > 0) {
            next(i--);
          }
        }
        final ANode temp = item[0];
        next(0);
        return temp;
      }
    };
  }
}
