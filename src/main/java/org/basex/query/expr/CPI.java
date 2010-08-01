package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryInfo;
import org.basex.query.QueryTokens;
import org.basex.query.item.FPI;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * PI fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CPI extends CFrag {
  /** Closing processing instruction. */
  private static final byte[] CLOSE = { '?', '>' };

  /**
   * Constructor.
   * @param i query info
   * @param n name
   * @param v value
   */
  public CPI(final QueryInfo i, final Expr n, final Expr v) {
    super(i, n, v);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    checkUp(expr[0], ctx);
    return this;
  }

  @Override
  public FPI atomic(final QueryContext ctx) throws QueryException {
    final Item it = checkEmpty(expr[0], ctx);
    if(!it.unt() && !it.str() && it.type != Type.QNM)
      error(CPIWRONG, it.type, it);

    final byte[] nm = trim(it.atom());
    if(eq(lc(nm), XML)) error(CPIXML, nm);
    if(!XMLToken.isNCName(nm)) error(CPIINVAL, nm);

    final Iter iter = ctx.iter(expr[1]);
    final TokenBuilder tb = new TokenBuilder();
    CText.add(tb, iter);
    byte[] v = tb.finish();

    int i = -1;
    while(++i != v.length && v[i] >= 0 && v[i] <= ' ');
    v = substring(v, i);
    return new FPI(new QNm(nm), check(this, v), null);
  }

  /**
   * Checks the specified token for validity.
   * @param e calling expression
   * @param atom token to be checked
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] check(final ParseExpr e, final byte[] atom)
      throws QueryException {

    if(contains(atom, CLOSE)) e.error(CPICONT, atom);
    return atom;
  }

  @Override
  public String info() {
    return info(QueryTokens.PI);
  }

  @Override
  public String toString() {
    return toString(Type.PI.name);
  }
}
