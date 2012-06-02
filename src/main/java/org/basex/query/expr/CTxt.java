package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Text fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CTxt extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param t text
   */
  public CTxt(final InputInfo ii, final Expr t) {
    super(ii, t);
    type = SeqType.TXT_ZO;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    return optPre(oneIsEmpty() ? null : this, ctx);
  }

  @Override
  public FTxt item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    Item it = iter.next();
    if(it == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.string(ii));
      more = true;
    } while((it = iter.next()) != null);

    return new FTxt(tb.finish());
  }

  @Override
  public String description() {
    return info(TEXT);
  }

  @Override
  public String toString() {
    return toString(TEXT);
  }
}
