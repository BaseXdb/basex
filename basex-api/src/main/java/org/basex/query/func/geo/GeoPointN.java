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
public final class GeoPointN extends GeoFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Geometry geo = geo(0, qc, LINE, Q_GML_LINEARRING, Q_GML_LINESTRING);
    final int max = geo.getNumPoints();
    final long n = toLong(exprs[1], qc);
    if(n < 1 || n > max) throw GEO_RANGE.get(info, n);
    return toElement(((LineString) geo).getPointN((int) n - 1), qc);
  }
}
