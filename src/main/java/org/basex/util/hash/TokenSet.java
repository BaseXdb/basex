package org.basex.util.hash;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This is an efficient hash set, storing keys in byte arrays.
 * The {@link TokenMap} class extends it to a hash map.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class TokenSet implements Iterable<byte[]> {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Hash entries. Note: actual number of entries is {@code size - 1}. */
  protected int size = 1;
  /** Hashed keys. */
  protected byte[][] keys;

  /** Pointers to the next token. */
  private int[] next;
  /** Hash table buckets. */
  private int[] bucket;

  /**
   * Constructor.
   */
  public TokenSet() {
    keys = new byte[CAP][];
    next = new int[CAP];
    bucket = new int[CAP];
  }

  /**
   * Constructor.
   * @param init initial tokens
   */
  public TokenSet(final byte[]... init) {
    this();
    for(final byte[] i : init) add(i);
  }

  /**
   * Constructor.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public TokenSet(final DataInput in) throws IOException {
    read(in);
  }

  /**
   * Reads the token set from the specified input.
   * @param in input stream
   * @throws IOException I/O exception
   */
  protected void read(final DataInput in) throws IOException {
    keys = in.readTokens();
    next = in.readNums();
    bucket = in.readNums();
    size = in.readNum();
  }

  /**
   * Writes the token set to the specified output.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public void write(final DataOutput out) throws IOException {
    out.writeTokens(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNum(size);
  }

  /**
   * Indexes the specified key and returns the offset of the added key.
   * If the key already exists, a negative offset is returned.
   * @param key key
   * @return offset of added key, negative offset otherwise
   */
  public final int add(final byte[] key) {
    if(size == next.length) rehash();
    final int p = hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(eq(key, keys[id])) return -id;
    }
    next[size] = bucket[p];
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  /**
   * Deletes the specified key.
   * <b>Warning</b>: After a deletion, the key array will have {@code null}
   * entries, and the total number of entries will not reflect the number
   * of valid entries anymore.
   * @param key key
   * @return deleted key or 0
   */
  public int delete(final byte[] key) {
    final int p = hash(key) & bucket.length - 1;
    int o = 0, n;
    for(int id = bucket[p]; id != 0; id = n) {
      n = next[id];
      if(eq(key, keys[id])) {
        if(bucket[p] == id) bucket[p] = n;
        else next[o] = next[n];
        keys[id] = null;
        return id;
      }
      o = id;
    }
    return 0;
  }

  /**
   * Returns the id of the specified key or 0 if the key does not exist.
   * @param key key to be found
   * @return id or 0 if nothing was found
   */
  public final int id(final byte[] key) {
    final int p = hash(key) & bucket.length - 1;
    for(int id = bucket[p]; id != 0; id = next[id]) {
      if(eq(key, keys[id])) return id;
    }
    return 0;
  }

  /**
   * Returns the specified key.
   * @param i key index
   * @return key
   */
  public final byte[] key(final int i) {
    return keys[i];
  }

  /**
   * Returns the hash keys.
   * @return keys
   */
  public final byte[][] keys() {
    final byte[][] tmp = new byte[size()][];
    System.arraycopy(keys, 1, tmp, 0, size - 1);
    return tmp;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
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
    for(int i = 0; i != l; ++i) {
      int id = bucket[i];
      while(id != 0) {
        final int p = hash(keys[id]) & s - 1;
        final int nx = next[id];
        next[id] = tmp[p];
        tmp[p] = id;
        id = nx;
      }
    }
    bucket = tmp;
    next = Arrays.copyOf(next, s);
    final byte[][] k = new byte[s][];
    System.arraycopy(keys, 0, k, 0, size);
    keys = k;
  }

  @Override
  public final Iterator<byte[]> iterator() {
    return new Iterator<byte[]>() {
      private int c = 1;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public byte[] next() { return keys[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    return new TokenBuilder(Util.name(this)).add('[').addSep(keys(),
        ", ").add(']').toString();
  }
}
