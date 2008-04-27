package org.basex.index;

import java.io.IOException;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * This class provides a main-memory access to attribute values and
 * text contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MemValues extends Index {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Hash keys. */
  protected byte[][] keys;
  /** Pointers to the next token. */
  protected int[] next;
  /** Hash table buckets. */
  protected int[] bucket;
  /** IDs. */
  private int[][] ids = new int[CAP][];
  /** ID array lengths. */
  private int[] len = new int[CAP];
  /** Hash entries. Actual hash size is <code>size - 1</code>. */
  public int size;
  
  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param id id value
   * @return index position
   */
  public int index(final byte[] key, final int id) {
    int i = add(key);
    if(i > 0) {
      ids[i] = new int[] { id };
    } else {
      i = -i;
      final int l = len[i];
      if(ids[i].length == l) ids[i] = Array.extend(ids[i]);
      ids[i][l] = id;
    }
    len[i]++;
    return i;
  }

  /**
   * Indexes the specified key.
   * @param key key
   * @return offset of added key, negative offset otherwise
   */
  private int add(final byte[] key) {
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
   * Returns the id for the specified key.
   * @param key key
   * @return id (negative if value wasn't found)
   */
  public int get(final byte[] key) {
    return -add(key);
  }

  /**
   * Returns the token for the specified id.
   * @param id id
   * @return token
   */
  public byte[] token(final int id) {
    return keys[id];
  }
  
  @Override
  public int[] ids(final byte[] key) {
    final int i = id(key);
    return i == 0 ? Array.NOINTS : Array.finish(ids[i], len[i]);
  }

  /**
   * Returns the id of the specified key or 0 if key was not found.
   * @param key key to be found
   * @return id or 0 if nothing was found
   */
  private int id(final byte[] key) {
    final int p = Token.hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(Token.eq(key, keys[id])) return id;
    }
    return 0;
  }
  
  @Override
  public int nrIDs(final byte[] key) {
    return len[id(key)];
  }
  
  /**
   * Rehashes the hash contents.
   */
  private void rehash() {
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
    ids = Array.extend(ids);
    len = Array.extend(len);
  }

  @Override
  public void info(final PrintOutput out) throws IOException {
    out.print("MemValues");
  }
}
