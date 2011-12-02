package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FPI;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.AtomType;
import org.basex.query.item.Type;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;

/**
 * PI fragment.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CPI extends CName {
  /**
   * Constructor.
   * @param ii input info
   * @param n name
   * @param v value
   */
  public CPI(final InputInfo ii, final Expr n, final Expr v) {
    super(PI, ii, n, v);
  }

  @Override
  public FPI item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = checkItem(name, ctx);
    final Type ip = it.type;
    if(!ip.isUntyped() && !ip.isString() && ip != AtomType.QNM)
      CPIWRONG.thrw(input, it);

    final byte[] nm = trim(it.string(ii));
    if(eq(lc(nm), XML)) CPIXML.thrw(input, nm);
    if(!XMLToken.isNCName(nm)) CPIINVAL.thrw(input, nm);

    byte[] v = value(ctx, ii);
    int i = -1;
    while(++i != v.length && v[i] >= 0 && v[i] <= ' ');
    v = substring(v, i);
    return new FPI(new QNm(nm), FPI.parse(v, input));
  }
}
