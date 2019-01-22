package org.basex.index;

import org.basex.core.*;
import org.basex.index.query.*;
import org.basex.query.util.*;

/**
 * This interface defines the methods which have to be implemented
 * by an index structure.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public interface Index {
  /**
   * Returns information on the index structure.
   * @param options main options
   * @return info
   */
  byte[] info(MainOptions options);

  /**
   * Returns all entries that match the specified token.
   * @param entries index entries
   * @return entries
   */
  EntryIterator entries(IndexEntries entries);

  /**
   * Returns an iterator for the index results.
   * @param token token to be found
   * @return sorted pre values for the token
   */
  IndexIterator iter(IndexToken token);

  /**
   * Computes costs for accessing the specified token. An integer is returned:
   * <ul>
   *   <li> A negative value indicates that index access is not possible.</li>
   *   <li> A value of zero indicates that no results will be returned.</li>
   *   <li> A small value indicates that index access is fast.</li>
   * </ul>
   * Smaller values are better, a value of zero indicates that no results will be returned.
   * @param token token to be found
   * @return cost estimation
   */
  IndexCosts costs(IndexToken token);

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
