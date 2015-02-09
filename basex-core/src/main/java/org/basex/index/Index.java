package org.basex.index;

import org.basex.core.*;
import org.basex.index.query.*;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface Index {
  /**
   * Initializes the index.
   */
  void init();

  /**
   * Returns information on the index structure.
   * @param options main options
   * @return info
   */
  byte[] info(final MainOptions options);

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
   * Returns a cost estimation for searching the specified token.
   * Smaller values are better, a value of zero indicates that no results will be returned.
   * @param token token to be found
   * @return cost estimation
   */
  int costs(final IndexToken token);

  /**
   * Closes the index.
   */
  void close();

  /**
   * Drops the index.
   * @return success flag
   */
  boolean drop();
}
