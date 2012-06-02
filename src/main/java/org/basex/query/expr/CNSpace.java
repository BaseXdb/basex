package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Namespace constructor.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CNSpace extends CName {
  /**
   * Constructor.
   * @param ii input info
   * @param n name
   * @param v attribute values
   */
  public CNSpace(final InputInfo ii, final Expr n, final Expr v) {
    super(NSPACE, ii, n, v);
    type = SeqType.NSP;
  }

  @Override
  public FNames item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = name.item(ctx, info);
    final byte[] cp = checkEStr(it);
    if(cp.length != 0 && !XMLToken.isNCName(cp)) INVNAME.thrw(info, expr[0]);

    final byte[] cu = trim(value(ctx, ii));
    if(eq(cp, XML) ^ eq(cu, XMLURI)) CNXML.thrw(info);
    if(eq(cp, XMLNS)) CNINV.thrw(info, cp);
    if(eq(cu, XMLNSURI) || cu.length == 0) CNINV.thrw(info, cu);

    return new FNames(cp, cu);
  }
}
