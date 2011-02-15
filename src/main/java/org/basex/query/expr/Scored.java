package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;

/**
 * Scored expression. This is a proprietary extension to XQuery FT introduced by
 * Stefan Klinger.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class Scored extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e scored expression
   * @param s expression computing the score
   */
  public Scored(final InputInfo ii, final Expr e, final Expr s) {
    super(ii, e, s);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    // binds a score value to the operand
    final Item it = expr[0].item(ctx, input);
    it.score(checkDbl(expr[1], ctx));
    return it;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return toString(SCORED);
  }
}
