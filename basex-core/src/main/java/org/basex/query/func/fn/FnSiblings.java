package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSiblings extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XNode node = toNodeOrNull(context(qc), qc);
    if(node == null) return Empty.ITER;

    final XNode parent = node.parent();
    if(node.kind().oneOf(Kind.ATTRIBUTE, Kind.NAMESPACE) || parent == null)
      return node.iter();

    final Iter iter = parent.childIter();
    return new NodeIter() {
      @Override
      public XNode next() throws QueryException {
        qc.checkStop();
        return (XNode) iter.next();
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    final Expr expr = defined(0) ? arg(0) : value;
    exprType.data(expr);
    return optFirst(false, false, value);
  }
}
