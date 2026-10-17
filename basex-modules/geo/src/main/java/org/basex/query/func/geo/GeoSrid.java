package org.basex.query.func.geo;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoSrid extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return Itr.get(toGeometry(0, qc).getSRID());
  }
}
