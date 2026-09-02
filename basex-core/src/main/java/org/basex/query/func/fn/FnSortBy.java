package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnSortBy extends SortFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return iter(arg(0).value(qc), qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    return input.seqType().zero() ? input : adoptType(input);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    return simplifyOrder(mode, cc);
  }

  /**
   * Sort the input data and returns an iterator.
   * @param input items to be sorted
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  Iter iter(final Value input, final QueryContext qc) throws QueryException {
    if(input.isEmpty()) return input.iter();

    final long is = input.size();
    final ItemList list = new ItemList(is);
    for(final Item item : input) list.add(item);
    final Item[] values = list.finish();
    final Integer[] index = index(values, qc);
    return sorted(index) ? input.iter() : new BasicIter<>(is) {
      @Override
      public Item get(final long l) {
        return values[index[(int) l]];
      }
    };
  }
}
