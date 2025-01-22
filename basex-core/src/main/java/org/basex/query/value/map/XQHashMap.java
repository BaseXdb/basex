package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
public final class XQHashMap extends XQMap {
  /** Hash map. */
  private final ItemObjectMap<Value> map;
  /** Cached immutable variant, for updates. */
  private XQMap trie;

  /**
   * Constructor.
   * @param map hash map
   */
  XQHashMap(final ItemObjectMap<Value> map) {
    this.map = map;
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return map.get(key);
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    return trie().putInternal(key, value);
  }

  @Override
  public XQMap removeInternal(final Item key) throws QueryException {
    return trie().removeInternal(key);
  }

  @Override
  public long structSize() {
    return map.size();
  }

  @Override
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final int is = map.size();
    for(int i = 1; i <= is; i++) func.accept(map.key(i), map.value(i));
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final int is = map.size();
    for(int i = 1; i <= is; i++) {
      if(!func.test(map.key(i), map.value(i))) return false;
    }
    return true;
  }

  @Override
  public BasicIter<Item> keys() {
    return new BasicIter<>(map.size()) {
      @Override
      public Item get(final long i) {
        return map.key((int) i + 1);
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(map.keys(), (int) size, ((MapType) type).keyType);
      }
    };
  }

  @Override
  public boolean deepEqual(final XQMap compare, final DeepEqual deep) throws QueryException {
    final ItemObjectMap<Value> map2 = ((XQHashMap) compare).map;
    final int is = map.size();
    for(int i = 1; i <= is; i++) {
      final Item key = map.key(i), key2 = map2.key(i);
      final Value value = map.value(i), value2 = map2.value(i);
      if(deep != null) {
        // different order: call fallback
        if(!(key.atomicEqual(key2) && deep.equal(value, value2))) return deepEq(compare, deep);
      } else {
        if(!(key.equals(key2) && value.equals(value2))) return false;
      }
    }
    return true;
  }

  /**
   * Transforms the map to an immutable representation.
   * @return map
   * @throws QueryException query exception
   */
  private XQMap trie() throws QueryException {
    if(trie == null) {
      XQMap mp = XQMap.empty();
      final int is = map.size();
      for(int i = 1; i <= is; i++) {
        mp = mp.put(map.key(i), map.value(i));
      }
      trie = mp;
    }
    return trie;
  }
}
