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
public final class XQItemObjMap extends XQHashMap {
  /** Hash map. */
  private final ItemObjMap<Value> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQItemObjMap(final long capacity) {
    super(capacity, SeqType.MAP);
    map = new ItemObjMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return map.get(key);
  }

  @Override
  Value keysInternal() {
    return ItemSeq.get(map.keys(), (int) structSize(), ((MapType) type).keyType());
  }

  @Override
  Item keyInternal(final int pos) {
    return map.key(pos);
  }

  @Override
  Value valueInternal(final int pos) {
    return map.value(pos);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    map.put(key, value);
    return this;
  }
}
