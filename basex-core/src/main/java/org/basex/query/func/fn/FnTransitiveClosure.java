package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
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
    final long min = defined(2) ? toLong(arg(2), qc) : 1;
    final long max = defined(3) ? toLong(arg(3), qc) : Long.MAX_VALUE;

    final DeepEqual deep = new DeepEqual(info, null, qc);
    Value input = node;
    for(long m = Math.max(1, min); m < max; m++) {
      final ANodeBuilder nb = new ANodeBuilder();
      for(final Item in : input) {
        nb.add(toNode(in));
        for(final Item item : step.invoke(qc, info, in)) {
          nb.add(toNode(item));
        }
      }
      final Value output = nb.value(this);
      if(deep.equal(input, output)) break;
      input = output;
    }
    return input;
  }
}
