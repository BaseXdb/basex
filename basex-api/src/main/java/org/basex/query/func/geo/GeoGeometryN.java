package org.basex.query.func.geo;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

import com.vividsolutions.jts.geom.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GeoGeometryN extends GeoFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Geometry geo = checkGeo(0, qc);
    final long n = toLong(exprs[1], qc);
    if(n < 1 || n > geo.getNumGeometries()) throw GEO_RANGE.get(info, n);
    return toElement(geo.getGeometryN((int) n - 1), qc);
  }
}
