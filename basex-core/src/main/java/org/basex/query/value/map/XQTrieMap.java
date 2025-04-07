package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

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
  private final TrieOrder order;

  /**
   * Constructor.
   * @param root root node
   * @param order map order (can be ({@code null})
   * @param type map type
   */
  XQTrieMap(final TrieNode root, final TrieOrder order, final Type type) {
    super(type);
    this.root = root;
    this.order = order;
  }

  @Override
  public Value getOrNull(final Item key) throws QueryException {
    return root.get(key.hashCode(), key, 0);
  }

  @Override
  public Item keyAt(final int index) {
    return keys().get(index);
  }

  @Override
  public Value valueAt(final int index) {
    try {
      return getOrNull(keyAt(index));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public XQTrieMap put(final Item key, final Value value) throws QueryException {
    final long oldSize = structSize();
    if(oldSize == 0) return new XQTrieMap(new TrieLeaf(key.hashCode(), key, value), null,
          MapType.get(key.type, value.seqType()));

    final TrieUpdate update = new TrieUpdate(key, value, order);
    final TrieNode node = root.put(key.hashCode(), 0, update);
    if(node == root) return this;

    final TrieOrder to;
    final Type mt;
    if(node.size == 1) {
      // single entry: no map order, initialize type
      to = null;
      mt = MapType.get((((TrieLeaf) root).key).type, value.seqType());
    } else {
      // initialize map order if a second entry was added
      to = oldSize == 1 ? new TrieOrder(((TrieLeaf) root).key, key) : update.order();
      mt = ((MapType) type).union(key.type, value.seqType());
    }
    return new XQTrieMap(node, to, mt);
  }

  @Override
  public XQTrieMap putAt(final int index, final Value value) throws QueryException {
    return put(keyAt(index), value);
  }

  @Override
  public XQMap remove(final Item key) throws QueryException {
    final TrieUpdate update = new TrieUpdate(key, null, order);
    final TrieNode node = root.remove(key.hashCode(), 0, update);
    if(node == root) return this;
    if(node == null) return empty();

    // drop map order if a single entry is left
    final TrieOrder to = node.size == 1 ? null : update.order();
    return new XQTrieMap(node, to, type);
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
  public BasicIter<Item> keys() {
    final long size = structSize();
    return size == 0 ? Empty.ITER : size == 1 ? ((TrieLeaf) root).key.iter() : order.keys().iter();
  }
}
