package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayIndexOf extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Value target = arg(1).value(qc);
    final Collation collation = toCollation(arg(2), qc);

    int c = 0;
    final DeepEqual deep = new DeepEqual(info, collation, qc);
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Value value : array.members()) {
      qc.checkStop();
      ++c;
      if(deep.equal(value, target)) vb.add(c);
    }
    return vb.value(BasicType.INTEGER);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    // array:index-of([], $target) → ()
    if(array == XQArray.empty()) return Empty.VALUE;

    if(arraySize(array) == 1) exprType.assign(Occ.ZERO_OR_ONE);
    return this;
  }
}
