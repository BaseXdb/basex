package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is a simple container for tokens (byte arrays).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class TokenList extends ElementList implements Iterable<byte[]> {
  /** Element container. */
  private byte[][] list;

  /**
   * Default constructor.
   */
  public TokenList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size
   * (default is {@link Array#CAPACITY}).
   * @param capacity initial array capacity
   */
  public TokenList(final int capacity) {
    list = new byte[capacity][];
  }

  /**
   * Constructor, specifying a resize factor. Smaller values are more memory-saving,
   * while larger will provide better performance.
   * @param resize resize factor
   */
  public TokenList(final double resize) {
    this();
    factor = resize;
  }

  /**
   * Constructor, adopting the elements from the specified set.
   * @param set set to be added
   */
  public TokenList(final TokenSet set) {
    this(set.size());
    for(final byte[] e : set) add(e);
  }

  /**
   * Lightweight constructor, assigning the specified array.
   * @param elements initial array
   */
  public TokenList(final byte[]... elements) {
    list = elements;
    size = elements.length;
  }

  /**
   * Adds an element.
   * @param element element to be added
   * @return self reference
   */
  public TokenList add(final byte[] element) {
    byte[][] lst = list;
    final int s = size;
    if(s == lst.length) lst = Array.copyOf(lst, newSize());
    lst[s] = element;
    list = lst;
    size = s + 1;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  public TokenList add(final byte[]... elements) {
    byte[][] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Array.copyOf(lst, newSize(ns));
    System.arraycopy(elements, 0, lst, s, l);
    list = lst;
    size = ns;
    return this;
  }

  /**
   * Adds a long value.
   * @param element element to be added
   */
  public void add(final long element) {
    add(token(element));
  }

  /**
   * Adds a string.
   * @param element element to be added
   */
  public void add(final String element) {
    add(token(element));
  }

  /**
   * Inserts the given elements at the specified position.
   * @param index inserting position
   * @param elements elements to insert
   */
  public void insert(final int index, final byte[][] elements) {
    final int l = elements.length;
    if(l == 0) return;

    if(size + l > list.length) list = Array.copyOf(list, newSize(size + l));
    Array.move(list, index, l, size - index);
    System.arraycopy(elements, 0, list, index, l);
    size += l;
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   * @return deleted element
   */
  public byte[] remove(final int index) {
    final byte[] l = list[index];
    Array.move(list, index + 1, -1, --size - index);
    return l;
  }

  /**
   * Returns the element at the specified position.
   * @param index index of the element to return
   * @return element
   */
  public byte[] get(final int index) {
    return index < list.length ? list[index] : null;
  }

  /**
   * Stores an element to the specified position.
   * @param index index of the element to replace
   * @param element element to be stored
   */
  public void set(final int index, final byte[] element) {
    if(index >= list.length) list = Array.copyOf(list, newSize(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public byte[] pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public void push(final byte[] element) {
    add(element);
  }

  /**
   * Returns the uppermost element on the stack, without removing it.
   * @return uppermost element
   */
  public byte[] peek() {
    return list[size - 1];
  }

  /**
   * Checks if the specified element is found in the list.
   * @param element element to be found
   * @return result of check
   */
  public boolean contains(final byte[] element) {
    for(int i = 0; i < size; ++i) if(eq(list[i], element)) return true;
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
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public byte[][] next() {
    final byte[][] lst = Array.copyOf(list, size);
    reset();
    return lst;
  }

  /**
   * Returns the token as byte array, and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return token
   */
  public byte[][] finish() {
    final byte[][] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Array.copyOf(lst, s);
  }

  /**
   * Returns an array with all elements as strings.
   * @return array
   */
  public String[] toStringArray() {
    final int is = size;
    final byte[][] l = list;
    final String[] items = new String[is];
    for(int i = 0; i < is; ++i) items[i] = string(l[i]);
    return items;
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @return self reference
   */
  public TokenList sort(final boolean cs) {
    return sort(cs, true);
  }

  /**
   * Sorts the elements.
   * @param cs respect case sensitivity
   * @param asc ascending (true)/descending (false) flag
   * @return self reference
   */
  public TokenList sort(final boolean cs, final boolean asc) {
    final Comparator<byte[]> comp = cs ? COMP : LC_COMP;
    Arrays.sort(list, 0, size, asc ? comp : Collections.reverseOrder(comp));
    return this;
  }

  /**
   * Removes duplicates from the list.
   * The list must be sorted.
   * @return self reference
   */
  public TokenList unique() {
    if(size != 0) {
      int s = 0;
      for(int l = 1; l < size; l++) {
        if(!eq(list[l], list[s])) list[++s] = list[l];
      }
      size = s + 1;
    }
    return this;
  }

  @Override
  public Iterator<byte[]> iterator() {
    return new ArrayIterator<>(list, size);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.className(this) + '[');
    for(int i = 0; i < size; ++i) {
      if(i != 0) tb.add(", ");
      tb.addExt(list[i]);
    }
    return tb.add(']').toString();
  }
}
