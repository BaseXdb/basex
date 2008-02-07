package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.FComm;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Comment fragment.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CComm extends Single {
  /** Two Dashes. */
  private static final byte[] DASHES = { '-', '-' };
  
  /**
   * Constructor.
   * @param c comment
   */
  public CComm(final Expr c) {
    super(c);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr);

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    Item it;
    while((it = iter.next()) != null) {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    }
    final byte[] atom = tb.finish();
    if(Token.contains(atom, DASHES) || Token.startsWith(atom, '-') ||
        Token.endsWith(atom, '-')) Err.or(COMINVALID, atom);

    return new FComm(atom, null).iter();
  }

  @Override
  public String toString() {
    return "comment {" + expr + "}";
  }
  
  @Override
  public String info() {
    return "Comment constructor";
  }

  @Override
  public String color() {
    return "FF3333";
  }
}
