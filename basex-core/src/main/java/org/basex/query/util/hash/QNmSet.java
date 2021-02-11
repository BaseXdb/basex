package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This is an efficient and memory-saving hash map for storing QNames.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class QNmSet extends ASet implements Iterable<QNm> {
  /** Hashed keys. */
  protected QNm[] keys;
  /** Hash values. */
  protected int[] hash;

  /**
   * Default constructor.
   */
  public QNmSet() {
    super(Array.INITIAL_CAPACITY);
    keys = new QNm[capacity()];
    hash = new int[capacity()];
  }

  /**
   * Stores the specified QName if it has not been stored before.
   * @param qnm QName to add
   * @return {@code true} if the QName did not exist yet and was stored
   */
  public final boolean add(final QNm qnm) {
    return index(qnm) >= 0;
  }

  /**
   * Stores the specified QName and returns its id.
   * @param qnm QName to put
   * @return unique id of stored QName (larger than zero)
   */
  public final int put(final QNm qnm) {
    final int id = index(qnm);
    return Math.abs(id);
  }

  /**
   * Checks if the specified QName exists.
   * @param qnm QName to look up
   * @return result of check
   */
  public final boolean contains(final QNm qnm) {
    return id(qnm) > 0;
  }

  /**
   * Returns the id of the specified QName, or {@code 0} if the QName does not exist.
   * @param qnm QName to look up
   * @return id, or {@code 0} if QName does not exist
   */
  public final int id(final QNm qnm) {
    final int b = qnm.hash(null) & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(keys[id].eq(qnm)) return id;
    }
    return 0;
  }

  /**
   * Stores the specified QName and returns its id, or returns the negative id if the
   * QName has already been stored.
   * @param qnm QName to look up
   * @return id, or negative id if QName has already been stored
   */
  private int index(final QNm qnm) {
    checkSize();
    final int h = qnm.hash(null), b = h & capacity() - 1;
    for(int id = buckets[b]; id != 0; id = next[id]) {
      if(keys[id].eq(qnm)) return -id;
    }
    final int s = size++;
    next[s] = buckets[b];
    keys[s] = qnm;
    hash[s] = h;
    buckets[b] = s;
    return s;
  }

  @Override
  protected final int hash(final int id) {
    return hash[id];
  }

  @Override
  protected void rehash(final int newSize) {
    keys = Array.copy(keys, new QNm[newSize]);
    hash = Arrays.copyOf(hash, newSize);
  }

  @Override
  public final Iterator<QNm> iterator() {
    return new ArrayIterator<>(keys, 1, size);
  }

  @Override
  public String toString() {
    return toString(keys);
  }
}
