package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ProfTime extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // create timer
    final Performance p = new Performance();

    // optional message
    final byte[] msg = exprs.length > 2 ? toToken(exprs[2], qc) : null;

    // check caching flag
    if(exprs.length > 1 && toBoolean(exprs[1], qc)) {
      final Value v = qc.value(exprs[0]).cache().value();
      FnTrace.trace(token(p.getTime()), msg, qc);
      return v.iter();
    }

    return new Iter() {
      final Iter iter = qc.iter(exprs[0]);
      @Override
      public Item next() throws QueryException {
        final Item it = iter.next();
        if(it == null) FnTrace.trace(token(p.getTime()), msg, qc);
        return it;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(exprs[0]);
  }
}
