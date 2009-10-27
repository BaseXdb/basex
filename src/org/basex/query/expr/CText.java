package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.TokenBuilder;

/**
 * Text fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CText extends CFrag {
  /**
   * Constructor.
   * @param t text
   */
  public CText(final Expr t) {
    super(t);
  }

  @Override
  public FTxt atomic(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    Item it = iter.next();
    if(it == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    } while((it = iter.next()) != null);
    return new FTxt(tb.finish(), null);
  }

  /**
   * Adds a single item to the token builder.
   * @param tb token builder
   * @param iter iterator
   * @throws QueryException query exception
   */
  static void add(final TokenBuilder tb, final Iter iter)
      throws QueryException {
    boolean more = false;
    Item it = null;
    while((it = iter.next()) != null) {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    }
  }

  @Override
  public String info() {
    return "text constructor";
  }

  @Override
  public String toString() {
    return toString("text");
  }
}
