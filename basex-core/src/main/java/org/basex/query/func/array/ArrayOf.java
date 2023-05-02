package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
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
public final class ArrayOf extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);

    Item item = input.next();
    if(item == null) return XQArray.empty();

    final Value first = toMember(item);
    item = input.next();
    if(item == null) return XQArray.member(first);

    final ArrayBuilder ab = new ArrayBuilder().append(first);
    do {
      ab.append(toMember(item));
    } while((item = qc.next(input)) != null);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) {
      exprType.assign(ArrayType.get(ft.declType));
    }
    return this;
  }
}
