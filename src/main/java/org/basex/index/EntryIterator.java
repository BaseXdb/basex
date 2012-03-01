package org.basex.index;

/**
 * This interface provides methods for returning index entries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class EntryIterator {
  /**
   * Returns the next index entry.
   * @return entry
   */
  public abstract byte[] next();

  /**
   * Returns the number of occurrences of the current token.
   * @return counter
   */
  public abstract int count();
}
