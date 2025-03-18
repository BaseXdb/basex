package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Unmodifiable hash map implementation for strings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQTokenMap extends XQHashMap {
  /** Map type. */
  private static final MapType TYPE = MapType.get(AtomType.STRING, SeqType.STRING_O);
  /** Hash map. */
  private final TokenMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQTokenMap(final long capacity) {
    super(capacity, TYPE);
    map = new TokenMap(capacity);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  Str getInternal(final Item key) throws QueryException {
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
  Str valueInternal(final int pos) {
    return Str.get(map.value(pos));
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toString(key), v = toString(value);
    if(k != null) {
      if(v != null) {
        map.put(k, v);
        return this;
      }
      return new XQTokenObjMap(capacity).build(this).build(key, value);
    }
    return new XQItemObjMap(capacity).build(this).build(key, value);
  }
}
