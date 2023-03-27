package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
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
public final class ArraySort extends StandardFunc {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Collation coll = toCollationOrDefault(arg(1), qc);
    final FItem key = defined(2) ? toFunction(arg(2), 1, qc) : null;

    final ValueList values = new ValueList(array.arraySize());
    for(final Value value : array.members()) {
      values.add((key == null ? value : key.invoke(qc, info, value)).atomValue(qc, info));
    }

    final ArrayBuilder ab = new ArrayBuilder();
    for(final int order : FnSort.sort(values, this, coll, qc)) {
      ab.append(array.get(order));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final SeqType st = array.seqType();
    final Type type = st.type;
    if(type instanceof ArrayType) {
      if(defined(2)) {
        arg(2, arg -> coerceFunc(arg, cc, SeqType.ANY_ATOMIC_TYPE_ZM, ((ArrayType) type).declType));
      }
      exprType.assign(type);
    }
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && defined(2) || super.has(flags);
  }
}
