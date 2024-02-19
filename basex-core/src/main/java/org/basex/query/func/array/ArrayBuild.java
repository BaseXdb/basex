package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayBuild extends StandardFunc {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunctionOrNull(arg(1), 1, qc);

    final ArrayBuilder ab = new ArrayBuilder();
    for(Item item; (item = qc.next(input)) != null;) {
      ab.append(action != null ? action.invoke(qc, info, item) : item);
    }
    return ab.array();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return cc.voidAndReturn(input, XQArray.empty(), info);

    if(defined(1)) {
      arg(1, arg -> refineFunc(arg, cc, SeqType.ITEM_ZM, st.with(Occ.EXACTLY_ONE)));
      final FuncType ft = arg(1).funcType();
      if(ft != null) exprType.assign(ArrayType.get(ft.declType));
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
