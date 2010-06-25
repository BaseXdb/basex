package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.FComm;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;

/**
 * Comment fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class CComm extends CFrag {
  /** Two dashes, marking the start/end of a comment. */
  private static final byte[] DASHES = { '-', '-' };

  /**
   * Constructor.
   * @param c comment
   */
  public CComm(final Expr c) {
    super(c);
  }

  @Override
  public FComm atomic(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    Item it;
    while((it = iter.next()) != null) {
      if(more) tb.add(' ');
      tb.add(it.str());
      more = true;
    }
    return new FComm(check(tb.finish()), null);
  }

  /**
   * Checks the specified token for validity.
   * @param atom token to be checked
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] check(final byte[] atom) throws QueryException {
    if(contains(atom, DASHES) || endsWith(atom, '-')) Err.or(COMINVALID, atom);
    return atom;
  }

  @Override
  public String info() {
    return info(QueryTokens.COMMENT);
  }

  @Override
  public String toString() {
    return toString(Type.COM.name);
  }
}
