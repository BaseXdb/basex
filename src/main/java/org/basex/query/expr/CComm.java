package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FComm;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Comment fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    Item it;
    while((it = iter.next()) != null) {
      if(more) tb.add(' ');
      tb.add(it.atom());
      more = true;
    }
    return new FComm(FComm.parse(tb.finish(), input), null);
  }

  @Override
  public String desc() {
    return info(QueryTokens.COMMENT);
  }

  @Override
  public String toString() {
    return toString(Type.COM.name);
  }
}
