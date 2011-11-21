package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Text fragment.
 *
 * @author BaseX Team 2005-11, BSD License
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
    type = SeqType.NOD_ZO;
  }

  @Override
  public FTxt item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

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
  public String desc() {
    return info(QueryText.TEXT);
  }

  @Override
  public String toString() {
    return toString(Token.string(NodeType.TXT.string()));
  }
}
