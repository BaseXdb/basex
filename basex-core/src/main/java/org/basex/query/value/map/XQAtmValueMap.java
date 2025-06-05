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

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  XQAtmValueMap(final int capacity) {
    super(TYPE);
    map = new TokenObjectMap<>(capacity);
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
  public Value keys() {
    return StrSeq.get(map.keys(), AtomType.UNTYPED_ATOMIC);
  }

  @Override
  public Atm keyAt(final int index) {
    return Atm.get(map.key(index + 1));
  }

  @Override
  public Value valueAt(final int index) {
    return map.value(index + 1);
  }

  @Override
  void valueAt(final int index, final Value value) {
    map.value(index + 1, value);
  }

  @Override
  XQHashMap build(final Item key, final Value value) throws QueryException {
    final byte[] k = toAtm(key);
    if(k != null) {
      map.put(k, value);
      return this;
    }
    return new XQItemValueMap(map.capacity() - 2).build(this).build(key, value);
  }

  @Override
  public Value shrink(final QueryContext qc) throws QueryException {
    // see MapBuilder#put for types with compact representation
    shrinkValues(qc);
    refineType();
    final SeqType vt = ((MapType) type).valueType();
    return vt.one() && vt.type.oneOf(AtomType.INTEGER, AtomType.STRING) ? rebuild(qc) : this;
  }
}
