package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Full-text expression. */
  private final FTExpr ftexpr;
  /** Database name. */
  private final IndexContext ictx;

  /**
   * Constructor.
   * @param info input info
   * @param ftexpr contains, select and optional ignore expression
   * @param ictx index context
   */
  public FTIndexAccess(final InputInfo info, final FTExpr ftexpr, final IndexContext ictx) {
    super(info);
    this.ftexpr = ftexpr;
    this.ictx = ictx;
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final FTIter ir = ftexpr.iter(qc);

    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
        final FTNode it = ir.next();
        if(it != null) {
          // cache entry for visualizations or ft:mark/ft:extract
          if(qc.ftPosData != null) qc.ftPosData.add(it.data, it.pre, it.all);
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
  public boolean has(final Flag flag) {
    return ftexpr.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return ftexpr.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return ftexpr.count(v);
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    return ftexpr.inline(qc, scp, v, e) == null ? null : optimize(qc, scp);
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTIndexAccess(info, ftexpr.copy(qc, scp, vs), ictx);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(ictx.data.meta.name) && ftexpr.accept(visitor);
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
  public int exprSize() {
    return ftexpr.exprSize() + 1;
  }

  @Override
  public String toString() {
    Expr e = ftexpr;
    if(ftexpr instanceof FTWords) {
      final FTWords f = (FTWords) ftexpr;
      if(f.mode == FTMode.ANY && f.occ == null) e = f.query;
    }
    return Function._FT_SEARCH.get(null, info, Str.get(ictx.data.meta.name), e).toString();
  }
}
