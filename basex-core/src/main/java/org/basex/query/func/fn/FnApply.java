package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class FnApply extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final FItem func = toFunc(exprs[0], qc);
    return func.invokeValue(qc, info, values(func, qc));
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem func = toFunc(exprs[0], qc);
    return func.invokeItem(qc, info, values(func, qc));
  }

  /**
   * Returns the values to apply to the function.
   * @param func function
   * @param qc query context
   * @return values
   * @throws QueryException query exception
   */
  private Value[] values(final FItem func, final QueryContext qc) throws QueryException {
    final XQArray array = toArray(exprs[1], qc);
    final long ar = checkUp(func, this instanceof UpdateApply, sc).arity(), as = array.arraySize();
    if(ar != as) throw APPLY_X_X.get(info, ar, as);

    final ValueList values = new ValueList(Seq.initialCapacity(as));
    for(final Value value : array.members()) values.add(value);
    return values.finish();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    FuncType ft1 = expr1.funcType();
    final FuncType ft2 = expr2.funcType();

    // try to pass on types of array argument to function item
    if(ft2 instanceof ArrayType) {
      if(expr2 instanceof XQArray) {
        // argument is a value: final types are known
        final XQArray arr = (XQArray) expr2;
        final int as = Math.max(0, (int) arr.arraySize());
        final SeqType[] args = new SeqType[as];
        for(int a = 0; a < as; a++) args[a] = arr.get(a).seqType();
        exprs[0] = coerceFunc(exprs[0], cc, SeqType.ITEM_ZM, args);
      } else if(ft1 != null) {
        // argument will be of type array: assign generic array return type to all arguments
        final SeqType[] args1 = ft1.argTypes;
        if(args1 != null) {
          final int as = args1.length;
          final SeqType[] args2 = new SeqType[as];
          final ArrayType at2 = (ArrayType) ft2;
          for(int a = 0; a < as; a++) args2[a] = at2.declType;
          exprs[0] = coerceFunc(exprs[0], cc, SeqType.ITEM_ZM, args2);
        }
      }
    }

    final boolean updating = this instanceof UpdateApply;
    ft1 = exprs[0].funcType();
    if(ft1 != null && !updating) exprType.assign(ft1.declType);
    return this;
  }
}
