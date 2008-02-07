package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FTxt;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.TokenBuilder;

/**
 * Text fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CText extends Single {
  /**
   * Constructor.
   * @param t text
   */
  public CText(final Expr t) {
    super(t);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr);
    Item it = iter.next();
    if(it == null) return Iter.EMPTY;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    } while((it = iter.next()) != null);
    return new FTxt(tb.finish(), null).iter();
  }

  /**
   * Adds a single item to the token builder.
   * @param tb token builder
   * @param iter iterator
   * @throws XQException query exception
   */
  public static void add(final TokenBuilder tb, final Iter iter)
      throws XQException {
    boolean more = false;
    Item it = null;
    while((it = iter.next()) != null) {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    }
  }
  
  @Override
  public String toString() {
    return "text { " + expr + " }";
  }

  @Override
  public String info() {
    return "Text constructor";
  }

  @Override
  public String color() {
    return "FF3333";
  }
}
