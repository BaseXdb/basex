package org.basex.index.query;

/**
 * This interface provides methods for returning index entries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface EntryIterator {
  /**
   * Returns the next index entry.
   * @return entry or {@code null}
   */
  byte[] next();

  /**
   * Returns the number of occurrences of the current index entry.
   * @return count
   */
  int count();

  /**
   * Returns the specified index entry.
   * If this method returns tokens, {@link #size()} needs to be implemented as well.
   * @param i index of entry
   * @return entry or {@code null}
   */
  default byte[] get(@SuppressWarnings("unused") final int i) {
    return null;
  }

  /**
   * Returns the number of results.
   * If this method returns a positive value, {@link #get(int)} needs to be implemented as well.
   * @return number of results, or {@code -1} if unknown
   */
  default int size() {
    return -1;
  }
}
