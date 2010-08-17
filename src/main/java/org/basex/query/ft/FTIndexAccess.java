package org.basex.query.ft;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.Simple;
import org.basex.query.item.FTItem;
import org.basex.query.item.Item;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * FTContains expression with index access.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Scoring flag. */
  final boolean scoring;
  /** Full-text expression. */
  private final FTExpr ftexpr;
  /** Index context. */
  private final IndexContext ictx;

  /**
   * Constructor.
   * @param ii input info
   * @param ex contains, select and optional ignore expression
   * @param ic index context
   */
  public FTIndexAccess(final InputInfo ii, final FTExpr ex,
      final IndexContext ic) {

    super(ii);
    ftexpr = ex;
    ictx = ic;
    scoring = !ic.ctx.ftfast;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = ftexpr.iter(ctx);

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final FTItem it = ir.next();
        if(it != null) {
          // add entry to visualization
          if(ctx.ftpos != null) ctx.ftpos.add(it.pre, it.all);
          // assign scoring, if necessary and not done yet
          if(scoring) it.score();
          // remove matches reference to save memory
          it.all = null;
        }
        return it;
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
  public boolean duplicates() {
    return ictx.dupl;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof FTIndexAccess;
  }

  @Override
  public String toString() {
    return name() + "(" + ftexpr + ")";
  }
}
