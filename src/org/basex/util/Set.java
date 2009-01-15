package org.basex.util;

/**
 * This is a simple hash set, storing keys in byte arrays.
 * The {@link TokenMap} class extends it to a hash map.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class Set {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Hash keys. */
  protected byte[][] keys;
  /** Pointers to the next token. */
  protected int[] next;
  /** Hash table buckets. */
  protected int[] bucket;
  /** Hash entries. Actual hash size is <code>size - 1</code>. */
  protected int size;

  /**
   * Empty Constructor.
   */
  public Set() {
    keys = new byte[CAP][];
    next = new int[CAP];
    bucket = new int[CAP];
    size = 1;
  }

  /**
   * Indexes the specified key.
   * @param key key
   * @return offset of added key, negative offset otherwise
   */
  public int add(final byte[] key) {
    if(size == next.length) rehash();
    final int p = Token.hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(Token.eq(key, keys[id])) return -id;
    }

    next[size] = bucket[p];
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  /**
   * Deletes the specified key.
   * @param key key
   * @return deleted key or 0
   */
  public final int delete(final byte[] key) {
    final int p = Token.hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(Token.eq(key, keys[id])) {
        if(bucket[p] == id) bucket[p] = next[id];
        else next[id] = next[next[id]];
        keys[id] = null;
        return id;
      }
    }
    return 0;
  }

  /**
   * Returns the id of the specified key or 0 if key was not found.
   * @param key key to be found
   * @return id or 0 if nothing was found
   */
  public final int id(final byte[] key) {
    final int p = Token.hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(Token.eq(key, keys[id])) return id;
    }
    return 0;
  }

  /**
   * Returns the specified key.
   * @param p key index
   * @return key
   */
  public final byte[] key(final int p) {
    return keys[p];
  }

  /**
   * Returns the hash keys.
   * @return keys
   */
  public final byte[][] keys() {
    final byte[][] tmp = new byte[size()][];
    int t = 0;
    for(int i = 1; i < size; i++) tmp[t++] = keys[i];
    return tmp;
  }

  /**
   * Returns number of entries.
   * @return number of entries.
   */
  public final int size() {
    return size - 1;
  }

  /**
   * Resizes the hash table.
   */
  protected void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];

    final int l = bucket.length;
    for(int i = 0; i != l; i++) {
      int id = bucket[i];
      while(id != 0) {
        final int p = Token.hash(keys[id]) & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    bucket = tmp;
    next = Array.extend(next);
    keys = Array.extend(keys);
  }
}
