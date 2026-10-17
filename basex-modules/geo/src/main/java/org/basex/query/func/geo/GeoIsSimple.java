package org.basex.query.func.geo;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.item.Bln;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoIsSimple extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return Bln.get(toGeometry(0, qc).isSimple());
  }
}
