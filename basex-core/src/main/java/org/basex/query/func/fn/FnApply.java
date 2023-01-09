package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnApply extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final FItem func = toFunction(exprs[0], qc);
    final XQArray args = toArray(exprs[1], qc);

    final long ar = checkUp(func, this instanceof UpdateApply, sc).arity(), as = args.arraySize();
    if(ar != as) throw APPLY_X_X.get(info, arguments(as), func, args);

    final ValueList values = new ValueList(as);
    for(final Value value : args.members()) values.add(value);
    return func.invoke(qc, info, values.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr func = exprs[0], args = exprs[1];
    FuncType ft = func.funcType();
    final FuncType ftArgs = args.funcType();

    // try to pass on types of array argument to function item
    if(ftArgs instanceof ArrayType) {
      if(args instanceof XQArray) {
        // argument is a value: final types are known
        final XQArray array = (XQArray) args;
        final int as = Math.max(0, (int) array.arraySize());
        final SeqType[] ast = new SeqType[as];
        for(int a = 0; a < as; a++) ast[a] = array.get(a).seqType();
        exprs[0] = coerceFunc(exprs[0], cc, SeqType.ITEM_ZM, ast);
      } else if(ft != null) {
        // argument will be of type array: assign generic array return type to all arguments
        final SeqType[] at = ft.argTypes;
        if(at != null) {
          final int al = at.length;
          final SeqType[] ast = new SeqType[al];
          final ArrayType at2 = (ArrayType) ftArgs;
          for(int a = 0; a < al; a++) ast[a] = at2.declType;
          exprs[0] = coerceFunc(exprs[0], cc, SeqType.ITEM_ZM, ast);
        }
      }
    }

    ft = exprs[0].funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }
}
