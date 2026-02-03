package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Index database. */
  private IndexDb db;
  /** Full-text expression. */
  private FTExpr ftexpr;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param ftexpr contains, select and optional ignore expression
   * @param db index database
   */
  public FTIndexAccess(final InputInfo info, final FTExpr ftexpr, final IndexDb db) {
    super(info, Types.TEXT_ZM);
    this.ftexpr = ftexpr;
    this.db = db;
    exprType.data(db.data());
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final FTIter iter = ftexpr.iter(qc);
    return new NodeIter() {
      @Override
      public XNode next() throws QueryException {
        final FTNode item = iter.next();
        if(item != null) {
          // assign scoring
          if(qc.scoring) item.score();
          // cache entry for visualizations or ft:mark/ft:extract
          if(qc.ftPosData != null) qc.ftPosData.add(item.data(), item.pre(), item.matches());
          // remove matches reference to save memory
          item.matches(null);
        }
        return item;
      }
    };
  }

  @Override
  public boolean has(final Flag... flags) {
    return ftexpr.has(flags) || db.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return ftexpr.inlineable(ic) && db.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return ftexpr.count(var).plus(db.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final FTExpr ftinlined = ftexpr.inline(ic);
    if(ftinlined != null) ftexpr = ftinlined;
    final IndexDb inlined = db.inline(ic);
    if(inlined != null) db = inlined;
    return ftinlined != null || inlined != null ? optimize(ic.cc) : this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTIndexAccess(info, ftexpr.copy(cc, vm), db.copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return ftexpr.accept(visitor) && db.accept(visitor);
  }

  @Override
  public boolean ddo() {
    return true;
  }

  @Override
  public int exprSize() {
    return ftexpr.exprSize() + db.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final FTIndexAccess fti && ftexpr.equals(fti.ftexpr) &&
        db.equals(fti.db);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), db, ftexpr);
  }

  @Override
  public void toString(final QueryString qs) {
    Expr expr = ftexpr;
    if(ftexpr instanceof final FTWords ftw) {
      if(ftw.mode == FTMode.ANY && ftw.occ == null && ftw.simple) expr = ftw.query;
    }
    qs.function(Function._FT_SEARCH, db, expr);
  }
}
