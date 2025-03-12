package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Unmodifiable hash map implementation for tokens.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQTokenObjMap extends XQHashMap {
  /** Initial capacity. */
  private final long capacity;
  /** Hash map. */
  private final TokenObjMap<Value> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQTokenObjMap(final long capacity) {
    this.capacity = capacity;
    map = new TokenObjMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int id = map.id(key.string(null));
      if(id != 0) return valueInternal(id);
    }
    return null;
  }

  @Override
  Value keysInternal() {
    return StrSeq.get(map.toArray());
  }

  @Override
  Str keyInternal(final int pos) {
    return Str.get(map.key(pos));
  }

  @Override
  Value valueInternal(final int pos) {
    return map.value(pos);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    if(key.type == AtomType.STRING) {
      map.put(key.string(null), value);
      return this;
    }
    return new XQItemObjMap(capacity).build(this).build(key, value);
  }
}
