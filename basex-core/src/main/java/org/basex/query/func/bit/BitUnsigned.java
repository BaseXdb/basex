package org.basex.query.func.bit;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BitUnsigned extends BitFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item item = atomItem(arg(0), qc);
    return item == Empty.VALUE ? Empty.VALUE :
      Itr.get(unsigned(toLong(item, arg(0)), toSize(arg(1), Long.MAX_VALUE, qc)));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    final Expr value = arg(0);
    final long size = size(arg(1));
    // bit:unsigned($i, 64) → $i
    if(size >= Long.SIZE && integer(value)) return value;
    // bit:unsigned(bit:unsigned($i, 8), 16) → bit:unsigned($i, 8)
    final long min = Math.min(size, _BIT_UNSIGNED.is(value) ? size(value.arg(1)) : -1);
    return min != -1 ? cc.function(_BIT_UNSIGNED, info, value.arg(0), Itr.get(min)) : this;
  }
}
