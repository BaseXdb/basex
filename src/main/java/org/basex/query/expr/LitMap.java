package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.map.Map;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * A literal map expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class LitMap extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e key and value expression, interleaved
   */
  public LitMap(final InputInfo ii, final Expr[] e) {
    super(ii, e);
    type = SeqType.MAP_O;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    Map map = Map.EMPTY;
    for(int i = 0; i < expr.length; i++)
      map = map.insert(checkItem(expr[i], ctx), expr[++i].value(ctx), ii);
    return map;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return allAreValues() ? preEval(ctx) : this;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("map { ");
    boolean key = true;
    for(final Expr e : expr) {
      tb.add(key ? tb.size() > 6 ? ", " : "" : ":=").add(e.toString());
      key ^= true;
    }
    return tb.add(" }").toString();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }

  @Override
  public String description() {
    return QueryText.MAPSTR;
  }
}
