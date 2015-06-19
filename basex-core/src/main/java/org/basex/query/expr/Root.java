package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Root node.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Root extends Simple {
  /**
   * Constructor.
   * @param info input info
   */
  public Root(final InputInfo info) {
    super(info);
    seqType = SeqType.DOC_ZM;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) {
    return qc.value != null && qc.value.type == NodeType.DOC ? qc.value : this;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final Iter iter = ctxValue(qc).iter();
    final ANodeList list = new ANodeList().check();
    for(Item it; (it = iter.next()) != null;) {
      final ANode n = it instanceof ANode ? ((ANode) it).root() : null;
      if(n == null || n.type != NodeType.DOC) throw CTXNODE.get(info);
      list.add(n);
    }
    return list.iter();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Root(info);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(DBLocking.CONTEXT);
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Root;
  }

  @Override
  public String toString() {
    return "root()";
  }
}
