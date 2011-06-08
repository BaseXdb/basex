package org.basex.query.up;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.up.primitives.PrimitiveType;
import org.basex.query.up.primitives.Put;
import org.basex.query.up.primitives.Primitive;
import static org.basex.util.Token.token;
import org.basex.util.TokenSet;

/**
 * Holds all update primitives for a specific data reference.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
final class FragPrimitives extends Primitives {
  @Override
  protected void add(final Primitive p) throws QueryException {
    add(p.node.id, p);
  }

  @Override
  protected void check(final QueryContext ctx) throws QueryException {
    super.check();

    // check fn:put constraints ... duplicate uri
    final TokenSet uris = new TokenSet();
    for(final int i : putIds.toArray()) {
      final Put put = (Put) op.get(i).find(PrimitiveType.PUT);
      if(uris.add(token(put.path())) < 0) UPURIDUP.thrw(put.input, put.path());
    }
  }

  @Override
  protected void apply(final QueryContext ctx) throws QueryException {
    for(final int i : putIds.toArray()) {
      ((Put) op.get(i).find(PrimitiveType.PUT)).apply(0);
    }
  }

  @Override
  protected boolean parentDeleted(final int n) {
    return false;
  }
}
