package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Abstract FTContains expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTContains extends Single {
  /** Full-text expression. */
  public FTExpr ftexpr;

  /**
   * Constructor.
   * @param expr expression
   * @param ftexpr full-text expression
   * @param info input info
   */
  public FTContains(final Expr expr, final FTExpr ftexpr, final InputInfo info) {
    super(info, expr, SeqType.BOOLEAN_O);
    this.ftexpr = ftexpr;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final boolean scoring = qc.scoring;
    final Iter iter = expr.iter(qc);

    final FTLexer tmp = qc.ftLexer, lexer = new FTLexer(new FTOpt());
    qc.ftLexer = lexer;
    try {
      double score = 0;
      int count = 0;
      boolean found = false;
      final FTPosData ftPosData = qc.ftPosData;
      for(Item item; (item = qc.next(iter)) != null;) {
        lexer.init(item.string(info));
        final FTNode it = ftexpr.item(qc, info);
        final FTMatches all = it.matches();
        if(all.matches()) {
          found = true;
          if(scoring) score += it.score();
          // cache entry for visualizations or ft:mark/ft:extract
          if(ftPosData != null && item instanceof DBNode) {
            final DBNode node = (DBNode) item;
            ftPosData.add(node.data(), node.pre(), all);
          }
        }
        count++;
      }
      return found && scoring ? Bln.get(Scoring.avg(score, count)) : Bln.get(found);
    } finally {
      qc.ftLexer = tmp;
    }
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    super.compile(cc);
    ftexpr = ftexpr.compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = expr.simplifyFor(Simplify.STRING, cc);
    return expr.seqType().zero() ? cc.function(Function.BOOLEAN, info, expr) : this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return super.has(flags) || ftexpr.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return super.inlineable(ic) && ftexpr.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(ftexpr.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = expr.inline(ic);
    if(inlined != null) expr = inlined;
    final FTExpr ftinlined = ftexpr.inline(ic);
    if(ftinlined != null) ftexpr = ftinlined;
    return inlined != null || ftinlined != null ? optimize(ic.cc) : null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && ftexpr.accept(visitor);
  }

  @Override
  public int exprSize() {
    return super.exprSize() + ftexpr.exprSize();
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // check if index can be utilized
    final IndexType type = ii.type(expr, IndexType.FULLTEXT);
    if(type == null || !ftexpr.indexAccessible(ii)) return false;

    ii.create(new FTIndexAccess(info, ftexpr, ii.db), true,
        Util.info(OPTINDEX_X_X, "full-text", ftexpr), info);
    return true;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTContains(expr.copy(cc, vm), ftexpr.copy(cc, vm), info));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTContains && ftexpr.equals(((FTContains) obj).ftexpr) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), expr, ftexpr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(expr).token(CONTAINS).token(TEXT).token(ftexpr);
  }
}
