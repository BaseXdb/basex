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
public final class GeoRelate extends GeoFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Geometry geo1 = checkGeo(0, qc), geo2 = checkGeo(1, qc);
    final byte[] matrix = toToken(exprs[2], qc);
    try {
      return Bln.get(geo1.relate(geo2, Token.string(matrix)));
    } catch(final IllegalArgumentException ex) {
      Util.debug(ex);
      throw GEO_ARG.get(info, matrix);
    }
  }
}
