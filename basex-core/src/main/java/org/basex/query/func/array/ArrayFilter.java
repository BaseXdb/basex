package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayFilter extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    int p = 0;
    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : array.members()) {
      if(toBoolean(predicate.invoke(qc, info, value, Int.get(++p)).item(qc, info))) {
        ab.append(value);
      }
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      arg(1, arg -> coerceFunc(arg, cc, SeqType.BOOLEAN_O, ((ArrayType) type).declType,
          SeqType.INTEGER_O));
      exprType.assign(type);
    }
    return this;
  }
}
