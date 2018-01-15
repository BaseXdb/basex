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
 * @author BaseX Team 2005-18, BSD License
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
    super(sc, info, SeqType.DOC_O, expr);
  }

  @Override
  public FDoc item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // create document node and add children
    final Constr c = new Constr(info, sc);
    final FDoc doc = new FDoc(c.children, Token.EMPTY);
    c.add(qc, exprs);
    if(c.errAtt != null) throw DOCATTS_X.get(info, c.errAtt);
    if(!c.atts.isEmpty()) throw DOCATTS_X.get(info, c.atts.get(0).name());
    if(c.errNS != null) throw DOCNS_X.get(info, c.errNS);
    if(!c.nspaces.isEmpty()) throw DOCNS_X.get(info, c.nspaces.name(0));
    return doc.optimize();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CDoc(sc, info, exprs[0].copy(cc, vm));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CDoc && super.equals(obj);
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
