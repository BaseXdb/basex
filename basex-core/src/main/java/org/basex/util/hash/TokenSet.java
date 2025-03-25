package org.basex.util.hash;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash set for storing tokens.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class TokenSet extends ASet implements Iterable<byte[]> {
  /** Hashed keys. */
  protected byte[][] keys;

  /**
   * Default constructor.
   */
  public TokenSet() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public TokenSet(final long capacity) {
    super(capacity);
    keys = new byte[capacity()][];
  }

  /**
   * Constructor with initial keys.
   * @param keys keys to be added
   */
  public TokenSet(final byte[]... keys) {
    this(keys.length);
    for(final byte[] key : keys) add(key);
  }

  /**
   * Convenience constructor with initial strings as keys.
   * @param keys keys to be added
   */
  public TokenSet(final String... keys) {
    this(keys.length);
    for(final String key : keys) add(key);
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
  public void read(final DataInput in) throws IOException {
    keys = in.readTokens();
    next = in.readNums();
    buckets = in.readNums();
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
    out.writeNums(buckets);
    out.writeNum(size);
  }

  /**
   * Stores the specified key if it has not been stored before.
   * @param key key to be added
   * @return {@code true} if the key did not exist yet and was stored
   */
  public final boolean add(final byte[] key) {
    return store(key) > 0;
  }

  /**
   * Stores the specified string as key if it has not been stored before.
   * @param key string to be added
   * @return {@code true} if the key did not exist yet and was stored
   */
  public final boolean add(final String key) {
    return add(Token.token(key));
  }

  /**
   * Stores the specified key and returns its index.
   * @param key key to be added
   * @return index of stored key (larger than {@code 0})
   */
  public final int put(final byte[] key) {
    final int index = store(key);
    return Math.abs(index);
  }

  /**
   * Checks if the set contains the specified key.
   * @param key key to be looked up
   * @return result of check
   */
  public final boolean contains(final byte[] key) {
    return index(key) > 0;
  }

  /**
   * Returns the index of the specified key, or {@code 0} if the key does not exist.
   * @param key key to be looked up
   * @return index, or {@code 0} if key does not exist
   */
  public final int index(final byte[] key) {
    final int b = Token.hashCode(key) & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(Token.eq(key, keys[i])) return i;
    }
    return 0;
  }

  /**
   * Returns the key with the specified index.
   * @param index index of the key (starts with {@code 1})
   * @return key
   */
  public final byte[] key(final int index) {
    return keys[index];
  }

  /**
   * Removes the entry with the specified key.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
   * @param key key
   * @return index of the deleted key, or {@code 0} if the key did not exist
   */
  public int remove(final byte[] key) {
    final int b = Token.hashCode(key) & capacity() - 1;
    for(int p = 0, i = buckets[b]; i != 0; p = i, i = next[i]) {
      if(!Token.eq(key, keys[i])) continue;
      if(p == 0) buckets[b] = next[i];
      else next[p] = next[next[p]];
      keys[i] = null;
      return i;
    }
    return 0;
  }

  /**
   * Stores the specified key and returns its index,
   * or returns the negative index if the key has already been stored.
   * @param key key to be indexed
   * @return index, or negative index if the key already exists
   */
  private int store(final byte[] key) {
    final int h = Token.hashCode(key);
    int b = h & capacity() - 1;
    for(int i = buckets[b]; i != 0; i = next[i]) {
      if(Token.eq(key, keys[i])) return -i;
    }
    final int s = size++;
    if(checkCapacity()) b = h & capacity() - 1;
    next[s] = buckets[b];
    keys[s] = key;
    buckets[b] = s;
    return s;
  }

  @Override
  protected final int hashCode(final int index) {
    return Token.hashCode(keys[index]);
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copyOf(keys, newSize);
  }

  @Override
  public void clear() {
    Arrays.fill(keys, null);
    super.clear();
  }

  @Override
  public final Iterator<byte[]> iterator() {
    return new ArrayIterator<>(keys, 1, size);
  }

  /**
   * Returns an array with all keys.
   * @return array
   */
  public final byte[][] keys() {
    return Arrays.copyOfRange(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }
}
