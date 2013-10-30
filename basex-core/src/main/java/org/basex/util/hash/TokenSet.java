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
public class TokenSet extends ASet implements Iterable<byte[]> {
  /** Hashed keys. */
  protected byte[][] keys;

  /**
   * Default constructor.
   */
  public TokenSet() {
    super(Array.CAPACITY);
    keys = new byte[Array.CAPACITY][];
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
   * @return {@code true} if the key did not exist yet and was stored
   */
  public final boolean add(final byte[] key) {
    return index(key) > 0;
  }

  /**
   * Stores the specified string as key if it has not been stored before.
   * @param key string to be added
   * @return {@code true} if the key did not exist yet and was stored
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
    final int p = Token.hash(key) & bucket.length - 1;
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
   * Deletes the specified key.
   * The deletion of keys will lead to empty entries. If {@link #size} is called after
   * deletions, the original number of entries will be returned.
   * @param key key
   * @return deleted key or 0
   */
  public int delete(final byte[] key) {
    final int b = Token.hash(key) & bucket.length - 1;
    for(int p = 0, i = bucket[b]; i != 0; p = i, i = next[i]) {
      if(!eq(key, keys[i])) continue;
      if(p == 0) bucket[b] = next[i];
      else next[p] = next[next[i]];
      keys[i] = null;
      return i;
    }
    return 0;
  }

  /**
   * Stores the specified key and returns its id, or returns the negative id if the
   * key has already been stored.
   * @param key key to be found
   * @return id, or negative id if key has already been stored
   */
  private int index(final byte[] key) {
    checkSize();
    final int b = Token.hash(key) & bucket.length - 1;
    for(int r = bucket[b]; r != 0; r = next[r]) if(eq(key, keys[r])) return -r;
    next[size] = bucket[b];
    keys[size] = key;
    bucket[b] = size;
    return size++;
  }

  @Override
  protected int hash(final int id) {
    return Token.hash(keys[id]);
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copyOf(keys, newSize);
  }

  @Override
  public final void clear() {
    Arrays.fill(keys, null);
    super.clear();
  }

  @Override
  public final Iterator<byte[]> iterator() {
    return new ArrayIterator<byte[]>(keys, 1, size);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : this) {
      if(!tb.isEmpty()) tb.add(", ");
      if(key != null) tb.add(key);
    }
    return new TokenBuilder(Util.className(getClass())).add('[').add(tb.finish()).
        add(']').toString();
  }
}
