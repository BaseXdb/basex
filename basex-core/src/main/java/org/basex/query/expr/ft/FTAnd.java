package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTAnd expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTAnd extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public FTAnd(final InputInfo info, final FTExpr[] exprs) {
    super(info, exprs);
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    super.compile(cc);
    boolean not = true;
    for(final FTExpr expr : exprs) not &= expr instanceof FTNot;
    if(not) {
      // convert (!A and !B and ...) to !(A or B or ...)
      final int el = exprs.length;
      for(int e = 0; e < el; e++) exprs[e] = exprs[e].exprs[0];
      return (FTExpr) cc.replaceWith(this, new FTNot(info, new FTOr(info, exprs)));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTNode item = exprs[0].item(qc, info);
    final int el = exprs.length;
    for(int e = 1; e < el; e++) and(item, exprs[e].item(qc, info));
    return item;
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    // initialize iterators
    final int el = exprs.length;
    final FTIter[] iters = new FTIter[el];
    final FTNode[] nodes = new FTNode[el];
    for(int e = 0; e < el; e++) {
      iters[e] = exprs[e].iter(qc);
      nodes[e] = iters[e].next();
    }

    return new FTIter() {
      @Override
      public FTNode next() throws QueryException {
        // find item with lowest pre value
        final int il = nodes.length;
        for(int i = 0; i < il; ++i) {
          if(nodes[i] == null) return null;

          final int d = nodes[0].pre() - nodes[i].pre();
          if(d != 0) {
            if(d < 0) i = 0;
            nodes[i] = iters[i].next();
            i = -1;
          }
        }

        // merge all matches
        final FTNode item = nodes[0];
        for(int i = 1; i < il; ++i) {
          and(item, nodes[i]);
          nodes[i] = iters[i].next();
        }
        nodes[0] = iters[0].next();
        return item;
      }
    };
  }

  /**
   * Merges two matches.
   * @param node1 first node
   * @param node2 second node
   */
  private static void and(final FTNode node1, final FTNode node2) {
    final FTMatches all = new FTMatches((byte) Math.max(node1.matches().pos, node2.matches().pos));
    for(final FTMatch match1 : node1.matches()) {
      for(final FTMatch match2 : node2.matches()) {
        all.add(new FTMatch(match1.size() + match2.size()).add(match1).add(match2));
      }
    }
    node1.score(Scoring.avg(node1.score() + node2.score(), 2));
    node1.matches(all);
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    IndexCosts costs = IndexCosts.ZERO;
    for(final FTExpr expr : exprs) {
      if(!expr.indexAccessible(ii)) return false;
      costs = IndexCosts.add(costs, ii.costs);
    }
    ii.costs = costs;
    return true;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTAnd(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTAnd && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + FTAND + ' ', true);
  }
}
