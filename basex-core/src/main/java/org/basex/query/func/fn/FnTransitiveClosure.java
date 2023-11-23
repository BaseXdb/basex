package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnTransitiveClosure extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode node = toNodeOrNull(arg(0), qc);
    final FItem step = toFunction(arg(1), 1, qc);
    if(node == null) return Empty.VALUE;

    final ANodeBuilder result = new ANodeBuilder();
    Value input = node;
    while(true) {
      final ANodeBuilder output = new ANodeBuilder();
      for(final Item item : input) {
        for(final Item it : step.invoke(qc, info, item)) {
          final ANode n = toNode(it);
          if(!result.contains(n)) output.add(n);
        }
      }
      if(output.isEmpty()) return result.value(this);

      input = output.value(this);
      for(final Item item : input) result.add(toNode(item));
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr node = arg(0);
    return node.seqType().zero() ? node : this;
  }
}
