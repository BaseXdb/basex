package org.basex.query.func.geo;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.item.Str;
import org.locationtech.jts.io.WKTWriter;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoAsText extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return Str.get(new WKTWriter().write(toGeometry(0, qc)));
  }
}
