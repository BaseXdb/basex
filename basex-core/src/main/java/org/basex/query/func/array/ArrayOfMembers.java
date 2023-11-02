package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
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
public final class ArrayOfMembers extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter members = arg(0).iter(qc);

    final ArrayBuilder ab = new ArrayBuilder();
    for(Item item; (item = qc.next(members)) != null;) {
      ab.append(toRecord(item, Str.VALUE).get(Str.VALUE, info));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) exprType.assign(ArrayType.get(ft.declType));
    return this;
  }
}
