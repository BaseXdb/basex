package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
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

  /**
   * Constructor.
   * @param root root node
   */
  XQTrieMap(final TrieNode root) {
    this.root = root;
  }

  @Override
  Value getInternal(final Item key) throws QueryException {
    return root.get(key.hashCode(), key, 0);
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    final TrieUpdate update = new TrieUpdate(key, value);
    final TrieNode node = root.put(key.hashCode(), 0, update);
    return node == root ? this : new XQTrieMap(node);
  }

  @Override
  public XQMap deleteInternal(final Item key) throws QueryException {
    final TrieUpdate update = new TrieUpdate(key, null);
    final TrieNode node = root.delete(key.hashCode(), 0, update);
    return node == root ? this : node != null ? new XQTrieMap(node) : null;
  }

  @Override
  public long structSize() {
    return root.size;
  }

  @Override
  public void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    root.apply(func);
  }

  @Override
  public boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    return root.test(func);
  }

  @Override
  Item[] keysInternal() throws QueryException {
    final ItemList items = new ItemList(structSize());
    apply((key, value) -> items.add(key));
    return items.finish();
  }

  @Override
  public boolean deepEqual(final XQMap map, final DeepEqual deep) throws QueryException {
    return root.equal(((XQTrieMap) map).root, deep);
  }
}
