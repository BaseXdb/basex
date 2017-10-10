package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayForEach extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value val : array.members()) builder.append(fun.invokeValue(qc, info, val));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[1].seqType().type;
    if(t instanceof FuncType) {
      seqType = SeqType.get(ArrayType.get(((FuncType) t).valueType), Occ.ONE);
    }
    return this;
  }
}
