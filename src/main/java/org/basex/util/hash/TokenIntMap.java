package org.basex.util.hash;

import java.io.IOException;
import java.util.Arrays;

import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;

/**
 * This is an efficient hash map for integers,
 * extending the {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TokenIntMap extends TokenSet {
  /** Values. */
  private int[] values;

  /**
   * Constructor.
   */
  public TokenIntMap() {
    values = new int[CAP];
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
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final int val) {
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value, or {@code -1} if nothing was found
   */
  public int value(final byte[] key) {
    final int id = id(key);
    return id == 0 ? -1 : values[id];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}
