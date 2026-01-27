package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Unmodifiable hash map implementation for integers and values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQIntStrMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, Types.STRING_O);
  /** Hash map. */
  private final IntObjectMap<byte[]> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntStrMap(final int capacity) {
    super(TYPE);
    map = new IntObjectMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) {
    if(key instanceof final ANum num) {
      final double d = num.dbl();
      final int v = (int) d;
      if(d == v) {
        final int i = map.index(v);
        if(i != 0) return valueAt(i - 1);
      }
    }
    return null;
  }

  @Override
  public Value keys() {
    return IntSeq.get(map.keys());
  }

  @Override
  public Value items(final QueryContext qc) {
    final int ls = (int) structSize();
    final byte[][] list = new byte[ls][];
    for(int l = 0; l < ls; l++) list[l] = map.value(l + 1);
    return StrSeq.get(list);
  }

  @Override
  public Item keyAt(final int index) {
    return Itr.get(map.key(index + 1));
  }

  @Override
  public Value valueAt(final int index) {
    return Str.get(map.value(index + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key);
    final byte[] v = toStr(value);
    if(k != Integer.MIN_VALUE) {
      if(v != null) {
        map.put(k, v);
        return this;
      }
      return new XQIntValueMap(map.capacity() - 2).build(this).build(key, value);
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }
}
