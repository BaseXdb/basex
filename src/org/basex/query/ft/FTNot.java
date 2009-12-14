package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.data.FTMatch;
import org.basex.data.FTMatches;
import org.basex.data.FTStringMatch;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * FTUnaryNot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FTNot extends FTExpr {
  /**
   * Constructor.
   * @param e expression
   */
  public FTNot(final FTExpr e) {
    super(e);
  }

  @Override
  public FTExpr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return expr[0] instanceof FTNot ? expr[0].expr[0] : this;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    return not(ctx, expr[0].atomic(ctx));
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter ir = expr[0].iter(ctx);

      @Override
      public FTItem next() throws QueryException {
        return not(ctx, ir.next());
      }
    };
  }

  /**
   * Negates a hit.
   * @param ctx query context
   * @param item item
   * @return specified item
   */
  FTItem not(final QueryContext ctx, final FTItem item) {
    if(item != null) {
      item.all = not(item.all);
      item.score(ctx.score.not(item.score()));
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
        s.n ^= true;
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
    return FTNOT + " " + expr[0];
  }
}
