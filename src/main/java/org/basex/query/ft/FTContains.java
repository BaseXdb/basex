package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * FTContains expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class FTContains extends ParseExpr {
  /** Expression. */
  Expr expr;
  /** Full-text expression. */
  FTExpr ftexpr;
  /** Full-text parser. */
  FTLexer lex;

  /**
   * Constructor.
   * @param e expression
   * @param fte full-text expression
   * @param ii input info
   */
  public FTContains(final Expr e, final FTExpr fte, final InputInfo ii) {
    super(ii);
    expr = e;
    ftexpr = fte;
    type = SeqType.BLN;
  }

  @Override
  public final void checkUp() throws QueryException {
    checkNoUp(expr);
  }

  @Override
  public final Expr compile(final QueryContext ctx, final VarScope scp)
      throws QueryException {
    expr = expr.compile(ctx, scp).addText(ctx);
    ftexpr = ftexpr.compile(ctx, scp);
    if(lex == null) lex = new FTLexer(new FTOpt());
    return expr.isEmpty() ? optPre(Bln.FALSE, ctx) : this;
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter iter = expr.iter(ctx);
    final FTLexer tmp = ctx.fttoken;
    double s = 0;

    ctx.fttoken = lex;
    for(Item it; (it = iter.next()) != null;) {
      lex.init(it.string(info));
      final FTNode item = ftexpr.item(ctx, info);
      double d = 0;
      if(item.all.matches()) {
        d = item.score();
        // no scoring found - use default value
        if(d == 0) d = 1;
      }
      s = Scoring.and(s, d);

      // add entry to visualization
      if(d > 0 && ctx.ftpos != null && it instanceof DBNode) {
        final DBNode node = (DBNode) it;
        ctx.ftpos.add(node.data, node.pre, item.all);
      }
    }

    ctx.fttoken = tmp;
    return Bln.get(s);
  }

  @Override
  public final boolean indexAccessible(final IndexContext ic) throws QueryException {
    // return if step is no text node, or if no index is available
    final AxisStep s = expr instanceof Context ? ic.step : CmpG.indexStep(expr);
    final boolean ok = s != null && ic.data.meta.ftxtindex &&
      s.test.type == NodeType.TXT && ftexpr.indexAccessible(ic);
    ic.seq |= ic.not;
    return ok;
  }

  @Override
  public final Expr indexEquivalent(final IndexContext ic) throws QueryException {
    ic.ctx.compInfo(OPTFTXINDEX);

    // sequential evaluation with index access
    final FTExpr ie = ftexpr.indexEquivalent(ic);
    if(ic.seq) return new FTContainsIndex(info, expr, ie, ic);

    // standard index evaluation; first expression will always be an axis path
    final FTIndexAccess root = new FTIndexAccess(info, ie, ic);
    return expr instanceof Context ? root :
      ((AxisPath) expr).invertPath(root, ic.step);
  }

  @Override
  public final boolean uses(final Use u) {
    return expr.uses(u) || ftexpr.uses(u);
  }

  @Override
  public final boolean removable(final Var v) {
    return expr.removable(v) && ftexpr.removable(v);
  }

  @Override
  public final Expr remove(final Var v) {
    expr = expr.remove(v);
    ftexpr = ftexpr.remove(v);
    return this;
  }

  @Override
  public boolean databases(final StringList db) {
    return expr.databases(db) && ftexpr.databases(db);
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(), expr, ftexpr);
  }

  @Override
  public String toString() {
    return expr + " " + CONTAINS + ' ' + TEXT + ' ' + ftexpr;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return expr.visitVars(visitor) && ftexpr.visitVars(visitor);
  }
}
