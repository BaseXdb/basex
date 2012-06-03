package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * PI fragment.
 *
 * @author BaseX Team 2005-12, BSD License
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
    type = SeqType.PI;
  }

  @Override
  public FPI item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = checkItem(name, ctx);
    final Type ip = it.type;
    if(!ip.isUntyped() && !ip.isString() && ip != AtomType.QNM)
      CPIWRONG.thrw(info, it);

    final byte[] nm = trim(it.string(ii));
    if(eq(lc(nm), XML)) CPIXML.thrw(info, nm);
    if(!XMLToken.isNCName(nm)) CPIINVAL.thrw(info, nm);

    byte[] v = value(ctx, ii);
    int i = -1;
    while(++i != v.length && v[i] >= 0 && v[i] <= ' ');
    v = substring(v, i);
    return new FPI(new QNm(nm), FPI.parse(v, info));
  }
}
