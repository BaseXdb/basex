package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Root node.
 *
 * @author BaseX Team 2005-17, BSD License
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
  public Expr compile(final CompileContext cc) {
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    final Value v = cc.qc.focus.value;
    return v != null && v.type == NodeType.DOC && v.size() == 1 ? cc.replaceWith(this, v) : this;
  }

  @Override
  public BasicNodeIter iter(final QueryContext qc) throws QueryException {
    final Iter iter = ctxValue(qc).iter();
    final ANodeBuilder list = new ANodeBuilder();
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      final ANode n = it instanceof ANode ? ((ANode) it).root() : null;
      if(n == null || n.type != NodeType.DOC) throw CTXNODE.get(info);
      list.add(n);
    }
    return list.iter();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Root(info);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT);
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Root;
  }

  @Override
  public String toString() {
    return "root()";
  }
}
