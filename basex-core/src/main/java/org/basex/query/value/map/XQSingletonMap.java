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
  private Item k;
  /** Value. */
  private Value v;

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
  Value getInternal(final Item key) throws QueryException {
    return key.atomicEqual(k) ? v : null;
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    return empty().put(k, v).put(key, value);
  }

  @Override
  public XQMap removeInternal(final Item key) throws QueryException {
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
