package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJtree extends StandardFunc {
  @Override
  public JNode value(final QueryContext qc) throws QueryException {
    return new JNode(arg(0).value(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // jtree(array { ... }) → jnode((), array(...))
    final SeqType st = arg(0).seqType();
    if(!st.eq(Types.ITEM_ZM)) exprType.assign(NodeType.get(Empty.VALUE, st));
    return this;
  }
}
