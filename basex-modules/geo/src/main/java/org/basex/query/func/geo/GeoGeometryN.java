package org.basex.query.func.geo;

import static org.basex.query.func.geo.GeoError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.locationtech.jts.geom.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoGeometryN extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode elem = toElem(exprs[0], qc);
    final Geometry geo = toGeometry(elem);
    final long n = toLong(exprs[1], qc);
    if (n < 1 || n > geo.getNumGeometries()) {
      throw GEO_RANGE.get(info, n);
    }
    return toElement(geo.getGeometryN((int) n - 1), elem);
  }
}
