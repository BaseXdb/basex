package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UtilDdo extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final Value value = iter.iterValue();
    if(value instanceof DBNodeSeq) return value;

    final ANodeBuilder nb = new ANodeBuilder();
    for(Item item; (item = qc.next(iter)) != null;) nb.add(toNode(item));
    return nb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.instanceOf(SeqType.NOD_ZM)) {
      if(st.zeroOrOne()) return expr;
      exprType.assign(st.type);
    }
    return this;
  }
}
