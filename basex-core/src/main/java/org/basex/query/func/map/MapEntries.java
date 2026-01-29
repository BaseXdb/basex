package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class MapEntries extends MapFn {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);

    return new Iter() {
      final BasicIter<Item> keys = map.keys().iter();

      @Override
      public XQMap next() throws QueryException {
        final Item key = keys.next();
        return key != null ? XQMap.get(key, map.get(key)) : null;
      }

      @Override
      public Item get(final long i) throws QueryException {
        final Item key = keys.get(i);
        return XQMap.get(key, map.get(key));
      }

      @Override
      public long size() {
        return map.structSize();
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
