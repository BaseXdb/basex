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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class CDoc extends CNode {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param computed computed constructor
   * @param expr expression
   */
  public CDoc(final StaticContext sc, final InputInfo info, final boolean computed,
      final Expr expr) {
    super(sc, info, SeqType.DOCUMENT_NODE_O, computed, expr);
  }

  @Override
  public FDoc item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FDoc doc = new FDoc();

    final Constr constr = new Constr(info, sc, qc).add(exprs);
    if(constr.errAtt != null) throw DOCATTS_X.get(info, constr.errAtt);
    if(constr.errNS != null) throw DOCNS_X.get(info, constr.errNS);
    final Atts ns = constr.builder.namespaces;
    if(ns != null) throw DOCNS_X.get(info, ns.name(0));
    final ANodeList attributes = constr.builder.attributes;
    if(attributes != null) throw DOCATTS_X.get(info, attributes.get(0).name());

    return doc.finish(constr.builder);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CDoc(sc, info, computed, exprs[0].copy(cc, vm)));
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
