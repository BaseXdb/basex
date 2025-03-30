package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Unmodifiable hash map implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQItemValueMap extends XQHashMap {
  /** Hash map. */
  private final ItemObjectMap<Value> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQItemValueMap(final int capacity) {
    super(SeqType.MAP);
    map = new ItemObjectMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    return map.get(key);
  }

  @Override
  Value keysInternal() {
    return ItemSeq.get(map.keys(), (int) structSize(), ((MapType) type).keyType());
  }

  @Override
  public Item keyAt(final int pos) {
    return map.key(pos + 1);
  }

  @Override
  public Value valueAt(final int pos) {
    return map.value(pos + 1);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    map.put(key, value);
    return this;
  }
}
