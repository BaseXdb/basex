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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends FnSort {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final ValueList list = new ValueList(array.arraySize());
    for(final Value member : array.members()) list.add(member);
    final Value[] values = list.finish();

    final ArrayBuilder ab = new ArrayBuilder();
    for(final int i : index(values, qc)) ab.append(values[i]);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final SeqType st = array.seqType();
    final Type type = st.type;
    if(type instanceof ArrayType) {
      if(defined(2) && arg(2).size() == 1) {
        arg(2, arg -> refineFunc(arg, cc, SeqType.ANY_ATOMIC_TYPE_ZM, ((ArrayType) type).declType));
      }
      exprType.assign(type);
    }
    return this;
  }
}
