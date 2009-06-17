package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
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
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    return not(expr[0].atomic(ctx));
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    return new FTIter() {
      final FTIter ir = expr[0].iter(ctx);

      @Override
      public FTItem next() throws QueryException {
        return not(ir.next());
      }
    };
  }

  /**
   * Negates a hit.
   * @param it item
   * @return specified item
   */
  FTItem not(final FTItem it) {
    if(it != null) {
      it.all.not();
      // ..for negated queries without hits ('a' ftcontains ftnot 'b')
      it.score(it.score() == 0 ? 1 : 0);
    }
    return it;
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    final boolean ia = expr[0].indexAccessible(ic);
    ic.ftnot ^= true;
    ic.seq = ic.ftnot;
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
