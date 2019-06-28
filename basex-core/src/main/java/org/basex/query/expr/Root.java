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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Root extends Simple {
  /**
   * Constructor.
   * @param info input info
   */
  public Root(final InputInfo info) {
    super(info, SeqType.DOC_ZM);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Value value = cc.qc.focus.value;
    if(cc.nestedFocus()) {
      exprType.assign(Occ.ONE);
    } else {
      if(value != null) return cc.replaceWith(this, value(cc.qc));
    }
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = ctxValue(qc);
    if(value.seqType().type == NodeType.DOC) return value;

    final Iter iter = value.iter();
    final ANodeBuilder list = new ANodeBuilder();
    for(Item item; (item = qc.next(iter)) != null;) {
      final ANode node = item instanceof ANode ? ((ANode) item).root() : null;
      if(node == null || node.type != NodeType.DOC) throw CTXNODE.get(info);
      list.add(node);
    }
    return list.value(this);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Root(info);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.CTX.in(flags);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return var != null ? null : ex.seqType().instanceOf(SeqType.DOC_ZO) ? ex :
      SimpleMap.get(info, ex, this);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT, false);
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
