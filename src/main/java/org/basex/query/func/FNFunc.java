package org.basex.query.func;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;

/**
 * Functions on functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNFunc extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FILTER:
      case MAP:
      case MAPPAIRS:
      case FOLDLEFT:
      case FOLDRIGHT:
        Err.or(NOTIMPL, func.desc);
        return null;
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case FUNCNAME:
      case FUNCARITY:
        Err.or(NOTIMPL, func.desc);
        return null;
      default:
        return super.atomic(ctx);
    }
  }
}
