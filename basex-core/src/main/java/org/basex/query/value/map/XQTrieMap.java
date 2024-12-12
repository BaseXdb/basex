package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

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
    return root.get(key.hash(), key, 0);
  }

  @Override
  XQMap putInternal(final Item key, final Value value) throws QueryException {
    final TrieNode node = root.put(key.hash(), key, value, 0);
    return node == root ? this : new XQTrieMap(node);
  }

  @Override
  XQMap mergeInternal(final XQMap map, final MergeDuplicates merge, final QueryContext qc,
      final InputInfo ii) throws QueryException {

    TrieNode node = root;
    if(map instanceof XQTrieMap) {
      final TrieNode tnode = ((XQTrieMap) map).root;
      node = node.merge(tnode, 0, merge, qc, ii);
      if(node == tnode) return map;
    } else {
      for(final Item key : map.keys()) {
        node = node.merge(new TrieLeaf(key.hash(), key, map.get(key)), 0, merge, qc, ii);
      }
    }
    return node == root ? this : new XQTrieMap(node);
  }

  @Override
  public XQMap deleteInternal(final Item key) throws QueryException {
    final TrieNode node = root.delete(key.hash(), key, 0);
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
  public boolean deepEqual(final XQMap map, final DeepEqual deep) throws QueryException {
    return root.equal(((XQTrieMap) map).root, deep);
  }
}
