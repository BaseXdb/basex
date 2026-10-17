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
public final class GeoExteriorRing extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode elem = toElem(exprs[0], qc);
    final Geometry geo = toGeometry(elem, POLYGON);
    if (!(geo instanceof Polygon)) {
      throw GEO_TYPE.get(info, POLYGON, elem.qname().local());
    }
    return toElement(((Polygon) geo).getExteriorRing(), elem);
  }
}
