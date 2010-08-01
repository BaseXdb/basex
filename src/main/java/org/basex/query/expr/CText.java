package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.QueryTokens;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.TokenBuilder;

/**
 * Text fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CText extends CFrag {
  /**
   * Constructor.
   * @param i query info
   * @param t text
   */
  public CText(final QueryInfo i, final Expr t) {
    super(i, t);
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
      tb.add(it.atom());
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
      tb.add(it.atom());
      more = true;
    }
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.NOD_ZO;
  }

  @Override
  public String info() {
    return info(QueryTokens.TEXT);
  }

  @Override
  public String toString() {
    return toString(Type.TXT.name);
  }
}
