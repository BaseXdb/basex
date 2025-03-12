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
public final class XQTokenMap extends XQHashMap {
  /** Initial capacity. */
  private final long capacity;
  /** Hash map. */
  private final TokenMap map;

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQTokenMap(final long capacity) {
    this.capacity = capacity;
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
    if(key.type == AtomType.STRING) {
      if(value.seqType().eq(SeqType.STRING_O)) {
        map.put(key.string(null), ((Item) value).string(null));
        return this;
      }
      return new XQTokenObjMap(capacity).build(this).build(key, value);
    }
    return new XQItemObjMap(capacity).build(this).build(key, value);
  }
}
