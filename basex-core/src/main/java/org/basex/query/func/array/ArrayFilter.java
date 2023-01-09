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
    final XQArray array = toArray(exprs[0], qc);
    final FItem predicate = toFunction(exprs[1], 1, qc);

    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : array.members()) {
      if(toBoolean(predicate.invoke(qc, info, value).item(qc, info))) {
        ab.append(value);
      }
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = exprs[0];
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O, ((ArrayType) type).declType);
      exprType.assign(type);
    }
    return this;
  }
}
