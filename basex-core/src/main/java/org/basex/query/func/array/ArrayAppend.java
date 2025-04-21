package org.basex.query.func.array;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayAppend extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Value member = arg(1).value(qc);
    return array.appendMember(member, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0), add = arg(1);
    if(array == XQArray.empty()) return cc.function(_UTIL_ARRAY_MEMBER, info, add);

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      final SeqType mt = ((ArrayType) type).valueType().union(add.seqType());
      exprType.assign(ArrayType.get(mt));
    }
    return this;
  }
}
