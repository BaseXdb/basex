package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * This is an abstract class for storing objects of any kind in an array-based list.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <E> generic object type
 * @param <L> generic object type
 */
public abstract class ObjectList<E, L extends ObjectList<E, ?>> extends ElementList
    implements Iterable<E> {

  /** Element container. */
  public E[] list;

  /**
   * Constructor.
   * @param list initial list
.   */
  @SuppressWarnings("unchecked")
  protected ObjectList(final E... list) {
    this.list = list;
  }

  /**
   * Creates a resized array.
   * @param s size
   * @return array
   */
  protected abstract E[] newArray(int s);

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element, or {@code null} if index exceeds list size
   */
  public E get(final int i) {
    return i < size ? list[i] : null;
  }

  /**
   * Checks if the specified element is found in the list.
   * @param element element to be found
   * @return result of check
   */
  public boolean contains(final E element) {
    final E[] lst = list;
    final int s = size;
    for(int l = 0; l < s; l++) {
      if(equals(lst[l], element)) return true;
    }
    return false;
  }

  /**
   * Adds an element to the array if it is not contained yet.
   * @param element element to be added
   * @return result of check
   */
  @SuppressWarnings("unchecked")
  public L addUnique(final E element) {
    if(!contains(element)) add(element);
    return (L) this;
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public L add(final E element) {
    E[] lst = list;
    final int s = size;
    if(s == lst.length) lst = Array.copy(lst, newArray(newCapacity()));
    lst[s] = element;
    list = lst;
    size = s + 1;
    return (L) this;
  }

  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public final L add(final E... elements) {
    E[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Array.copy(lst, newArray(newCapacity(ns)));
    Array.copyFromStart(elements, l, lst, s);
    list = lst;
    size = ns;
    return (L) this;
  }

  /**
   * Adds elements from a string list to the array.
   * @param elements string list to be added
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public final L add(final L elements) {
    for(final E e : elements) add(e);
    return (L) this;
  }

  /**
   * Sets an element at the specified index position.
   * @param index index
   * @param element element to be set
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public final L set(final int index, final E element) {
    E[] lst = list;
    final int sz = size;
    if(index >= lst.length) lst = Array.copy(lst, newArray(newCapacity(index + 1)));
    lst[index] = element;
    list = lst;
    size = Math.max(sz, index + 1);
    return (L) this;
  }

  /**
   * Inserts the given elements at the specified position.
   * @param index inserting position
   * @param elements elements to insert
   */
  @SuppressWarnings("unchecked")
  public final void insert(final int index, final E... elements) {
    final int l = elements.length;
    if(l == 0) return;

    E[] lst = list;
    final int sz = size;
    if(sz + l > lst.length) lst = Array.copy(lst, newArray(newCapacity(sz + l)));
    Array.insert(lst, index, l, sz, elements);
    list = lst;
    size = sz + l;
  }

  /**
   * Deletes the element at the specified position.
   * @param index index of the element to delete
   * @return deleted element
   */
  public final E remove(final int index) {
    final E[] lst = list;
    final E e = lst[index];
    Array.remove(lst, index, 1, size);
    lst[--size] = null;
    return e;
  }

  /**
   * Removes all occurrences of the specified element from the list.
   * @param element element to be removed
   * @return flag, indicating if any element was removed
   */
  public boolean removeAll(final E element) {
    final E[] lst = list;
    final int sz = size;
    int s = 0;
    for(int i = 0; i < sz; ++i) {
      if(!equals(lst[i], element)) lst[s++] = lst[i];
    }
    for(int i = s; i < sz; i++) lst[i] = null;
    size = s;
    return sz != s;
  }

  /**
   * Removes all elements from the specified list.
   * @param elements elements
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public L removeAll(final L elements) {
    for(final E e : elements) removeAll(e);
    return (L) this;
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public final E pop() {
    final E[] lst = list;
    final int sz = --size;
    final E e = lst[sz];
    lst[sz] = null;
    return e;
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public final void push(final E element) {
    add(element);
  }

  /**
   * Returns the uppermost element on the stack, without removing it.
   * @return uppermost element
   */
  public E peek() {
    return list[size - 1];
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final E[] toArray() {
    return Array.copy(list, newArray(size));
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public final E[] finish() {
    final E[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Array.copy(lst, newArray(s));
  }

  /**
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public E[] next() {
    final E[] lst = toArray();
    reset();
    return lst;
  }

  /**
   * Sorts the elements.
   * @param comp comparator
   * @param ascending ascending/descending order
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public final L sort(final Comparator<E> comp, final boolean ascending) {
    Arrays.sort(list, 0, size, ascending ? comp : Collections.reverseOrder(comp));
    return (L) this;
  }

  /**
   * Compares two list elements.
   * @param element1 first element
   * @param element2 second element
   * @return result of check
   */
  public boolean equals(final E element1, final E element2) {
    return Objects.equals(element1, element2);
  }

  /**
   * Removes duplicates, provided that the entries are sorted.
   * @return self reference
   */
  @SuppressWarnings("unchecked")
  public final L unique() {
    final E[] lst = list;
    final int s = size;
    if(s != 0) {
      int ns = 0;
      for(int l = 1; l < s; l++) {
        if(!equals(lst[l], lst[ns])) lst[++ns] = lst[l];
      }
      size = ns + 1;
    }
    return (L) this;
  }

  @Override
  public Iterator<E> iterator() {
    return new ArrayIterator<>(list, size);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof ObjectList)) return false;
    final ObjectList<?, ?> f = (ObjectList<?, ?>) obj;
    final int s = size;
    if(s != f.size) return false;
    final E[] lst1 = list, lst2 = (E[]) f.list;
    for(int l = 0; l < s; l++) {
      if(!equals(lst1[l], lst2[l])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
