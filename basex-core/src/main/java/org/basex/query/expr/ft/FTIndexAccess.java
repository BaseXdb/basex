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
 * @author BaseX Team 2005-19, BSD License
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
    super(info, SeqType.TXT_ZM);
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
    return refinedValue(qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    return ftexpr.has(flags) || db.has(flags);
  }

  @Override
  public boolean inlineable(final Var var) {
    return ftexpr.inlineable(var) && db.inlineable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return ftexpr.count(var).plus(db.count(var));
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final FTExpr fte = ftexpr.inline(var, ex, cc);
    if(fte != null) ftexpr = fte;
    final IndexDb sub = db.inline(var, ex, cc);
    if(sub != null) db = sub;
    return fte != null || sub != null ? optimize(cc) : this;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new FTIndexAccess(info, ftexpr.copy(cc, vm), db.copy(cc, vm));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return ftexpr.accept(visitor) && db.accept(visitor);
  }

  @Override
  public boolean iterable() {
    return seqType().zeroOrOne() || db.iterable();
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
  public String toString() {
    Expr expr = ftexpr;
    if(ftexpr instanceof FTWords) {
      final FTWords ftw = (FTWords) ftexpr;
      if(ftw.mode == FTMode.ANY && ftw.occ == null && ftw.simple) expr = ftw.query;
    }
    return Function._FT_SEARCH.args(db.source(), expr).substring(1);
  }
}
