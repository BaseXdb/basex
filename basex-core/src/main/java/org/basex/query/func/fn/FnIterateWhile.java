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
    final FItem predicate = toFunction(exprs[1], 1, qc), action = toFunction(exprs[2], 1, qc);
    Value value = exprs[0].value(qc);

    while(toBoolean(predicate.invoke(qc, info, value).item(qc, info))) {
      value = action.invoke(qc, info, value);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], predicate = exprs[1], action = exprs[2];

    // compute function types
    if(action instanceof FuncItem) {
      SeqType ist = input.seqType(), ost = SeqType.ITEM_ZM;
      Expr optAction = coerceFunc(action, cc, ost, ist);

      // repeat coercion until output types are equal and output type is instance of input type
      SeqType nst = optAction.funcType().declType;
      if (nst.type == NoneType.NONE) {
        ost = ist;
      }
      else {
        while(!ost.eq(nst) || !nst.instanceOf(ist)) {
          ist = ist.union(nst);
          optAction = coerceFunc(action, cc, SeqType.ITEM_ZM, ist);
          ost = nst;
          nst = nst.union(optAction.funcType().declType);
        }
      }
      exprType.assign(ost);
      exprs[2] = optAction;

      if(predicate instanceof FuncItem) {
        exprs[1] = coerceFunc(predicate, cc, SeqType.BOOLEAN_O, ist);
      }
    }
    return this;
  }
}
