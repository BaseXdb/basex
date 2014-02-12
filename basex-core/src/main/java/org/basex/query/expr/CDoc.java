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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CNode {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param e expression
   */
  public CDoc(final StaticContext sctx, final InputInfo ii, final Expr e) {
    super(sctx, ii, e);
    type = SeqType.DOC_O;
  }

  @Override
  public FDoc item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    // create node
    final Constr c = new Constr(ii, sc);
    final FDoc doc = new FDoc(c.children, Token.EMPTY);

    // add child nodes
    c.add(ctx, expr);
    if(c.errAtt || !c.atts.isEmpty()) throw DOCATTS.get(ii);
    if(c.errNS || !c.nspaces.isEmpty()) throw DOCNS.get(ii);
    return doc.optimize();
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CDoc(sc, info, expr[0].copy(ctx, scp, vs));
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
