package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FPI;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.Token;
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
   * @param n name
   * @param v value
   */
  public CPI(final Expr n, final Expr v) {
    super(n, v);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    if(checkUp(expr[0], ctx).e()) Err.empty(this);
    return this;
  }

  @Override
  public FPI atomic(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].atomic(ctx);
    if(it == null) Err.empty(this);
    if(!it.u() && !it.s() && it.type != Type.QNM) Err.or(CPIWRONG, it.type, it);

    final byte[] nm = Token.trim(it.str());
    if(Token.eq(Token.lc(nm), Token.XML)) Err.or(CPIXML, nm);
    if(!XMLToken.isNCName(nm)) Err.or(CPIINVAL, nm);

    final Iter iter = ctx.iter(expr[1]);
    final TokenBuilder tb = new TokenBuilder();
    CText.add(tb, iter);
    byte[] v = tb.finish();

    int i = -1;
    while(++i != v.length && v[i] >= 0 && v[i] <= ' ');
    v = Token.substring(v, i);
    return new FPI(new QNm(nm), check(v), null);
  }

  /**
   * Checks the specified token for validity.
   * @param atom token to be checked
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] check(final byte[] atom) throws QueryException {
    if(contains(atom, CLOSE)) Err.or(CPICONT, atom);
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
