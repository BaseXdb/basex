package org.basex.query.func.geo;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.item.Dbl;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoY extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return Dbl.get(toGeometry(0, qc, POINT).getCoordinate().y);
  }
}
