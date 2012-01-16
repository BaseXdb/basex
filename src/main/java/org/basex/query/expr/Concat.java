package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.AtomType;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * StringConcat expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Concat extends Arr {
  /**
   * Constructor.
   * @param ii input info
   * @param e expressions to be concatenated
   */
  public Concat(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(allAreValues()) return optPre(item(ctx, input), ctx);
    type = AtomType.STR.seq();
    size = 1;
    return this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr a : expr) {
      final Item it = a.item(ctx, input);
      if(it != null) tb.add(it.string(input));
    }
    return Str.get(tb.finish());
  }

  @Override
  public String toString() {
    return toString(" || ");
  }
}
