package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnSiblings extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final ANode node = toNodeOrNull(context(qc), qc);
    if(node != null) {
      if(node.type.oneOf(NodeType.ATTRIBUTE, NodeType.NAMESPACE_NODE)) return node.iter();

      final ANode parent = node.parent();
      if(parent != null) {
        final Iter iter = parent.childIter();
        return new NodeIter() {
          @Override
          public ANode next() throws QueryException {
            qc.checkStop();
            final Item next = iter.next();
            return next != null ? ((ANode) next).finish() : null;
          }
        };
      }
    }
    return Empty.ITER;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    final Expr expr = defined(0) ? arg(0) : value;
    exprType.data(expr);
    return optFirst(false, false, value);
  }
}
