package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Map with a single entry.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQSingletonMap extends XQMap {
  /** Key. */
  private final Item k;
  /** Value. */
  private final Value v;

  /**
   * Constructor.
   * @param key key
   * @param value value
   */
  XQSingletonMap(final Item key, final Value value) {
    super(MapType.get(key.type, value.seqType()));
    k = key;
    v = value;
  }

  @Override
  public long structSize() {
    return 1;
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    return key.atomicEqual(k) ? v : null;
  }

  @Override
  public Item keyAt(final int index) {
    return k;
  }

  @Override
  public Value valueAt(final int index) {
    return v;
  }

  @Override
  public XQMap put(final Item key, final Value value) throws QueryException {
    return key.atomicEqual(k) ? new XQSingletonMap(k, value) : empty().put(k, v).put(key, value);
  }

  @Override
  public XQMap putAt(final int index, final Value value) throws QueryException {
    return put(k, value);
  }

  @Override
  public XQMap remove(final Item key) throws QueryException {
    return key.atomicEqual(k) ? empty() : this;
  }

  @Override
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    func.accept(k, v);
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    return func.test(k, v);
  }

  @Override
  public BasicIter<Item> keys() {
    return k.iter();
  }
}
