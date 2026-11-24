package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Document constructor.
 *
 * @author BaseX Team, BSD License
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
    super(info, Types.DOCUMENT_O, computed, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    // document { <!--x-->, <a/> } → document-node(element(a))
    final Expr expr = exprs[0];
    Test child = null;
    for(final Expr ex : expr instanceof List ? expr.args() : new Expr[] { expr }) {
      final SeqType st = ex.seqType();
      if(!(st.type instanceof final NodeType nt)) return this;
      final Kind kind = nt.kind();
      if(kind.oneOf(Kind.COMMENT, Kind.PROCESSING_INSTRUCTION)) continue;
      if(child != null || !st.one()) return this;
      if(kind == Kind.ELEMENT) child = nt.test != null ? nt.test : NodeTest.ELEMENT;
      else if(nt.test instanceof final DocTest dt) child = dt.child();
      else return this;
    }
    if(child != null) exprType.assign(NodeType.get(new DocTest(child)));
    return this;
  }

  @Override
  public Occ childOcc(final Test test, final boolean attributes) {
    return contentOcc(test, attributes);
  }

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final FBuilder doc = FDoc.build(sc().baseURI().string(), false);

    final Constr constr = new Constr(doc, skipCopy, info, qc).add(exprs);
    if(constr.errAtt != null) throw DOCATTS_X.get(info, constr.errAtt);
    if(constr.errNS != null) throw DOCNS_X.get(info, constr.errNS);
    final Atts ns = doc.namespaces;
    if(ns != null) throw DOCNS_X.get(info, ns.name(0));
    final GNodeList attributes = doc.attributes;
    if(attributes != null) throw DOCATTS_X.get(info, attributes.get(0).name());

    return doc.finish();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
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
