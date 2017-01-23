package org.basex.query.func.random;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Dirk Kirsten
 */
public final class RandomSeededPermutation extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final long seed = toLong(exprs[0], qc);
    final ItemList list = new ItemList();
    final Random r = new Random(seed);

    final Iter iter = qc.iter(exprs[1]);
    for(Item item; (item = iter.next()) != null;) {
      final int ls = list.size();
      final int l = r.nextInt(ls + 1);
      if(l < ls) {
        final Item it = list.get(l);
        list.set(l, item);
        item = it;
      }
      list.add(item);
    }
    return list.value();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
