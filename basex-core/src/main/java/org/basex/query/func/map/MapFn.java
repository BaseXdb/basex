package org.basex.query.func.map;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.type.*;

/**
 * Functions on maps.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class MapFn extends StandardFunc {
  /**
   * Returns the array size of the specified expression.
   * @param expr expression
   * @return map size
   */
  final long mapSize(final Expr expr) {
    return expr.seqType().instanceOf(Types.MAP_O) ? expr.structSize() : -1;
  }

  @Override
  public long structSize() {
    return seqType().type instanceof final RecordType rt &&
        !rt.hasOptional() ? rt.fields().size() : -1;
  }
}
