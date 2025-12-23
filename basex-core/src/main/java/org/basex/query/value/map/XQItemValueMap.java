package org.basex.query.value.map;

import org.basex.core.jobs.*;
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
    super(Types.MAP);
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
  public Value keys() {
    return ItemSeq.get(map.keys(), (int) structSize(), ((MapType) type).keyType());
  }

  @Override
  public Item keyAt(final int index) {
    return map.key(index + 1);
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
    map.put(key, value);
    return this;
  }

  @Override
  public Item shrink(final Job job) throws QueryException {
    shrinkValues(job);
    refineType();
    // see MapBuilder#put for types with compact representation
    return ((MapType) type).keyType().oneOf(AtomType.INTEGER, AtomType.STRING,
        AtomType.UNTYPED_ATOMIC) ? rebuild(job) : this;
  }
}
