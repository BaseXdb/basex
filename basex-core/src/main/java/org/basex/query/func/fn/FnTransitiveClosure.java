package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnTransitiveClosure extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    final FItem step = toFunction(arg(1), 1, qc);

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
}
