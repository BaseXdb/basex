package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Namespace constructor.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CNSpace extends CName {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param n name
   * @param v attribute values
   */
  public CNSpace(final StaticContext sctx, final InputInfo ii, final Expr n,
      final Expr v) {
    super(NSPACE, sctx, ii, n, v);
    type = SeqType.NSP;
  }

  @Override
  public FNames item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item it = name.item(ctx, info);
    final byte[] cp = checkEStr(it);
    if(cp.length != 0 && !XMLToken.isNCName(cp)) throw INVNSNAME.get(info, cp);

    final byte[] cu = trim(value(ctx, ii));
    if(eq(cp, XML) ^ eq(cu, XMLURI)) throw CNXML.get(info);
    if(eq(cp, XMLNS)) throw CNINV.get(info, cp);
    if(eq(cu, XMLNSURI) || cu.length == 0) throw CNINV.get(info, cu);

    return new FNames(cp, cu);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CNSpace(sc, info, name.copy(ctx, scp, vs), expr[0].copy(ctx, scp, vs));
  }
}
