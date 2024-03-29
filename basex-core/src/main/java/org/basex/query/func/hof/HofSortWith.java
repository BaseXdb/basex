package org.basex.query.func.hof;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class HofSortWith extends HofFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Comparator<Item> comparator = comparator(qc);

    final ItemList items = new ItemList(Seq.initialCapacity(input.size()));
    for(Item item; (item = qc.next(input)) != null;) {
      items.add(item);
    }
    try {
      Arrays.sort(items.list, 0, items.size(), comparator);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return items.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // even single items must be sorted, as the input might be invalid
    final Expr input = arg(0);
    return input.seqType().zero() ? input : adoptType(input);
  }
}
