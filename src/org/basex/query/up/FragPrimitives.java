package org.basex.query.up;

import static org.basex.query.QueryText.*;

import org.basex.query.QueryException;
import org.basex.query.item.Nod;
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
  protected void add(final UpdatePrimitive p) throws QueryException {
    add(p.node.id(), p);
  }
  
  @Override
  protected void check() throws QueryException {
    super.check();

    // check fn:put constraints ... duplicate uri
    final TokenSet uris = new TokenSet();
    for(final int i : putIds.finish()) {
      final Put put = (Put) findPrimitive(PrimitiveType.PUT, op.get(i));
      if(uris.add(put.path()) < 0) Err.or(UPURIDUP, put.path());
    }
  }

  @Override
  protected void apply() throws QueryException {
    for(final int i : putIds.finish()) {
      final Put put = (Put) findPrimitive(PrimitiveType.PUT, op.get(i));
      put.apply(0);
    }
  }
  
  @Override
  protected boolean parentDeleted(final int n) {
    return false;
  }

  @Override
  protected int getId(final Nod n) {
    return n == null ? -1 : n.id();
  }
}
