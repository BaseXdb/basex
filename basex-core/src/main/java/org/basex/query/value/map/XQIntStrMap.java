package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Unmodifiable hash map implementation for integers and values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQIntStrMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, SeqType.STRING_O);
  /** Hash map. */
  private final IntObjectMap<byte[]> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntStrMap(final long capacity) {
    super(capacity, TYPE);
    map = new IntObjectMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    if(key instanceof ANum) {
      final double d = key.dbl(null);
      final int v = (int) d;
      if(d == v) {
        final int i = map.index(v);
        if(i != 0) return valueInternal(i);
      }
    }
    return null;
  }

  @Override
  Value keysInternal() {
    final long is = structSize();
    final LongList list = new LongList(is);
    for(int i = 1; i <= is; i++) list.add(map.key(i));
    return IntSeq.get(list.finish());
  }

  @Override
  Item keyInternal(final int pos) {
    return Int.get(map.key(pos));
  }

  @Override
  Value valueInternal(final int pos) {
    return Str.get(map.value(pos));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key);
    final byte[] v = toString(value);
    if(k != Integer.MIN_VALUE) {
      if(v != null) {
        map.put(k, v);
        return this;
      }
      return new XQIntValueMap(capacity).build(this).build(key, value);
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
