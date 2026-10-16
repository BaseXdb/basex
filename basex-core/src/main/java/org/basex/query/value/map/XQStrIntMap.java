package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Map with string keys and integer values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQStrIntMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(BasicType.STRING, Types.INTEGER_O);
  /** Hash map. */
  private final TokenIntMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQStrIntMap(final int capacity) {
    super(TYPE);
    map = new TokenIntMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public int index(final Item key) throws QueryException {
    return index(map, key);
  }

  @Override
  public Value keys() {
    return StrSeq.get(map.keys());
  }

  @Override
  public Value items(final QueryContext qc) {
    final int ls = (int) structSize();
    final int[] list = new int[ls];
    for(int l = 0; l < ls; l++) list[l] = map.value(l + 1);
    return IntSeq.get(list);
  }

  @Override
  public Str keyAt(final long index) {
    return Str.get(map.key((int) index + 1));
  }

  @Override
  public Itr valueAt(final long index) {
    return Itr.get(map.value((int) index + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toStr(key);
    final int v = toInt(value);
    if(k != null) {
      if(v != Integer.MIN_VALUE) {
        map.put(k, v);
        return this;
      }
      return new XQStrValueMap(map.capacity() - 2).build(this).build(key, value);
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }
}
