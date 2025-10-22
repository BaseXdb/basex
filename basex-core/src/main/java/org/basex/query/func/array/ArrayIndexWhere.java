package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

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
    final IntList list = new IntList();
    for(final Value value : array.iterable()) {
      if(test(predicate, args.set(0, value).inc(), qc)) list.add(args.pos());
    }
    return IntSeq.get(list.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return Empty.VALUE;

    final Type type = array.seqType().type;
    if(type instanceof final ArrayType at) {
      arg(1, arg -> refineFunc(arg, cc, at.valueType(), Types.INTEGER_O));
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
