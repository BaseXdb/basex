package org.basex.query.value.map;

import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Empty {@link XQMap} node.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class TrieEmpty extends TrieNode {
  /** The empty node. */
  static final TrieNode VALUE = new TrieEmpty();

  /**
   * Private constructor.
   */
  private TrieEmpty() {
    super(0);
  }

  @Override
  public TrieNode put(final int hs, final int lv, final TrieUpdate update) {
    return new TrieLeaf(hs, update.key, update.value);
  }

  @Override
  TrieNode remove(final int hs, final int lv, final TrieUpdate update) {
    return this;
  }

  @Override
  Value get(final int hs, final Item ky, final int lv) {
    return null;
  }

  @Override
  boolean equal(final TrieNode node, final DeepEqual deep) {
    return this == node;
  }

  @Override
  void add(final TokenBuilder tb, final String indent) {
    tb.add("{}");
  }
}
