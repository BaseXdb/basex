package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class FnSortWith extends SortFn {
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
  protected Expr opt(final CompileContext cc) {
    // even single items must be sorted, as the input might be invalid
    final Expr input = arg(0);
    return adoptType(input);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return simplifyOrder(mode, cc);
  }
}
