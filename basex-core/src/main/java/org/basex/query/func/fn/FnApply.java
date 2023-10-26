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

import java.util.Arrays;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnApply extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem function = checkUp(toFunction(arg(0), qc), this instanceof UpdateApply, sc);
    final XQArray arguments = toArray(arg(1), qc);

    final ValueList args = new ValueList(arguments.arraySize());
    for(final Value arg : arguments.members()) args.add(arg);
    return apply(function, args, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr func = arg(0), args = arg(1);
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
        arg(0, arg -> coerceFunc(arg, cc, SeqType.ITEM_ZM, ast));
      } else if(ft != null) {
        // argument will be of type array: assign generic array return type to all arguments
        final SeqType[] at = ft.argTypes;
        if(at != null) {
          final SeqType[] ast = new SeqType[at.length];
          Arrays.fill(ast, ((ArrayType) ftArgs).declType);
          arg(0, arg -> coerceFunc(arg, cc, SeqType.ITEM_ZM, ast));
        }
      }
    }

    ft = arg(0).funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }

  /**
   * Applies a function with the specified arguments.
   * @param func function
   * @param args arguments
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  protected final Value apply(final FItem func, final ValueList args, final QueryContext qc)
      throws QueryException {
    final long ar = func.arity(), as = args.size();
    if(ar != as) throw APPLY_X_X.get(info, arguments(as), func, args);
    return eval(func, qc, args.finish());
  }
}
