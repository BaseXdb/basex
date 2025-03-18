package org.basex.util.hash;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and integers.
 * {@link Integer#MIN_VALUE} is returned for an entry that does not exist.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenIntMap extends TokenSet {
  /** Values. */
  private int[] values;

  /**
   * Default constructor.
   */
  public TokenIntMap() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public TokenIntMap(final long capacity) {
    super(capacity);
    values = new int[capacity()];
    values[0] = Integer.MIN_VALUE;
  }

  /**
   * Input constructor.
   * @param in input stream
   * @throws IOException I/O exception
   */
  public TokenIntMap(final DataInput in) throws IOException {
    read(in);
  }

  @Override
  public void read(final DataInput in) throws IOException {
    super.read(in);
    values = in.readNums();
  }

  @Override
  public void write(final DataOutput out) throws IOException {
    super.write(out);
    out.writeNums(values);
  }

  /**
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * Note that {@link Integer#MIN_VALUE} is used to indicate that a key does not exist.
   * @param key key
   * @param value value
   * @return old value
   */
  public int put(final byte[] key, final int value) {
    // array bounds are checked before array is resized
    final int i = put(key);
    final int v = values[i];
    values[i] = value;
    return v;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@link Integer#MIN_VALUE} if the key does not exist
   */
  public int get(final byte[] key) {
    return values[id(key)];
  }

  /**
   * Returns the value with the specified id.
   * All ids start with {@code 1} instead of {@code 0}.
   * @param id id of the value
   * @return value
   */
  public int value(final int id) {
    return values[id];
  }

  @Override
  public int remove(final byte[] key) {
    final int i = super.remove(key);
    values[i] = Integer.MIN_VALUE;
    return i;
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Arrays.copyOf(values, newSize);
  }

  @Override
  public String toString() {
    final List<Object> v = new ArrayList<>();
    for(final int value : values) v.add(value);
    return toString(keys, v.toArray());
  }
}
