package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCompare extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Item value1 = arg(0).atomItem(qc, info);
    final Item value2 = arg(1).atomItem(qc, info);
    final Collation collation = toCollation(arg(2), qc);

    if(value1.isEmpty() || value2.isEmpty()) return Empty.VALUE;
    if(!value1.comparable(value2)) throw compareError(value1, value2, info);

    final long diff = value1.compare(value2, collation, true, qc, info);
    return Itr.get(Long.signum(diff));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // compare((), $b) → (), compare($a, ()) → ()
    return optPair(arg(0), arg(1), cc);
  }
}
