package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Document fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public CDoc(final InputInfo ii, final Expr e) {
    super(ii, e);
  }

  @Override
  public FDoc item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, ctx).add(expr);
    if(c.errAtt || c.atts.size() != 0) XPATT.thrw(ii);
    if(c.errNS || c.nspaces.size() != 0) XPNS.thrw(ii);
    return new FDoc(c.children, Token.EMPTY);
  }

  @Override
  public String description() {
    return info(DOCUMENT);
  }

  @Override
  public String toString() {
    return toString(DOCUMENT);
  }
}
