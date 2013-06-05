package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is a simple container for tokens (byte arrays).
 *
 * @author BaseX Team 2005-13, BSD License
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
   * Lightweight constructor, adopting the specified array.
   * @param tokens initial array
   */
  public TokenList(final byte[][] tokens) {
    list = tokens;
    size = list.length;
  }

  /**
   * Lightweight constructor, adopting the elements from the specified set.
   * @param set set to be added
   */
  public TokenList(final TokenSet set) {
    this(set.size());
    for(final byte[] e : set) add(e);
  }

  /**
   * Adds an element.
   * @param element element to be added
   */
  public void add(final byte[] element) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = element;
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

    if(size + l > list.length) list = Arrays.copyOf(list, newSize(size + l));
    Array.move(list, index, l, size - index);
    System.arraycopy(elements, 0, list, index, l);
    size += l;
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   */
  public void deleteAt(final int index) {
    Array.move(list, index + 1, -1, --size - index);
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
    Arrays.sort(list, 0, size, cs ? COMP : LC_COMP);
    return this;
  }

  @Override
  public Iterator<byte[]> iterator() {
    return new ArrayIterator<byte[]>(list, size);
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

  /**
   * Creates a copy of this list.
   * @return copy of this list
   */
  public TokenList copy() {
    final TokenList tl = new TokenList(list.length);
    tl.factor = factor;
    for(int i = 0; i < size; i++) tl.add(list[i].clone());
    return tl;
  }
}
