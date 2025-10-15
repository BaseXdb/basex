package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class FnSortWith extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final Value comparators = arg(1).value(qc);
    if(comparators.isEmpty()) throw EMPTYFOUND.get(info);

    final FItem[] cmps = new FItem[(int) comparators.size()];
    int c = 0;
    for(final Item item : comparators) cmps[c++] = toFunction(item, 2, qc);

    final Comparator<Item> comparator = (item1, item2) -> {
      try {
        for(final FItem cmp : cmps) {
          final long diff = toLong(cmp.invoke(qc, info, item1, item2).item(qc, info));
          if(diff != 0) return diff < 0 ? -1 : 1;
        }
        return 0;
      } catch(final QueryException ex) {
        throw new QueryRTException(ex);
      }
    };

    final ItemList items = new ItemList(Seq.initialCapacity(input.size()));
    for(Item item; (item = qc.next(input)) != null;) items.add(item);
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
    return adoptType(input);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // count(sort(A))  ->  count(A)
    return cc.simplify(this, mode == Simplify.COUNT ? arg(0) : this, mode);
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
