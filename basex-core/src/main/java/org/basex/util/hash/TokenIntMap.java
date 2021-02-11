package org.basex.util.hash;

import java.io.*;
import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;

/**
 * This is an efficient and memory-saving hash map for storing tokens and integers.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class TokenIntMap extends TokenSet {
  /** Values. */
  private int[] values;

  /**
   * Constructor.
   */
  public TokenIntMap() {
    values = new int[capacity()];
  }

  /**
   * Constructor.
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
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * @param key key
   * @param value value
   */
  public void put(final byte[] key, final int value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code -1} if the key was not found
   */
  public int get(final byte[] key) {
    final int i = id(key);
    return i == 0 ? -1 : values[i];
  }

  @Override
  public int remove(final byte[] key) {
    final int i = super.remove(key);
    values[i] = -1;
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
