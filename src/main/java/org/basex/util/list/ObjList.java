package org.basex.util.list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;
import org.basex.util.Array;
import org.basex.util.Util;

/**
 * This is an abstract class for storing objects in an array-based list.
 * @param <E> generic value type
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ObjList<E> extends ElementList
    implements Iterable<E>, RandomAccess {

  /** Element container. */
  Object[] list;

  /**
   * Default constructor.
   */
  public ObjList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public ObjList(final int c) {
    list = new Object[c];
  }

  /**
   * Adds an element to the array.
   * @param e element to be added
   */
  public void add(final E e) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = e;
  }

  /**
   * Returns the specified element.
   * @param p position
   * @return value
   */
  @SuppressWarnings("unchecked")
  public E get(final int p) {
    return (E) list[p];
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public boolean contains(final E e) {
    for(int i = 0; i < size; ++i) if(list[i].equals(e)) return true;
    return false;
  }

  /**
   * Sets an element at the specified index position.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final E e) {
    if(i >= list.length) list = Arrays.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  /**
   * Deletes the specified element.
   * @param i element to be deleted
   */
  public void delete(final int i) {
    Array.move(list, i + 1, -1, --size - i);
  }

  /**
   * Fills and returns the array with all elements.
   * @param a container to be filled
   * @return array with all elements
   */
  public E[] toArray(final E[] a) {
    if(a.length != size) Util.notexpected();
    System.arraycopy(list, 0, a, 0, size);
    return a;
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private int c = -1;
      private boolean r;
      @Override
      public boolean hasNext() {
        if(!r) r = more();
        return r;
      }
      @Override
      @SuppressWarnings("unchecked")
      public E next() {
        if(!r) more();
        r = false;
        return (E) list[c];
      }
      private boolean more() {
        return ++c < size;
      }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    // no implicit copying of the list
    return getClass().getSimpleName() + Arrays.asList(list).subList(0, size);
  }
}
