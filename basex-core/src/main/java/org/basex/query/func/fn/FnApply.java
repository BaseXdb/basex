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

    final ValueList vl = new ValueList((int) as);
    for(final Value val : array.members()) vl.add(val);
    return vl.finish();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof FuncType) exprType.assign(((FuncType) t).declType);
    return this;
  }
}
