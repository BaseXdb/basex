package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Text fragment.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CTxt extends CNode {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param t text
   */
  public CTxt(final StaticContext sctx, final InputInfo ii, final Expr t) {
    super(sctx, ii, t);
    type = SeqType.TXT_ZO;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optPre(oneIsEmpty() ? null : this, ctx);
  }

  @Override
  public FTxt item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    Item it = iter.next();
    if(it == null) return null;

    final TokenBuilder tb = new TokenBuilder();
    boolean more = false;
    do {
      if(more) tb.add(' ');
      tb.add(it.string(ii));
      more = true;
    } while((it = iter.next()) != null);

    return new FTxt(tb.finish());
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CTxt(sc, info, expr[0].copy(ctx, scp, vs));
  }

  @Override
  public String description() {
    return info(TEXT);
  }

  @Override
  public String toString() {
    return toString(TEXT);
  }
}
