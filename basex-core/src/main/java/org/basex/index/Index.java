package org.basex.index;

import org.basex.index.query.*;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public interface Index {
  /**
   * Initializes the index.
   */
  void init();

  /**
   * Returns information on the index structure.
   * @return info
   */
  byte[] info();

  /**
   * Returns all entries that match the specified token.
   * @param entries index entries
   * @return entries
   */
  EntryIterator entries(final IndexEntries entries);

  /**
   * Returns an iterator for the index results.
   * @param token token to be found
   * @return sorted pre values for the token
   */
  IndexIterator iter(final IndexToken token);

  /**
   * Returns the (approximate/estimated) number of hits for the specified token.
   * @param token token to be found
   * @return number of hits
   */
  int count(final IndexToken token);

  /**
   * Closes the index.
   */
  void close();
}
