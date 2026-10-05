package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapEntries extends MapFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);

    return new BasicIter<XQMap>(map.structSize()) {
      @Override
      public XQMap get(final long i) {
        return XQMap.get(map.keyAt(i), map.valueAt(i));
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    if(map.seqType().type instanceof final MapType mt) {
      exprType.assign(MapType.get(mt).seqType(Occ.ZERO_OR_MORE), map.structSize());
    }
    return this;
  }
}
