package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FComm;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Comment fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CComm extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param c comment
   */
  public CComm(final InputInfo ii, final Expr c) {
    super(ii, c);
  }

  @Override
  public FComm item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final Iter iter = ctx.iter(expr[0]);

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    for(Item it; (it = iter.next()) != null;) {
      if(more) tb.add(' ');
      tb.add(it.string(ii));
      more = true;
    }
    return new FComm(FComm.parse(tb.finish(), input));
  }

  @Override
  public String description() {
    return info(COMMENT);
  }

  @Override
  public String toString() {
    return toString(COMMENT);
  }
}
