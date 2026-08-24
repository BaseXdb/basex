package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Map with string keys and double values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQStrDblMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(BasicType.STRING, Types.DOUBLE_O);
  /** Hash map. */
  private final TokenDblMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQStrDblMap(final int capacity) {
    super(TYPE);
    map = new TokenDblMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = map.index(key.string(null));
      if(i != 0) return valueAt(i - 1);
    }
    return null;
  }

  @Override
  public Value keys() {
    return StrSeq.get(map.keys());
  }

  @Override
  public Value items(final QueryContext qc) {
    final int ls = (int) structSize();
    final double[] list = new double[ls];
    for(int l = 0; l < ls; l++) list[l] = map.value(l + 1);
    return DblSeq.get(list);
  }

  @Override
  public Str keyAt(final long index) {
    return Str.get(map.key((int) index + 1));
  }

  @Override
  public Dbl valueAt(final long index) {
    return Dbl.get(map.value((int) index + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toStr(key);
    final Dbl v = toDbl(value);
    if(k != null) {
      if(v != null) {
        map.put(k, v.dbl());
        return this;
      }
      return new XQStrValueMap(map.capacity() - 2).build(this).build(key, value);
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }
}
