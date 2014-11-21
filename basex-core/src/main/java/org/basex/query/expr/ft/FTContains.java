package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTContains extends Single {
  /** Full-text parser. */
  private final FTLexer lex;
  /** Full-text expression. */
  public FTExpr ftexpr;

  /**
   * Constructor.
   * @param expr expression
   * @param ftexpr full-text expression
   * @param info input info
   */
  public FTContains(final Expr expr, final FTExpr ftexpr, final InputInfo info) {
    super(info, expr);
    this.ftexpr = ftexpr;
    seqType = SeqType.BLN;
    lex = new FTLexer(new FTOpt());
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = expr.iter(qc);
    final FTLexer tmp = qc.ftToken;

    qc.ftToken = lex;
    double s = 0;
    for(Item it; (it = iter.next()) != null;) {
      lex.init(it.string(info));
      final FTNode item = ftexpr.item(qc, info);
      double d = 0;
      if(item.all.matches()) {
        d = item.score();
        // no scoring found - use default value
        if(d == 0) d = 1;
      }
      s = s == 0 ? d : Scoring.merge(s, d);

      // cache entry for visualizations or ft:mark/ft:extract
      if(d > 0 && qc.ftPosData != null && it instanceof DBNode) {
        final DBNode node = (DBNode) it;
        qc.ftPosData.add(node.data, node.pre, item.all);
      }
    }
    qc.ftToken = tmp;
    return Bln.get(s);
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    ftexpr = ftexpr.compile(qc, scp);
    return expr.isEmpty() ? optPre(Bln.FALSE, qc) : this;
  }

  @Override
  public boolean has(final Flag flag) {
    return super.has(flag) || ftexpr.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return super.removable(var) && ftexpr.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return super.count(var).plus(ftexpr.count(var));
  }

  @Override
  public Expr inline(final QueryContext qc, final VarScope scp, final Var var, final Expr ex)
      throws QueryException {

    final Expr sub = expr.inline(qc, scp, var, ex);
    if(sub != null) expr = sub;
    final FTExpr fte = ftexpr.inline(qc, scp, var, ex);
    if(fte != null) ftexpr = fte;
    return sub != null || fte != null ? optimize(qc, scp) : null;
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
    // return false if step is no text node, or if no index is available
    if(!ii.check(expr, true) || !ftexpr.indexAccessible(ii)) return false;

    ii.create(new FTIndexAccess(info, ftexpr, ii.ic), info, Util.info(OPTFTXINDEX, ftexpr), true);
    return true;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTContains(expr.copy(qc, scp, vs), ftexpr.copy(qc, scp, vs), info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, ftexpr);
  }

  @Override
  public String toString() {
    return expr + " " + CONTAINS + ' ' + TEXT + ' ' + ftexpr;
  }
}
