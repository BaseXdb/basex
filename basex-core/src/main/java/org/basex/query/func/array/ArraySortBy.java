package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ArraySortBy extends SortFn {
  @Override
  protected XQArray item(final QueryContext qc) throws QueryException {
    return sort(toArray(arg(0), qc), qc);
  }

  /**
   * Sorts the members of an array.
   * @param array array to be sorted
   * @param qc query context
   * @return sorted array
   * @throws QueryException query exception
   */
  final XQArray sort(final XQArray array, final QueryContext qc) throws QueryException {
    final long as = array.structSize();
    if(as == 0) return array;

    final ValueList list = new ValueList(as);
    for(final Value member : array.members()) list.add(member);
    final Value[] values = list.finish();
    final Integer[] index = index(values, qc);
    if(sorted(index)) return array;

    final ArrayBuilder ab = new ArrayBuilder(qc, as);
    for(final int i : index) ab.add(values[i]);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }

  @Override
  public long structSize() {
    return ArrayFn.arraySize(arg(0));
  }
}
