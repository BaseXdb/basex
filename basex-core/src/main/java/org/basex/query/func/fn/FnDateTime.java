package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDateTime extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item date = atomItem(arg(0), qc);
    final Item time = atomItem(arg(1), qc);
    if(date.isEmpty() || time.isEmpty()) return Empty.VALUE;

    final Dat dat = date.type.isUntyped() ? new Dat(date.string(info), info) :
      (Dat) checkType(date, BasicType.DATE);
    final Tim tim = time.type.isUntyped() ? new Tim(time.string(info), info) :
      (Tim) checkType(time, BasicType.TIME);
    return new Dtm(dat, tim, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // dateTime((), $time) → (), dateTime($date, ()) → ()
    return optPair(arg(0), defined(1) ? arg(1) : Str.EMPTY, cc);
  }
}
