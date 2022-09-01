package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnRoot extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNodeOrNull(ctxArg(0, qc), qc);
    return node == null ? Empty.VALUE : node.root();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    final Expr expr;
    if(exprs.length > 0) {
      expr = exprs[0];
      if(expr.seqType().instanceOf(SeqType.DOCUMENT_NODE_ZO)) return expr;
    } else {
      expr = value;
    }
    exprType.data(expr);
    return optFirst(true, false, value);
  }
}
