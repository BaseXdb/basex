package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Simple;
import org.basex.query.item.FTNodeItem;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Fulltext expression. */
  final FTExpr ftexpr;
  /** Fulltext parser. */
  final FTTokenizer ft;
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param ex contains, select and optional ignore expression
   * @param ftt FTTokenizer
   * @param ic index context
   */
  public FTIndexAccess(final FTExpr ex, final FTTokenizer ftt,
      final IndexContext ic) {
    ftexpr = ex;
    ft = ftt;
    ictx = ic;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter(){
      @Override
      public Item next() throws QueryException {
        final FTTokenizer tmp = ctx.ftitem;
        ctx.ftitem = ft;
        final FTNodeItem it = ftexpr.iter(ctx).next();
        ctx.ftitem = tmp;
        if (it.ftn.ip != null && it.ftn.p !=  null && ctx.ftdata != null) {
          ctx.ftdata.add(it.ftn.ip.finish(), it.ftn.p.finish());
        }
        return it.score() == 0 ? null : it;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ftexpr.plan(ser);
    ser.closeElement();
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return ictx.dupl;
  }

  @Override
  public String toString() {
    return "FTContainsIndex(" + ftexpr + ")";
  }
}
