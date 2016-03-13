package org.basex.index;

import org.basex.core.*;
import org.basex.index.query.*;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface Index {
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
   * Computes costs for accessing the specified token. An integer is returned:
   * <ul>
   *   <li> A negative zero indicates that index access is not possible.</li>
   *   <li> A value of zero indicates that no results will be returned.</li>
   *   <li> A small value indicates that index access is fast.</li>
   * </ul>
   * Smaller values are better, a value of zero indicates that no results will be returned.
   * @param token token to be found
   * @return cost estimation
   */
  int costs(final IndexToken token);

  /**
   * Drops the index. Also returns true if the index does not exist.
   * @return success flag
   */
  boolean drop();

  /**
   * Closes the index.
   */
  void close();
}
