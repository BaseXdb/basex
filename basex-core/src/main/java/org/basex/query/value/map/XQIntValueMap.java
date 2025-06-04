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
public final class XQIntValueMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.INTEGER, SeqType.ITEM_ZM);
  /** Hash map. */
  private final IntObjectMap<Value> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQIntValueMap(final int capacity) {
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
  public Item keyAt(final int pos) {
    return Int.get(map.key(pos + 1));
  }

  @Override
  public Value valueAt(final int pos) {
    return map.value(pos + 1);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key);
    if(k != Integer.MIN_VALUE) {
      map.put(k, value);
      return this;
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }
}
