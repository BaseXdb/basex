package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnTransitiveClosure extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode node = toGNodeOrNull(arg(0), qc);
    final FItem step = toFunction(arg(1), 1, qc);
    if(node == null) return Empty.VALUE;

    final GNodeBuilder result = new GNodeBuilder();
    Value input = node;
    while(true) {
      final GNodeBuilder output = new GNodeBuilder();
      for(final Item item : input) {
        for(final Item it : step.invoke(qc, info, item)) {
          final GNode n = toGNode(it);
          if(!result.contains(n)) output.add(n);
        }
      }
      if(output.isEmpty()) return result.value(this);

      input = output.value(this);
      for(final Item item : input) result.add(toGNode(item));
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr node = arg(0), step = arg(1);
    final SeqType nst = node.seqType();
    if(nst.zero()) return node;

    if(step instanceof FuncItem || step instanceof Closure) {
      // the step is applied to the input node and to all nodes it returns
      SeqType st = nst.with(Occ.EXACTLY_ONE), ost, rt;
      do {
        final SeqType[] types = { st };
        arg(1, arg -> step.refineFunc(cc, types));
        rt = arg(1).funcType().refinedType;
        ost = st;
        if(!rt.zero()) st = st.union(rt.with(Occ.EXACTLY_ONE));
      } while(!st.eq(ost));
      if(!rt.zero() && rt.type.instanceOf(seqType().type)) {
        exprType.assign(rt.type, Occ.ZERO_OR_MORE);
      }
    }
    return this;
  }
}
