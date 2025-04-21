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
 * @author BaseX Team, BSD License
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
        return key != null ? entry(key, map.get(key)) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        final Item key = keys.get(i);
        return entry(key, map.get(key));
      }
      @Override
      public long size() {
        return map.structSize();
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);

    final ValueBuilder vb = new ValueBuilder(qc, structSize());
    map.forEach((key, value) -> vb.add(entry(key, value)));
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      exprType.assign(type.seqType(Occ.ZERO_OR_MORE), map.structSize());
    }
    return this;
  }

  /**
   * Returns a single map entry as a new map.
   * @param key key
   * @param value value
   * @return created map entry
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  XQMap entry(final Item key, final Value value) throws QueryException {
    return XQMap.get(key, value);
  }
}
