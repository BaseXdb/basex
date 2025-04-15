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
public final class XQAtmValueMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.UNTYPED_ATOMIC, SeqType.ITEM_ZM);
  /** Hash map. */
  private final TokenObjectMap<Value> map;
  /** Initial capacity. */
  private final int capacity;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQAtmValueMap(final int capacity) {
    super(TYPE);
    map = new TokenObjectMap<>(capacity);
    this.capacity = capacity;
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    if(key.type.isStringOrUntyped()) {
      final int i = map.index(key.string(null));
      if(i != 0) return valueAt(i - 1);
    }
    return null;
  }

  @Override
  Value keysInternal() {
    return StrSeq.get(map.keys(), AtomType.UNTYPED_ATOMIC);
  }

  @Override
  public Atm keyAt(final int pos) {
    return Atm.get(map.key(pos + 1));
  }

  @Override
  public Value valueAt(final int pos) {
    return map.value(pos + 1);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toAtm(key);
    if(k != null) {
      map.put(k, value);
      return this;
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
