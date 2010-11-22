package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Simple;
import org.basex.query.item.FTNode;
import org.basex.query.item.Nod;
import org.basex.query.iter.FTIter;
import org.basex.query.iter.NodeIter;
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
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = ftexpr.iter(ctx);

    return new NodeIter() {
      @Override
      public Nod next() throws QueryException {
        final FTNode it = ir.next();
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
    ser.openElement(this, DATA, token(ictx.data.meta.name));
    ftexpr.plan(ser);
    ser.closeElement();
  }

  @Override
  public boolean duplicates() {
    return ictx.dupl;
  }

  @Override
  public String toString() {
    return name() + PAR1 + "\"" + ictx.data.meta.name + "\"" +
      SEP + ftexpr + PAR2;
  }
}
