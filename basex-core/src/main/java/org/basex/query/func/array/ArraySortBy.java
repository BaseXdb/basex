package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
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
public class ArraySortBy extends FnSortBy {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final long as = array.structSize();
    if(as == 0) return array;

    final ValueList list = new ValueList(as);
    for(final Value member : array.iterable()) list.add(member);
    final Value[] values = list.finish();
    final Integer[] index = index(values, qc);
    if(sorted(index)) return array;

    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final int i : index) ab.add(values[i]);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) exprType.assign(type);
    return this;
  }
}
