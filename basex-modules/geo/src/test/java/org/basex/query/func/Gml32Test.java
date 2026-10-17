package org.basex.query.func;

import static org.basex.query.func.geo.GeoFn.*;
import static org.basex.util.Token.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo" on GML 3.2 geometries.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class Gml32Test extends Gml3Test {
  @Override
  protected String gmlUri() {
    return string(GML32_URI);
  }
}
