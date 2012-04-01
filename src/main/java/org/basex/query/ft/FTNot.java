package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.data.FTStringMatch;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTNode;
import org.basex.query.iter.FTIter;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.ft.Scoring;

/**
 * FTUnaryNot expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTNot extends FTExpr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public FTNot(final InputInfo ii, final FTExpr e) {
    super(ii, e);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return expr[0] instanceof FTNot ? expr[0].expr[0] : this;
  }

  @Override
  public FTNode item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return not(expr[0].item(ctx, info));
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter ir = expr[0].iter(ctx);

      @Override
      public FTNode next() throws QueryException {
        return not(ir.next());
      }
    };
  }

  /**
   * Negates a hit.
   * @param item item
   * @return specified item
   */
  static FTNode not(final FTNode item) {
    if(item != null) {
      item.all = not(item.all);
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
    final FTMatches all = new FTMatches(m.sTokenNum);
    if(i == m.size) {
      all.add(new FTMatch());
    } else {
      for(final FTStringMatch s : m.match[i]) {
        s.ex ^= true;
        for(final FTMatch tmp : not(m, i + 1)) {
          all.add(new FTMatch().add(s).add(tmp));
        }
      }
    }
    return all;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    final boolean ia = expr[0].indexAccessible(ic);
    ic.not ^= true;
    return ia;
  }

  @Override
  public boolean usesExclude() {
    return true;
  }

  @Override
  public String toString() {
    return FTNOT + ' ' + expr[0];
  }
}
