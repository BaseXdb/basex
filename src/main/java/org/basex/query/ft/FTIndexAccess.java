package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
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
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    final FTIter ir = ftexpr.iter(ctx);

    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
        final FTNode it = ir.next();
        if(it != null) {
          // add entry to visualization
          if(ctx.ftpos != null) ctx.ftpos.add(it.data, it.pre, it.all);
          // assign scoring, if not done yet
          it.score();
          // remove matches reference to save memory
          it.all = null;
        }
        return it;
      }
    };
  }

  @Override
  public boolean uses(final Use u) {
    return ftexpr.uses(u);
  }

  @Override
  public int count(final Var v) {
    return ftexpr.count(v);
  }

  @Override
  public boolean removable(final Var v) {
    return ftexpr.removable(v);
  }

  @Override
  public Expr remove(final Var v) {
    ftexpr.remove(v);
    return this;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(DATA, ictx.data.meta.name), ftexpr);
  }

  @Override
  public boolean iterable() {
    return ictx.iterable;
  }

  @Override
  public String toString() {
    return Function._DB_FULLTEXT.get(info, Str.get(ictx.data.meta.name),
        ftexpr).toString();
  }
}
