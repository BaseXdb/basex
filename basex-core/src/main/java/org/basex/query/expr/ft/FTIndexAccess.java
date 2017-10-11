package org.basex.query.expr.ft;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Full-text expression. */
  private final FTExpr ftexpr;
  /** Index context. */
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
    ictx.data(qc, IndexType.FULLTEXT, info);
    final FTIter iter = ftexpr.iter(qc);
    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
        final FTNode it = iter.next();
        if(it != null) {
          // assign scoring
          if(qc.scoring) it.score();
          // cache entry for visualizations or ft:mark/ft:extract
          if(qc.ftPosData != null) qc.ftPosData.add(it.data(), it.pre(), it.matches());
          // remove matches reference to save memory
          it.matches(null);
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
  public boolean removable(final Var var) {
    return ftexpr.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return ftexpr.count(var);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return ftexpr.inline(var, ex, cc) == null ? null : optimize(cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new FTIndexAccess(info, ftexpr.copy(cc, vm), ictx);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return ftexpr.accept(visitor);
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTIndexAccess)) return false;
    final FTIndexAccess f = (FTIndexAccess) obj;
    return ftexpr.equals(f.ftexpr) && ictx.equals(f.ictx);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), ictx.expr(), ftexpr);
  }

  @Override
  public String toString() {
    Expr e = ftexpr;
    if(ftexpr instanceof FTWords) {
      final FTWords ftw = (FTWords) ftexpr;
      if(ftw.mode == FTMode.ANY && ftw.occ == null && ftw.simple) e = ftw.query;
    }
    return Function._FT_SEARCH.toString(ictx.expr(), e);
  }
}
