package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class MapEntries extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final XQMap map = toMap(arg(0), qc);
      final BasicIter<Item> keys = map.keys().iter();

      @Override
      public XQMap next() throws QueryException {
        final Item key = keys.next();
        return key != null ? entry(key, map.get(key, info)) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        final Item key = keys.get(i);
        return entry(key, map.get(key, info));
      }
      @Override
      public long size() {
        return map.mapSize();
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    map.apply((key, value) -> vb.add(entry(key, value)));
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) exprType.assign(ft);
    return this;
  }

  /**
   * Creates a map pair.
   * @param key key
   * @param value value
   * @return created map entry
   * @throws QueryException query exception
   */
  XQMap entry(final Item key, final Value value) throws QueryException {
    return XQMap.entry(key, value, info);
  }
}
