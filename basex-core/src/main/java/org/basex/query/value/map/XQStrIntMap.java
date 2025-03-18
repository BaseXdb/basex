package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Unmodifiable hash map implementation for strings and integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQStrIntMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.STRING, SeqType.INTEGER_O);
  /** Hash map. */
  private final TokenIntMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQStrIntMap(final long capacity) {
    super(capacity, TYPE);
    map = new TokenIntMap(capacity);
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
  Int valueInternal(final int pos) {
    return Int.get(map.value(pos));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toString(key);
    final int v = toInt(value);
    if(k != null) {
      if(v != Integer.MIN_VALUE) {
        map.put(k, v);
        return this;
      }
      return new XQStrValueMap(capacity).build(this).build(key, value);
    }
    return new XQItemValueMap(capacity).build(this).build(key, value);
  }
}
