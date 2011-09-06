package org.basex.util.list;

import static org.basex.util.Token.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;

import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * This is a simple container for tokens (byte arrays).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TokenList extends ElementList implements Iterable<byte[]> {
  /** Element container. */
  byte[][] list;

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
   * Adds an element.
   * @param e element to be added
   */
  public void add(final byte[] e) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = e;
  }

  /**
   * Adds a long value.
   * @param e element to be added
   */
  public void add(final long e) {
    add(token(e));
  }

  /**
   * Adds a string.
   * @param e element to be added
   */
  public void add(final String e) {
    add(Token.token(e));
  }

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element
   */
  public byte[] get(final int i) {
    return list[i];
  }

  /**
   * Sets an element at the specified index.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final byte[] e) {
    if(i >= list.length) list = Array.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   * @throws EmptyStackException if the stack is empty
   */
  public byte[] pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param val element
   */
  public void push(final byte[] val) {
    add(val);
  }

  /**
   * Returns the uppermost element on the stack, without removing it.
   * @return uppermost element
   * @throws EmptyStackException if the stack is empty
   */
  public byte[] peek() {
    return list[size - 1];
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be checked
   * @return result of check
   */
  public boolean contains(final byte[] e) {
    for(int i = 0; i < size; ++i) if(eq(list[i], e)) return true;
    return false;
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public byte[][] toArray() {
    return Array.copyOf(list, size);
  }

  /**
   * Returns an array with all elements as strings.
   * @return array
   */
  public String[] toStringArray() {
    final String[] items = new String[size];
    for(int i = 0; i < items.length; ++i) items[i] = string(list[i]);
    return items;
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @return self reference
   */
  public TokenList sort(final boolean cs) {
    Arrays.sort(list, 0, size, new Comparator<byte[]>() {
      @Override
      public int compare(final byte[] s1, final byte[] s2) {
        return cs ? diff(s1, s2) : diff(lc(s1), lc(s2));
      }
    });
    return this;
  }

  @Override
  public Iterator<byte[]> iterator() {
    return new Iterator<byte[]>() {
      private int c;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public byte[] next() { return list[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.name(this) + '[');
    for(int i = 0; i < size; ++i) {
      if(i != 0) tb.add(", ");
      tb.add(list[i]);
    }
    return tb.add(']').toString();
  }
}
