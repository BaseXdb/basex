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
  XQIntMap(final int capacity) {
    super(TYPE);
    map = new IntMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Itr getOrNull(final Item key) {
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
    final long is = structSize();
    final IntList list = new IntList(is);
    for(int i = 1; i <= is; i++) list.add(map.value(i));
    return IntSeq.get(list.finish());
  }

  @Override
  public Itr keyAt(final int index) {
    return Itr.get(map.key(index + 1));
  }

  @Override
  public Itr valueAt(final int index) {
    return Itr.get(map.value(index + 1));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final int k = toInt(key), v = toInt(value);
    if(k != Integer.MIN_VALUE) {
      if(v != Integer.MIN_VALUE) {
        map.put(k, v);
        return this;
      }
      return new XQIntValueMap(map.capacity() - 2).build(this).build(key, value);
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }
}
