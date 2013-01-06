package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

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
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return allAreValues() ? preEval(ctx) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    Map map = Map.EMPTY;
    for(int i = 0; i < expr.length; i++) {
      map = map.insert(checkItem(expr[i], ctx), ctx.value(expr[++i]), ii);
    }
    return map;
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
