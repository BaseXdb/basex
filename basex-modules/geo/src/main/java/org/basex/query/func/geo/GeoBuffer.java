package org.basex.query.func.geo;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.basex.query.value.node.GNode;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GeoBuffer extends GeoFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final GNode elem = toElem(exprs[0], qc);
    return toElement(toGeometry(elem).buffer(toDouble(exprs[1], qc)), elem);
  }
}
