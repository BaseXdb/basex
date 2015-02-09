package org.basex.index.query;

/**
 * This interface provides methods for returning index entries.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface EntryIterator {
  /**
   * Returns the next index entry.
   * @return entry
   */
  byte[] next();

  /**
   * Returns the number of occurrences of the current token.
   * @return counter
   */
  int count();
}
