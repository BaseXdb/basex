package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnApply extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem fun = toFunc(exprs[0], qc);
    return fun.invokeValue(qc, info, values(fun, qc));
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final FItem fun = toFunc(exprs[0], qc);
    return fun.invokeItem(qc, info, values(fun, qc));
  }

  /**
   * Returns the values to apply to the function.
   * @param fun function
   * @param qc query context
   * @return values
   * @throws QueryException query exception
   */
  private Value[] values(final FItem fun, final QueryContext qc) throws QueryException {
    final Array array = toArray(exprs[1], qc);
    if(!sc.mixUpdates && fun.annotations().contains(Annotation.UPDATING))
      throw FUNCUP_X.get(info, fun);

    final long ar = fun.arity(), as = array.arraySize();
    if(ar != as) throw APPLY_X_X.get(info, ar, as);

    final ValueList vl = new ValueList(as);
    for(final Value val : array.members()) vl.add(val);
    return vl.finish();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex1 = exprs[0], ex2 = exprs[1];
    final Type t1 = ex1.seqType().type, t2 = ex2.seqType().type;
    final FuncType ft1 = t1 instanceof FuncType ? (FuncType) t1 : null;

    // try to pass on types of array argument to function item
    if(t2 instanceof ArrayType) {
      if(ex2 instanceof Array) {
        // argument is a value: final types are known
        final Array arr = (Array) ex2;
        final int as = Math.max(0, (int) arr.arraySize());
        final SeqType[] args = new SeqType[as];
        for(int a = 0; a < as; a++) args[a] = arr.get(a).seqType();
        coerceFunc(0, cc, SeqType.ITEM_ZM, args);
      } else if(ft1 != null) {
        // argument will be of type array: assign generic array return type to all arguments
        final SeqType[] args1 = ft1.argTypes;
        if(args1 != null) {
          final int as = args1.length;
          final SeqType[] args2 = new SeqType[as];
          final ArrayType at2 = (ArrayType) t2;
          for(int a = 0; a < as; a++) args2[a] = at2.declType;
          coerceFunc(0, cc, SeqType.ITEM_ZM, args2);
        }
      }
    }

    if(ft1 != null) exprType.assign(ft1.declType);
    return this;
  }
}
