package org.basex.query.expr.ft;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.index.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContains expression with index access.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTIndexAccess extends Simple {
  /** Index database. */
  private IndexDb db;
  /** Full-text expression. */
  private FTExpr ftexpr;

  /**
   * Constructor.
   * @param info input info
   * @param ftexpr contains, select and optional ignore expression
   * @param db index database
   */
  public FTIndexAccess(final InputInfo info, final FTExpr ftexpr, final IndexDb db) {
    super(info, SeqType.TEXT_ZM);
    this.ftexpr = ftexpr;
    this.db = db;
  }

  @Override
  public NodeIter iter(final QueryContext qc) throws QueryException {
    final FTIter iter = ftexpr.iter(qc);
    return new NodeIter() {
      @Override
      public ANode next() throws QueryException {
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
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
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
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
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
    if(this == obj) return true;
    if(!(obj instanceof FTIndexAccess)) return false;
    final FTIndexAccess f = (FTIndexAccess) obj;
    return ftexpr.equals(f.ftexpr) && db.equals(f.db);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), db, ftexpr);
  }

  @Override
  public void plan(final QueryString qs) {
    Expr expr = ftexpr;
    if(ftexpr instanceof FTWords) {
      final FTWords ftw = (FTWords) ftexpr;
      if(ftw.mode == FTMode.ANY && ftw.occ == null && ftw.simple) expr = ftw.query;
    }
    qs.function(Function._FT_SEARCH, db, expr);
  }
}
