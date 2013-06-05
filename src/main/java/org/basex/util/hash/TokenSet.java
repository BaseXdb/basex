package org.basex.util.hash;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash set for storing tokens.
 * The first entry of the token set (offset 0) is always empty.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class TokenSet implements Iterable<byte[]> {
  /** Hash entries. The actual number of entries is {@code size - 1}. */
  protected int size = 1;
  /** Hashed keys. */
  protected byte[][] keys;

  /** Pointers to the next token. */
  private int[] next;
  /** Hash table buckets. */
  private int[] bucket;

  /**
   * Default constructor.
   */
  public TokenSet() {
    keys = new byte[Array.CAPACITY][];
    next = new int[Array.CAPACITY];
    bucket = new int[Array.CAPACITY];
  }

  /**
   * Constructor, specifying an initial key.
   * @param key initial key
   */
  public TokenSet(final byte[] key) {
    this();
    add(key);
  }

  /**
   * Constructor, specifying initial keys.
   * @param key initial keys
   */
  public TokenSet(final byte[]... key) {
    this();
    for(final byte[] i : key) add(i);
  }

  /**
   * Constructor, specifying some initial input.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public TokenSet(final DataInput in) throws IOException {
    read(in);
  }

  /**
   * Reads the data structure from the specified input.
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
   * Writes the data structure to the specified output.
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
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@Code true} if the key did not exist yet and was stored
   */
  public final boolean add(final byte[] key) {
    return index(key) > 0;
  }

  /**
   * Stores the specified string as key if it has not been stored before.
   * @param key string to be added
   * @return {@Code true} if the key did not exist yet and was stored
   */
  public boolean add(final String key) {
    return add(token(key));
  }

  /**
   * Stores the specified key and returns its id.
   * @param key key to be added
   * @return unique id of stored key (larger than zero)
   */
  public final int put(final byte[] key) {
    final int i = index(key);
    return Math.abs(i);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final byte[] key) {
    return id(key) > 0;
  }

  /**
   * Returns the id of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return id, or {@code 0} if key does not exist
   */
  public final int id(final byte[] key) {
    final int p = hash(key) & bucket.length - 1;
    for(int i = bucket[p]; i != 0; i = next[i]) if(eq(key, keys[i])) return i;
    return 0;
  }

  /**
   * Returns the key with the specified id.
   * All ids starts with {@code 1} instead of {@code 0}.
   * @param id id of the key to return
   * @return key
   */
  public final byte[] key(final int id) {
    return keys[id];
  }

  /**
   * Returns the number of entries.
   * The actual number of keys may be smaller if keys have been deleted.
   * @return number of entries
   */
  public final int size() {
    return size - 1;
  }

  /**
   * Resets the data structure.
   */
  public final void clear() {
    Arrays.fill(keys, null);
    Arrays.fill(bucket, 0);
    size = 1;
  }

  /**
   * Tests is the set is empty.
   * @return result of check
   */
  public final boolean isEmpty() {
    return size == 1;
  }

  /**
   * Deletes the specified key.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
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
   * Resizes the hash table.
   */
  protected void rehash() {
    final int s = size << 1;
    final int[] tmp = new int[s];

    for(final int b : bucket) {
      int id = b;
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

  /**
   * Stores the specified key and returns its id, or returns the negative id if the
   * key has already been stored.
   * @param key key to be found
   * @return id, or negative id if key has already been stored
   */
  private int index(final byte[] key) {
    if(size == next.length) rehash();
    final int p = hash(key) & bucket.length - 1;
    for(int i = bucket[p]; i != 0; i = next[i]) if(eq(key, keys[i])) return -i;
    next[size] = bucket[p];
    keys[size] = key;
    bucket[p] = size;
    return size++;
  }

  @Override
  public final Iterator<byte[]> iterator() {
    return new ArrayIterator<byte[]>(keys, 1, size);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 1; i < size; i++) {
      if(!tb.isEmpty()) tb.add(", ");
      if(keys[i] != null) tb.add(keys[i]);
    }
    return new TokenBuilder(Util.name(getClass())).add('[').add(tb.finish()).
        add(']').toString();
  }
}
