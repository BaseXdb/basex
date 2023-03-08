package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayIndexWhere extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 1, qc);

    int c = 0;
    final LongList list = new LongList();
    for(final Value value : array.members()) {
      ++c;
      if(toBoolean(predicate.invoke(qc, info, value).item(qc, info))) {
        list.add(c);
      }
    }
    return IntSeq.get(list);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return Empty.VALUE;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      arg(1, arg -> coerceFunc(arg, cc, SeqType.BOOLEAN_O, ((ArrayType) type).declType));
      exprType.assign(type);
    }
    return this;
  }
}
