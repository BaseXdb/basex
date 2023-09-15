package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnIterateWhile extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem predicate = toFunction(arg(1), 1, qc), action = toFunction(arg(2), 1, qc);
    Value value = arg(0).value(qc);

    while(toBoolean(eval(predicate, qc, value).item(qc, info))) {
      value = eval(action, qc, value);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1), action = arg(2);

    // compute function types
    if(action instanceof FuncItem) {
      SeqType ist = input.seqType(), ost = SeqType.ITEM_ZM;
      Expr optAction = coerceFunc(action, cc, ost, ist);

      // repeat coercion until output types are equal and output type is instance of input type
      SeqType nst = optAction.funcType().declType;
      while(!ost.eq(nst) || !nst.instanceOf(ist)) {
        ist = ist.union(nst);
        optAction = coerceFunc(action, cc, SeqType.ITEM_ZM, ist);
        ost = nst;
        nst = nst.union(optAction.funcType().declType);
      }
      exprType.assign(ost);
      final Expr oa = optAction;
      arg(2, arg -> oa);

      if(predicate instanceof FuncItem) {
        final SeqType is = ist;
        arg(1, arg -> coerceFunc(predicate, cc, SeqType.BOOLEAN_O, is));
      }
    }
    return this;
  }
}
