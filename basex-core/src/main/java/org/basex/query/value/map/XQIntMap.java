package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Unmodifiable hash map implementation for integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQIntMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, SeqType.INTEGER_O);
  /** Hash map. */
  private final IntMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntMap(final long capacity) {
    super(capacity, TYPE);
    map = new IntMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Int getInternal(final Item key) throws QueryException {
    if(key instanceof ANum) {
      final double d = key.dbl(null);
      final int v = (int) d;
      if(d == v) {
        final int i = map.index(v);
        if(i != 0) return valueAt(i - 1);
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
  public Int keyAt(final int pos) {
    return Int.get(map.key(pos + 1));
  }

  @Override
  public Int valueAt(final int pos) {
    return Int.get(map.value(pos + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key), v = toInt(value);
    if(k != Integer.MIN_VALUE) {
      if(v != Integer.MIN_VALUE) {
        map.put(k, v);
        return this;
      }
      return new XQIntValueMap(capacity).build(this).build(key, value);
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
