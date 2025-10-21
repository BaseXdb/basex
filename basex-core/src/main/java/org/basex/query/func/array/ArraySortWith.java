package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
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
public final class ArraySortWith extends FnSortWith {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);

    final ValueList values = new ValueList(Seq.initialCapacity(array.structSize()));
    for(final Value member : array.iterable()) values.add(member);
    sort(values, qc);

    final ArrayBuilder ab = new ArrayBuilder(qc, values.size());
    for(final Value value : values) ab.add(value);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // even single items must be sorted, as the input might be invalid
    final Expr array = arg(0);
    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }
}
