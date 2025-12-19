package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayBuild extends StandardFunc {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem action = toFunctionOrNull(arg(1), 2, qc);
    if(action == null) return XQArray.items(arg(0).value(qc));

    final Iter input = arg(0).iter(qc);
    final ArrayBuilder ab = new ArrayBuilder(qc, input.size());
    final HofArgs args = new HofArgs(2, action);
    for(Item item; (item = qc.next(input)) != null;) {
      ab.add(invoke(action, args.set(0, item).inc(), qc));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    // array:build(()) → {}
    if(st.zero()) return cc.voidAndReturn(input, XQArray.empty(), info);
    // array:build(1 to 3) → array { 1 to 3 }
    if(!defined(1)) return new CItemArray(info, input);

    arg(1, arg -> refineFunc(arg, cc, st.with(Occ.EXACTLY_ONE), Types.INTEGER_O));
    final FuncType ft = arg(1).funcType();
    if(ft != null) exprType.assign(ArrayType.get(ft.declType));
    return this;
  }

  @Override
  public long structSize() {
    return arg(0).size();
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
