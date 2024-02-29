package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Document constructor.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CNode {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param computed computed constructor
   * @param expr expression
   */
  public CDoc(final InputInfo info, final boolean computed, final Expr expr) {
    super(info, SeqType.DOCUMENT_NODE_O, computed, expr);
  }

  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FBuilder doc = FDoc.build();

    final Constr constr = new Constr(doc, info, qc).add(exprs);
    if(constr.errAtt != null) throw DOCATTS_X.get(info, constr.errAtt);
    if(constr.errNS != null) throw DOCNS_X.get(info, constr.errNS);
    final Atts ns = doc.namespaces;
    if(ns != null) throw DOCNS_X.get(info, ns.name(0));
    final ANodeList attributes = doc.attributes;
    if(attributes != null) throw DOCATTS_X.get(info, attributes.get(0).name());

    return doc.finish();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CDoc(info, computed, exprs[0].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CDoc && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    toString(qs, DOCUMENT);
  }
}
