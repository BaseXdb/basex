package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Document fragment.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CNode {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   */
  public CDoc(final StaticContext sc, final InputInfo info, final Expr expr) {
    super(sc, info, expr);
    seqType = SeqType.DOC_O;
  }

  @Override
  public FDoc item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // create document node and add children
    final Constr c = new Constr(ii, sc);
    final FDoc doc = new FDoc(c.children, Token.EMPTY);
    c.add(qc, exprs);
    if(c.errAtt != null) throw DOCATTS_X.get(ii, c.errAtt);
    if(!c.atts.isEmpty()) throw DOCATTS_X.get(ii, c.atts.get(0).name());
    if(c.errNS != null) throw DOCNS_X.get(ii, c.errNS);
    if(!c.nspaces.isEmpty()) throw DOCNS_X.get(ii, c.nspaces.name(0));
    return doc.optimize();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CDoc(sc, info, exprs[0].copy(qc, scp, vs));
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
