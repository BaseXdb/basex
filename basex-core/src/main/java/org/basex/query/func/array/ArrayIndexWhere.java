package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayIndexWhere extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    final HofArgs args = new HofArgs(2, predicate);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value value : array.members()) {
      if(test(predicate, args.set(0, value).inc(), qc)) vb.add(args.pos());
    }
    return vb.value(BasicType.INTEGER);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    // array:index-where([], $predicate) → ()
    if(array == XQArray.empty()) return Empty.VALUE;

    if(arraySize(array) == 1) exprType.assign(Occ.ZERO_OR_ONE);
    if(array.seqType().type instanceof final ArrayType at) {
      arg(1, arg -> refineFunc(arg, cc, at.valueType(), Types.INTEGER_O));
    }
    return this;
  }
}
