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
public final class GeoInteriorRingN extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode elem = toElem(exprs[0], qc);
    final Geometry geo = toGeometry(elem, POLYGON);
    if (!(geo instanceof Polygon)) {
      throw GEO_TYPE.get(info, POLYGON, elem.qname().local());
    }
    final long n = toLong(exprs[1], qc);
    final int max = ((Polygon) geo).getNumInteriorRing();
    if (n < 1 || n > max) {
      throw GEO_RANGE.get(info, n);
    }
    return toElement(((Polygon) geo).getInteriorRingN((int) n - 1), elem);
  }
}
