package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Hash array mapped trie implementation.
 *
 * @author BaseX Team, BSD License
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
  public void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    for(final Item key : keys()) {
      func.accept(key, get(key));
    }
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    for(final Item key : keys()) {
      if(!func.test(key, get(key))) return false;
    }
    return true;
  }

  @Override
  public BasicIter<Item> keys() throws QueryException {
    final long size = structSize();
    return size == 0 ? Empty.ITER : size == 1 ? ((TrieLeaf) root).key.iter() : order.keys();
  }

  @Override
  public boolean deepEqual(final XQMap map, final DeepEqual deep) throws QueryException {
    return root.equal(((XQTrieMap) map).root, deep);
  }
}
