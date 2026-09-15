package org.basex.index.ft;

import org.basex.index.query.*;
import org.basex.query.expr.ft.*;
import org.basex.util.list.*;

/**
 * A source of full-text references: a segment on disk, or the buffer in main memory.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
interface FTSource {
  /**
   * Returns the number of references for a token, including superseded and deleted ones.
   * @param token token
   * @return number of references
   */
  int count(byte[] token);

  /**
   * Collects the live references for a token.
   * @param token token
   * @param pres PRE values
   * @param poss positions
   */
  void exact(byte[] token, IntList pres, IntList poss);

  /**
   * Collects the live references for all tokens that match a wildcard expression.
   * @param wc wildcard matcher
   * @param full support full range of Unicode characters
   * @param pres PRE values
   * @param poss positions
   */
  void wildcards(FTWildcard wc, boolean full, IntList pres, IntList poss);

  /**
   * Collects the live references for all tokens that are similar to a token.
   * @param fuzzy fuzzy matcher
   * @param pres PRE values
   * @param poss positions
   */
  void fuzzy(FTFuzzy fuzzy, IntList pres, IntList poss);

  /**
   * Returns all tokens that start with the specified prefix, in ascending order.
   * @param prefix prefix
   * @return entry iterator
   */
  EntryIterator entries(byte[] prefix);

  /**
   * Returns all tokens that are similar to the token of a fuzzy matcher, in ascending order.
   * @param fuzzy fuzzy matcher
   * @return entry iterator
   */
  EntryIterator entries(FTFuzzy fuzzy);

  /**
   * Returns the number of tokens.
   * @return number of tokens
   */
  int size();

  /**
   * Returns the size of the files.
   * @return size in bytes
   */
  long length();
}
