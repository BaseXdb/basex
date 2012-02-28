package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.Arrays;

import org.basex.util.hash.TokenIntMap;

/**
 * Two-way Token Map allows efficient key->value and value->key lookups.
 * Also speeds up "contains"-operation.
 *
 * Value->key is done using {@link TokenList} which is an ArrayList.
 * Key->value is done using {@link TokenIntMap} which is an HashMap.
 *
 * Elements may not be inserted twice (bijection).
 *
 * @author Jens Erat
 *
 */
public class TwoWayTokenMap extends TokenList {
  /** TokenIntMap for reverse lookup. */
  private final TokenIntMap map = new TokenIntMap();

  /**
   * Adds an element.
   * @param e element to be added
   * @throws IllegalArgumentException when element already exists
   */
  @Override
  public void add(final byte[] e) {
    if (-1 == map.value(e)) {
      super.add(e);
      map.add(e, size);
    } else {
      throw new IllegalArgumentException("Cannot insert value twice!");
    }
  }

  /**
   * Adds an element.
   * @param e element to be added
   * @throws IllegalArgumentException when element already exists
   */
  @Override
  public void add(final long e) {
    add(token(e));
  }

  /**
   * Adds an element.
   * @param e element to be added
   * @throws IllegalArgumentException when element already exists
   */
  @Override
  public void add(final String e) {
    add(token(e));
  }

  /**
   * Sets an element at the specified index.
   * @param i index
   * @param e element to be set
   * @throws IllegalArgumentException when element already exists
   */
  @Override
  public void set(final int i, final byte[] e) {
    if (-1 == map.value(e) || Arrays.equals(get(i), e)) {
      map.delete(get(i));
      super.set(i, e);
      map.add(e, i + 1);
    } else {
      throw new IllegalArgumentException("Cannot insert value twice!");
    }
  }

  /**
   * Sets an element at the specified index.
   * @param i index
   * @param e element to be set
   * @throws IllegalArgumentException when element already exists
   */
  public void set(final int i, final String e) {
    set(i, token(e));
  }

  @Override
  public byte[] pop() {
    final byte[] e = super.pop();
    map.delete(e);
    return e;
  }

  /**
   * Pushes an element onto the stack.
   * @param val element
   * @throws IllegalArgumentException when element already exists
   */
  @Override
  public void push(final byte[] val) {
    add(val);
  }

  @Override
  public boolean contains(final byte[] e) {
    return map.value(e) != -1;
  }

  /**
   * Checks if the specified element is found in the list. Convenience method
   * for casting Strings.
   * @see #contains(byte[])
   * @param e element to be checked
   * @return result of check
   */
  public boolean contains(final String e) {
    return contains(token(e));
  }

  /**
   * Sorting a TwoWayTokenMap is not implemented.
   * @param cs sort order: not used
   * @return (will not return)
   * @throws UnsupportedOperationException when executed
   */
  @Override
  public TokenList sort(final boolean cs) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets key by value.
   * @param val Value to search for
   * @return Key or -1 if not found
   */
  public int getKey(final byte[] val) {
    final int key = map.value(val);
    return key > 0 ? key - 1 : -1;
  }

  /**
   * Gets key by value.
   * @param val Value to search for
   * @return Key or -1 if not found
   */
  public int getKey(final long val) {
    return getKey(token(val));
  }

  /**
   * Gets key by value.
   * @param val Value to search for
   * @return Key or -1 if not found
   */
  public int getKey(final String val) {
    return getKey(token(val));
  }

  /**
   * Gets key by value. {@link #size()} will not represent actual size any more
   * and {@link #get} will return null values for this key afterwards!
   * @param val Value to delete
   * @return Key or -1 if not found
   */
  public int delete(final byte[] val) {
    int key = map.value(val);
    key = key > 0 ? key - 1 : -1;
    if(-1 != key) {
      map.delete(val);
      list[key] = null;
    }
    return key;
  }

  /**
   * Gets key by value. {@link #size()} will not represent actual size any more
   * and {@link #get} will return null values for this key afterwards!
   * @param val Value to delete
   * @return Key or -1 if not found
   */
  public final int delete(final String val) {
    return delete(token(val));
  }

}
