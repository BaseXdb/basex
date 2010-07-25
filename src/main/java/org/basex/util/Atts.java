package org.basex.util;

import java.util.Arrays;
import org.basex.core.Main;

/**
 * This is a simple container for attributes (key/value tokens).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Atts {
  /** Key array. */
  public byte[][] key = new byte[1][];
  /** Value array. */
  public byte[][] val = new byte[1][];
  /** Current array size. */
  public int size;

  /**
   * Adds the next key/value pair.
   * @param k key to be added
   * @param v value to be added
   * @return self reference
   */
  public Atts add(final byte[] k, final byte[] v) {
    if(size == key.length) {
      final int s = size << 1;
      key = Arrays.copyOf(key, s);
      val = Arrays.copyOf(val, s);
    }
    key[size] = k;
    val[size++] = v;
    return this;
  }

  /**
   * Deletes the specified entry.
   * @param i entry offset
   */
  public void delete(final int i) {
    Array.move(key, i + 1, -1, --size - i);
    Array.move(val, i + 1, -1, size - i);
  }

  /**
   * Checks if the specified key is found.
   * @param k key to be checked
   * @return result of check
   */
  public boolean contains(final byte[] k) {
    return get(k) != -1;
  }

  /**
   * Returns the reference for the specified key.
   * @param k key to be found
   * @return reference or -1
   */
  public int get(final byte[] k) {
    for(int i = 0; i < size; i++) if(Token.eq(key[i], k)) return i;
    return -1;
  }

  /**
   * Resets the container.
   * @return self reference
   */
  public Atts reset() {
    size = 0;
    return this;
  }

  @Override
  public String toString() {
    final TokenBuilder sb = new TokenBuilder(Main.name(this) + "[");
    for(int i = 0; i < size; i++) {
      sb.add(key[i]);
      sb.add("=\"");
      sb.add(val[i]);
      sb.add("\"");
    }
    return sb.add("]").toString();
  }
}
