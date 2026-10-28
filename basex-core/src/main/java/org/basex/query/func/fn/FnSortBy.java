package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnSortBy extends SortFn {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return iter(arg(0).value(qc), qc);
  }

  /**
   * Sorts the input and returns an iterator with the smallest items.
   * @param limit number of required items
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  final Iter iter(final int limit, final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final long size = input.size();
    if(size != -1 && limit >= size >> 3) return iter(input.value(qc, arg(0)), qc);

    // few items requested: only keep the smallest items in memory
    final Value eager = input.eagerValue(), quick = eager != null ? quickValue(eager) : null;
    return quick != null ? quick.iter() : smallest(input, limit, qc).iter();
  }

  /**
   * Returns the sorted input if it can be computed without comparisons.
   * @param input input value
   * @return sorted value or {@code null}
   */
  Value quickValue(@SuppressWarnings("unused") final Value input) {
    return null;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    if(input.seqType().zero()) return input;
    coerceKeys(cc);

    // sort-by(E, { 'key': K }) → sort(E, (), K)
    final Expr[] args = sortArgs(false);
    if(args != null) return cc.function(SORT, info, args);
    return adoptType(input);
  }

  /**
   * Returns a call to {@code fn:lowest} or {@code fn:highest} that yields the first or last item.
   * @param first first or last item
   * @param cc compilation context
   * @return function call or {@code null}
   * @throws QueryException query exception
   */
  public Expr extreme(final boolean first, final CompileContext cc) throws QueryException {
    // ascending keys have already been rewritten to fn:sort
    final Expr[] args = sortArgs(true);
    return args != null ? lowestOrHighest(!first, args, cc) : null;
  }

  /**
   * Returns a call to {@code fn:lowest} or {@code fn:highest}.
   * @param lowest lowest or highest items
   * @param args arguments of the sort call
   * @param cc compilation context
   * @return function call or {@code null}
   * @throws QueryException query exception
   */
  final Expr lowestOrHighest(final boolean lowest, final Expr[] args, final CompileContext cc)
      throws QueryException {
    // fn:lowest and fn:highest compare untyped keys as numbers
    Type type = args[0].seqType().type;
    if(args.length > 2 && !args[2].seqType().zero()) {
      final FuncType ft = args[2].funcType();
      type = ft != null ? ft.refinedType.type : null;
    }
    final BasicType atomic = type != null ? type.atomic() : null;
    return atomic == null || BasicType.UNTYPED_ATOMIC.instanceOf(atomic) ? null :
      cc.function(lowest ? LOWEST : HIGHEST, info, args);
  }

  @Override
  public final Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {
    return simplifyOrder(mode, cc);
  }

  /**
   * Sorts the input data and returns an iterator.
   * @param input items to be sorted
   * @param qc query context
   * @return iterator with ordered items
   * @throws QueryException query exception
   */
  final Iter iter(final Value input, final QueryContext qc) throws QueryException {
    final Value quick = quickValue(input);
    if(quick != null) return quick.iter();
    if(input.isEmpty()) return input.iter();

    final long is = input.size();
    final ItemList list = new ItemList(is);
    for(final Item item : input) list.add(item);
    final Item[] values = list.finish();
    final int[] index = index(values, qc);
    return sorted(index) ? input.iter() : new BasicIter<>(index.length) {
      @Override
      public Item get(final long l) {
        return values[index[(int) l]];
      }
    };
  }
}
