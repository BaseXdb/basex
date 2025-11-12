package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilSelect extends ContextFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter keys = arg(0).iter(qc);
    final ANode node = toNode(context(qc), qc);

    final QNm name = node.qname();
    for(Item item; (item = qc.next(keys)) != null;) {
      if(item.atomicEqual(name)) return Bln.TRUE;
    }
    return Bln.FALSE;
  }

  @Override
  public int contextIndex() {
    return 1;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = arg(0), expr2 = defined(1) ? arg(1) : cc.qc.focus.value;
    final SeqType st1 = expr1.seqType(), st2 = expr2 != null ? expr2.seqType() : null;
    if(st1.zero()) return cc.function(Function.BOOLEAN, info, expr1);

    if(expr2 != null) {
      if(st1.type == AtomType.QNAME && st2.instanceOf(Types.NODE_O)) {
        // util:select((#a, #b)) → node-name() = (#a, #b)
        final Expr[] args = defined(1) ? new Expr[] { expr2 } : new Expr[0];
        final Expr op1 = cc.function(Function.NODE_NAME, info, args);
        return new CmpG(info, op1, expr1, OpG.EQ).optimize(cc);
      }
    }
    return this;
  }
}
