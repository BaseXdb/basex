package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Hash array mapped trie implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class XQTrieMap extends XQMap {
  /** Root node. */
  private final TrieNode root;
  /** Map order ({@code null} for empty and singleton maps). */
  private TrieOrder order;

  /**
   * Constructor.
   * @param root root node
   * @param order map order (can be ({@code null})
   */
  XQTrieMap(final TrieNode root, final TrieOrder order) {
    this.root = root;
    this.order = order;
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return root.get(key.hashCode(), key, 0);
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    final TrieUpdate update = new TrieUpdate(key, value, order);
    final TrieNode node = root.put(key.hashCode(), 0, update);
    return new XQTrieMap(node, update.newOrder);
  }

  @Override
  public XQMap removeInternal(final Item key) throws QueryException {
    final TrieUpdate update = new TrieUpdate(key, null, order);
    final TrieNode node = root.remove(key.hashCode(), 0, update);
    return node == root ? this : node != null ? new XQTrieMap(node, update.newOrder) : null;
  }

  @Override
  public long structSize() {
    return root.size;
  }

  @Override
  public void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    for(final Item key : keysInternal()) {
      func.accept(key, get(key));
    }
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    for(final Item key : keysInternal()) {
      if(!func.test(key, get(key))) return false;
    }
    return true;
  }

  @Override
  Item[] keysInternal() throws QueryException {
    final long s = structSize();
    return s == 0 ? new Item[0] : s == 1 ? new Item[] { ((TrieLeaf) root).key } : order.keys();
  }

  @Override
  public boolean deepEqual(final XQMap map, final DeepEqual deep) throws QueryException {
    return root.equal(((XQTrieMap) map).root, deep);
  }
}
