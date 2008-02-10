package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FPI;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * PI fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CPI extends Arr {
  /** Closing processing instruction. */
  private static final byte[] CLOSE = { '?', '>' };

  /**
   * Constructor.
   * @param n name
   * @param v value
   */
  public CPI(final Expr n, final Expr v) {
    super(n, v);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    Item it = ctx.iter(expr[0]).atomic(this, false);
    if(!it.u() && !it.s() && it.type != Type.QNM) Err.or(CPIWRONG, it.type, it);

    final byte[] nm = Token.trim(it.str());
    if(Token.eq(Token.lc(nm), XML)) Err.or(CPIXML, nm);
    if(!XMLToken.isNCName(nm)) Err.or(CPIINVAL, nm);

    final Iter iter = ctx.iter(expr[1]);
    final TokenBuilder tb = new TokenBuilder();
    CText.add(tb, iter);
    byte[] v = tb.finish();
    
    int i = -1;
    while(++i != v.length && v[i] >= 0 && v[i] <= ' ');
    v = Token.substring(v, i);
    if(Token.contains(v, CLOSE)) Err.or(CPICONT, v);

    return new FPI(new QNm(nm), v, null).iter();
  }

  @Override
  public String toString() {
    return "<?" + expr[0] + ' ' + expr[1] + "?>";
  }

  @Override
  public String info() {
    return "PI constructor";
  }

  @Override
  public String color() {
    return "FF3333";
  }
}
