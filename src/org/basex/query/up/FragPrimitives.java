package org.basex.query.up;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.query.util.Err;
import org.basex.util.TokenSet;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
final class FragPrimitives extends Primitives {
  @Override
  protected void check() throws QueryException {
    super.check();

    // [LK] revise data structures ... lots of useless access
    // check fn:put constraints ... duplicate uri
    final TokenSet uris = new TokenSet();
    for(final UpdatePrimitive[] ups : op.values()) {
      final Put put = (Put) ups[PrimitiveType.PUT.ordinal()];
      if(put == null) continue;
      if(uris.add(put.path()) < 0) Err.or(UPURIDUP, put.path());
    }
  }

  @Override
  protected void apply() throws QueryException {
    for(final UpdatePrimitive[] ups : op.values()) {
      final Put put = (Put) ups[PrimitiveType.PUT.ordinal()];
      if(put == null) continue;
      put.apply(0);
    }
  }
}
