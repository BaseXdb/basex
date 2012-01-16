package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FNames;
import org.basex.query.item.Item;
import org.basex.util.InputInfo;
import org.basex.util.XMLToken;

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
  }

  @Override
  public FNames item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item it = name.item(ctx, input);
    final byte[] cp = checkEStr(it);
    if(cp.length != 0 && !XMLToken.isNCName(cp)) INVNAME.thrw(input, expr[0]);

    final byte[] cu = trim(value(ctx, ii));
    if(eq(cp, XML) ^ eq(cu, XMLURI)) CNXML.thrw(input);
    if(eq(cp, XMLNS)) CNINV.thrw(input, cp);
    if(eq(cu, XMLNSURI) || cu.length == 0) CNINV.thrw(input, cu);

    return new FNames(cp, cu);
  }
}
