package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.core.Main;

/**
 * This is a simple container for tokens (byte arrays).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class TokenList implements Iterable<byte[]> {
  /** Resize factor for extending the the byte arrays. */
  private double factor = 2;
  /** Value array. */
  byte[][] list;
  /** Current array size. */
  int size;

  /**
   * Default constructor.
   */
  public TokenList() {
    this(8);
  }

  /**
   * Constructor.
   * @param is initial size of the list
   */
  public TokenList(final int is) {
    list = new byte[is][];
  }

  /**
   * Constructor.
   * @param f resize factor
   */
  public TokenList(final double f) {
    this(8);
    factor = f;
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final byte[] v) {
    if(size == list.length) {
      list = Arrays.copyOf(list, Math.max(size + 1, (int) (size * factor)));
    }
    list[size++] = v;
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final long v) {
    add(token(v));
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final String v) {
    add(Token.token(v));
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Sets the number of entries.
   * @param s number of entries
   */
  public void size(final int s) {
    size = s;
  }

  /**
   * Returns the specified value.
   * @param p position
   * @return value
   */
  public byte[] get(final int p) {
    return list[p];
  }

  /**
   * Sets the specified value at the specified position.
   * @param v value
   * @param p position
   */
  public void set(final byte[] v, final int p) {
    list[p] = v;
  }

  /**
   * Checks if the specified token is found in the list.
   * @param v token to be checked
   * @return true if value is found
   */
  public boolean contains(final byte[] v) {
    return indexOf(v) != -1;
  }

  /**
   * Finds the index of the specified token in the list.
   * @param v token to be found
   * @return index of the token if it is found, {@code -1} otherwise
   */
  public int indexOf(final byte[] v) {
    for(int i = 0; i < size; i++) if(eq(list[i], v)) return i;
    return -1;
  }

  /**
   * Finishes the array.
   * @return array
   */
  public byte[][] finish() {
    return size == list.length ? list : Arrays.copyOf(list, size);
  }

  /**
   * Finishes the list as string array.
   * @return array
   */
  public String[] finishString() {
    final String[] items = new String[size];
    for(int i = 0; i < items.length; i++) items[i] = string(list[i]);
    return items;
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Sorts the strings.
   * @param cs respect case sensitivity
   */
  public void sort(final boolean cs) {
    Arrays.sort(list, 0, size, new Comparator<byte[]>() {
      public int compare(final byte[] s1, final byte[] s2) {
        return cs ? diff(s1, s2) : diff(lc(s1), lc(s2));
      }
    });
  }

  public Iterator<byte[]> iterator() {
    return new Iterator<byte[]>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public byte[] next() { return list[c]; }
      public void remove() { Main.notexpected(); }
    };
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Main.name(this) + '[');
    for(int i = 0; i < size; i++) {
      if(i != 0) tb.add(", ");
      tb.add(list[i]);
    }
    return tb.add(']').toString();
  }
}
