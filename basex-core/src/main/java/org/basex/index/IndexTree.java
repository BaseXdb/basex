package org.basex.index;

import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class indexes keys in a hash set, including their ID values, and returns them in sorted
 * order.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class IndexTree {
  /** Factor for resize. */
  private static final double FACTOR = 1.2;
  /** Estimated overhead of an array object. */
  private static final int ARRAY = 20;
  /** Estimated size of a hash slot (bucket, chain, key reference). */
  private static final int SLOT = 16;

  /** Compressed ID values (and token positions for token and full-text index). */
  public final TokenList ids = new TokenList(FACTOR);

  /** Indexed keys. */
  private final TokenSet keys;
  /** Store token positions. */
  private final boolean tokenize;
  /** Sorted key offsets (assigned by {@link #init()}). */
  private int[] order;
  /** Current iterator position. */
  private int cn;
  /** Estimated memory consumption (bytes). */
  private long memory;

  /**
   * Constructor.
   * @param type index type
   */
  public IndexTree(final IndexType type) {
    this(type, 0);
  }

  /**
   * Constructor with an estimated number of distinct keys.
   * @param type index type
   * @param capacity estimated number of distinct keys
   */
  public IndexTree(final IndexType type, final int capacity) {
    tokenize = type == IndexType.TOKEN || type == IndexType.FULLTEXT;
    keys = new TokenSet(capacity);
  }

  /**
   * Indexes the specified key and ID. If the key has already been
   * indexed, its ID is appended to the existing array.
   * Otherwise, a new index entry is created.
   * @param key key to be indexed
   * @param id ID to be indexed
   * @param pos token position (only relevant for token and full-text index)
   */
  public void add(final byte[] key, final int id, final int pos) {
    final int n = keys.put(key) - 1;
    final byte[] old = n < ids.size() ? ids.get(n) : null;
    byte[] vs = old != null ? Num.add(old, id) : Num.newNum(id);
    if(tokenize) vs = Num.add(vs, pos);
    if(old != null) {
      ids.set(n, vs);
      memory += vs.length - old.length;
    } else {
      ids.add(vs);
      memory += ARRAY + key.length + ARRAY + vs.length;
    }
  }

  /**
   * Returns the key at the specified offset.
   * @param n offset
   * @return key
   */
  public byte[] key(final int n) {
    return keys.key(n + 1);
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return ids.size();
  }

  /**
   * Returns the estimated memory consumption of the tree.
   * @return memory consumption in bytes
   */
  public long memory() {
    return memory + (long) keys.capacity() * SLOT;
  }

  /**
   * Initializes the index iterator.
   */
  public void init() {
    if(order == null) order = Array.createOrder(keys.keys(), false, true);
    cn = 0;
  }

  /**
   * Checks if the iterator returns more keys.
   * @return true if more keys exist
   */
  public boolean more() {
    return cn < order.length;
  }

  /**
   * Returns the next pointer.
   * @return next pointer
   */
  public int next() {
    return order[cn++];
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(Util.className(this)).add('[').add(Prop.NL);
    final int size = size();
    for(int c = 0; c < size; c++) {
      tb.add("  \"").add(key(c)).add("\": ").add("ids");
      if(tokenize) tb.add("/pos");
      tb.add(": (").add(Num.toString(ids.get(c))).add(')').add(Prop.NL);
    }
    tb.add("]");
    return tb.toString();
  }
}
