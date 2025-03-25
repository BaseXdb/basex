package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Unmodifiable hash map implementation for strings and values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQStrValueMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.STRING, SeqType.ITEM_ZM);
  /** Hash map. */
  private final TokenObjectMap<Value> map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQStrValueMap(final long capacity) {
    super(capacity, TYPE);
    map = new TokenObjectMap<>(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = map.index(key.string(null));
      if(i != 0) return valueInternal(i);
    }
    return null;
  }

  @Override
  Value keysInternal() {
    return StrSeq.get(map.keys());
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
    final byte[] k = toString(key);
    if(k != null) {
      map.put(k, value);
      return this;
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
