package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class FnRoot extends ContextFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode node = toGNodeOrNull(contextArg(qc), qc);
    return node == null ? Empty.VALUE : node.root();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    final Expr expr = defined(0) ? arg(0) : value;
    if(expr != null) {
      // root($document) → $document, root(<a/>) → <a/>, <a/>/root() → <a/>/.
      final SeqType st = expr.seqType();
      if(st.zeroOrOne() && (st.type instanceof final NodeType nt && nt.root() ||
          expr.parentless())) {
        return defined(0) ? expr : ContextValue.get(cc, info);
      }
    }
    exprType.data(expr);
    return optFirst(true, false, value);
  }

  @Override
  public boolean parentless() {
    return true;
  }
}
