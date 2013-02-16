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
import org.basex.util.hash.*;

/**
 * FTContains expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTContainsExpr extends FTContains {

  /**
   * Constructor.
   * @param e expression
   * @param fte full-text expression
   * @param ii input info
   */
  public FTContainsExpr(final Expr e, final FTExpr fte, final InputInfo ii) {
    super(e, fte, ii);
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
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // return if step is no text node, or if no index is available
    final Step s = expr instanceof Context ? ic.step : CmpG.indexStep(expr);
    final boolean ok = s != null && ic.data.meta.ftxtindex &&
      s.test.type == NodeType.TXT && ftexpr.indexAccessible(ic);
    ic.seq |= ic.not;
    return ok;
  }

  @Override
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    ic.ctx.compInfo(OPTFTXINDEX);

    // sequential evaluation with index access
    final FTExpr ie = ftexpr.indexEquivalent(ic);
    if(ic.seq) return new FTContainsIndex(info, expr, ie, ic.not);

    // standard index evaluation; first expression will always be an axis path
    final FTIndexAccess rt = new FTIndexAccess(info, ie, ic.data.meta.name, ic.iterable);
    return expr instanceof Context ? rt : ((AxisPath) expr).invertPath(rt, ic.step);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    final FTContains ftc =  new FTContainsExpr(expr.copy(ctx, scp, vs),
        ftexpr.copy(ctx, scp, vs), info);
    if(lex != null) ftc.lex = new FTLexer(new FTOpt());
    return ftc;
  }
}
