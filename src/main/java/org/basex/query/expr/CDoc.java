package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Document fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CFrag {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   */
  public CDoc(final InputInfo ii, final Expr e) {
    super(ii, e);
    type = SeqType.DOC_O;
  }

  @Override
  public FDoc item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, ctx).add(expr);
    if(c.errAtt || c.atts.size() != 0) DOCATTS.thrw(ii);
    if(c.errNS || c.nspaces.size() != 0) DOCNS.thrw(ii);
    return new FDoc(c.children, Token.EMPTY);
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CDoc(info, expr[0].copy(ctx, scp, vs));
  }

  @Override
  public String description() {
    return info(DOCUMENT);
  }

  @Override
  public String toString() {
    return toString(DOCUMENT);
  }
}
