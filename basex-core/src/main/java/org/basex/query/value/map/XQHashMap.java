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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class XQHashMap extends XQMap {
  /** Hash map. */
  private final ItemValueMap ivm;
  /** Cached immutable variant, for updates. */
  private XQMap trie;

  /**
   * Constructor.
   * @param ivm hash map
   */
  XQHashMap(final ItemValueMap ivm) {
    this.ivm = ivm;
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return ivm.get(key);
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
    return ivm.size();
  }

  @Override
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final int is = ivm.size();
    for(int i = 1; i <= is; i++) func.accept(ivm.key(i), ivm.value(i));
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final int is = ivm.size();
    for(int i = 1; i <= is; i++) {
      if(!func.test(ivm.key(i), ivm.value(i))) return false;
    }
    return true;
  }

  @Override
  public BasicIter<Item> keys() throws QueryException {
    return new BasicIter<>(ivm.size()) {
      @Override
      public Item get(final long i) {
        return ivm.key((int) i + 1);
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return ItemSeq.get(ivm.keys(), (int) size, ((MapType) type).keyType);
      }
    };
  }

  @Override
  public boolean deepEqual(final XQMap map, final DeepEqual deep) throws QueryException {
    final ItemValueMap ivm2 = ((XQHashMap) map).ivm;
    final int is = ivm.size();
    for(int i = 1; i <= is; i++) {
      final Item key = ivm.key(i), key2 = ivm2.key(i);
      final Value value = ivm.value(i), value2 = ivm2.value(i);
      // no success: call fallback
      if(!(deep != null ?
        key.atomicEqual(key2) && deep.equal(value, value2) :
        key.equals(key2) && value.equals(value2)
      )) return deepEq(map, deep);
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
      XQMap map = XQMap.empty();
      final int is = ivm.size();
      for(int i = 1; i <= is; i++) {
        map = map.put(ivm.key(i), ivm.value(i));
      }
      trie = map;
    }
    return trie;
  }
}
