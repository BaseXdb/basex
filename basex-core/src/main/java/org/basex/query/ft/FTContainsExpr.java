package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContains expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTContainsExpr extends FTContains {
  /**
   * Constructor.
   * @param expr expression
   * @param ftexpr full-text expression
   * @param info input info
   */
  public FTContainsExpr(final Expr expr, final FTExpr ftexpr, final InputInfo info) {
    super(expr, ftexpr, info);
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
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    // return if step is no text node, or if no index is available
    if(!ii.check(expr, true) || !ftexpr.indexAccessible(ii)) return false;

    ii.create(new FTIndexAccess(info, ftexpr, ii.ic), info, Util.info(OPTFTXINDEX, ftexpr), true);
    return true;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTContainsExpr(expr.copy(qc, scp, vs), ftexpr.copy(qc, scp, vs), info);
  }
}
