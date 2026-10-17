package org.basex.query.func.geo;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.item.B64;
import org.locationtech.jts.io.WKBWriter;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoAsBinary extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return B64.get(new WKBWriter().write(toGeometry(0, qc)));
  }
}
