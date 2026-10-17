package org.basex.query.func.geo;

import static org.basex.query.func.geo.GeoError.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.locationtech.jts.geom.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoIsRing extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode elem = toElem(exprs[0], qc);
    final Geometry geo = toGeometry(elem, LINE);
    if (!(geo instanceof LineString)) {
      throw GEO_TYPE.get(info, LINE, elem.qname().local());
    }
    return Bln.get(((LineString) geo).isRing());
  }
}
