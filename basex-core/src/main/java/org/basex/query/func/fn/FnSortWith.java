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
public class FnSortWith extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final ValueList values = new ValueList(Seq.initialCapacity(input.size()));
    for(Item item; (item = qc.next(input)) != null;) values.add(item);
    sort(values, qc);

    return new BasicIter<>(values.size()) {
      @Override
      public Item get(final long l) {
        return (Item) values.get((int) l);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  /**
   * Sort the input data and returns an iterator.
   * @param values values to be sorted
   * @param qc query context
   * @throws QueryException query exception
   */
  protected final void sort(final ValueList values, final QueryContext qc) throws QueryException {
    final Value comparators = arg(1).value(qc);
    if(comparators.isEmpty()) throw EMPTYFOUND.get(info);

    final FItem[] cmps = new FItem[(int) comparators.size()];
    int c = 0;
    for(final Item item : comparators) cmps[c++] = toFunction(item, 2, qc);

    final Comparator<Value> comparator = (value1, value2) -> {
      try {
        for(final FItem cmp : cmps) {
          final long diff = toLong(cmp.invoke(qc, info, value1, value2).item(qc, info));
          if(diff != 0) return Long.signum(diff);
        }
        return 0;
      } catch(final QueryException ex) {
        throw new QueryRTException(ex);
      }
    };

    try {
      Arrays.sort(values.list, 0, values.size(), comparator);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
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
