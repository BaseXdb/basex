package org.basex.query.value.map;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * A single binding of a {@link XQMap}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class TrieLeaf extends TrieNode {
  /** Hash code of the key, stored for performance. */
  final int hash;
  /** Key of this binding. */
  final Item key;
  /** Value of this binding. */
  final Value value;

  /**
   * Constructor.
   * @param hash hash code of the key
   * @param key key
   * @param value value
   */
  TrieLeaf(final int hash, final Item key, final Value value) {
    super(1);
    this.hash = hash;
    this.key = key;
    this.value = value;
  }

  @Override
  TrieNode put(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    // same hash
    final boolean same = hs == hash;
    // same key: create new leaf
    if(same && key.atomicEqual(update.key)) return new TrieLeaf(hs, key, update.value);
    // different key: create list of values or branch
    update.add(key);
    return same ? new TrieList(hs, key, value, update.key, update.value) :
      branch(hs, lv, hash, 1, update);
  }

  @Override
  TrieNode remove(final int hs, final int lv, final TrieUpdate update) throws QueryException {
    if(hs == hash && key.atomicEqual(update.key)) {
      update.remove(key);
      return null;
    }
    return this;
  }

  @Override
  Value get(final int hs, final Item ky, final int lv) throws QueryException {
    return hs == hash && key.atomicEqual(ky) ? value : null;
  }

  @Override
  boolean equal(final TrieNode node, final DeepEqual deep) throws QueryException {
    if(node instanceof TrieLeaf) {
      final TrieLeaf leaf = (TrieLeaf) node;
      return deep != null ? key.atomicEqual(leaf.key) && deep.equal(value, leaf.value) :
        key.equals(leaf.key) && value.equals(leaf.value);
    }
    return false;
  }

  @Override
  void add(final TokenBuilder tb, final String indent) {
    tb.add(indent).add("`-- ").add(key).add(" => ").add(value).add('\n');
  }
}
