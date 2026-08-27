package org.basex.query.func.array;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySize extends ArrayFn {
  @Override
  protected Itr item(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    return Itr.get(array.structSize());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    final long size = arraySize(array);
    // array:size($array) → INTEGER  (statically known size)
    if(size != -1 && !array.has(Flag.NDT)) return Itr.get(size);

    // array:size(array:reverse($array)) → array:size($array)
    if(_ARRAY_REVERSE.is(array) || _ARRAY_SORT.is(array) || _ARRAY_SORT_BY.is(array) ||
        _ARRAY_SORT_WITH.is(array))
      return cc.function(_ARRAY_SIZE, info, array.arg(0));

    // array:size(array:append($array, $member)) → array:size($array) + 1
    if(_ARRAY_APPEND.is(array)) {
      final Expr count = cc.function(_ARRAY_SIZE, info, array.arg(0));
      return cc.voidAndReturn(array.arg(1),
          new Arith(info, count, Itr.ONE, Calc.ADD).optimize(cc), info);
    }
    return this;
  }
}
