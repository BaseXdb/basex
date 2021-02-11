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
 * FTOr expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTOr extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public FTOr(final InputInfo info, final FTExpr[] exprs) {
    super(info, exprs);
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    super.compile(cc);
    boolean not = true;
    for(final FTExpr expr : exprs) not &= expr instanceof FTNot;
    if(not) {
      // convert (!A or !B or ...) to !(A and B and ...)
      final int el = exprs.length;
      for(int e = 0; e < el; e++) exprs[e] = exprs[e].exprs[0];
      return (FTExpr) cc.replaceWith(this, new FTNot(info, new FTAnd(info, exprs)));
    }
    return this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FTNode item = exprs[0].item(qc, info);
    final int el = exprs.length;
    for(int e = 1; e < el; e++) or(item, exprs[e].item(qc, info));
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
        // find item with smallest pre value
        int p = -1;
        for(int e = 0; e < el; ++e) {
          if(nodes[e] != null && (p == -1 || nodes[p].pre() > nodes[e].pre())) p = e;
        }
        // no items left - leave
        if(p == -1) return null;

        // merge all matches
        final FTNode item = nodes[p];
        for(int e = 0; e < el; ++e) {
          if(nodes[e] != null && p != e && item.pre() == nodes[e].pre()) {
            or(item, nodes[e]);
            nodes[e] = iters[e].next();
          }
        }
        nodes[p] = iters[p].next();
        return item;
      }
    };
  }

  /**
   * Merges two matches.
   * @param i1 first item
   * @param i2 second item
   */
  private static void or(final FTNode i1, final FTNode i2) {
    final FTMatches all1 = i1.matches(), all2 = i2.matches();
    final FTMatches all = new FTMatches((byte) Math.max(all1.pos, all2.pos));
    for(final FTMatch m : all1) all.add(m);
    for(final FTMatch m : all2) all.add(m);
    i1.score(Scoring.avg(i1.score() + i2.score(), 2));
    i1.matches(all);
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
    return copyType(new FTOr(info, Arr.copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTOr && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, ' ' + FTOR + ' ', true);
  }
}
