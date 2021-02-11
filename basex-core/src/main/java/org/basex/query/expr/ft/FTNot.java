package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTUnaryNot expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTNot extends FTExpr {
  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   */
  public FTNot(final InputInfo info, final FTExpr expr) {
    super(info, expr);
  }

  @Override
  public FTExpr compile(final CompileContext cc) throws QueryException {
    super.compile(cc);
    return exprs[0] instanceof FTNot ? (FTExpr) cc.replaceWith(this, exprs[0].exprs[0]) : this;
  }

  @Override
  public FTNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return not(exprs[0].item(qc, info));
  }

  @Override
  public FTIter iter(final QueryContext qc) throws QueryException {
    return new FTIter() {
      final FTIter iter = exprs[0].iter(qc);
      @Override
      public FTNode next() throws QueryException {
        return not(iter.next());
      }
    };
  }

  /**
   * Negates a hit.
   * @param item item
   * @return specified item
   */
  private static FTNode not(final FTNode item) {
    if(item != null) {
      item.matches(not(item.matches()));
      item.score(Scoring.not(item.score()));
    }
    return item;
  }

  /**
   * Negates the specified matches.
   * @param m match
   * @return resulting matches
   */
  static FTMatches not(final FTMatches m) {
    return not(m, 0);
  }

  /**
   * Negates the specified matches.
   * @param m match
   * @param i position to start from
   * @return resulting match
   */
  private static FTMatches not(final FTMatches m, final int i) {
    final FTMatches all = new FTMatches(m.pos);
    if(i == m.size()) {
      all.add(new FTMatch());
    } else {
      for(final FTStringMatch s : m.list[i]) {
        s.exclude ^= true;
        for(final FTMatch tmp : not(m, i + 1)) {
          all.add(new FTMatch(1 + tmp.size()).add(s).add(tmp));
        }
      }
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexInfo ii) {
    return false;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTNot(info, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean usesExclude() {
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTNot && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(FTNOT).token(exprs[0]);
  }
}
