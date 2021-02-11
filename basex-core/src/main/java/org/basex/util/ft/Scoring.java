package org.basex.util.ft;

import static java.lang.StrictMath.*;

/**
 * Default scoring model, assembling all score calculations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Scoring {
  /** Logarithmic base for calculating the score value. */
  private static final double LOG = Math.E - 1;

  /** Private constructor. */
  private Scoring() { }

  /**
   * Calculates a score value, based on the token length
   * and complete text length.
   * @param token token length
   * @param length complete length
   * @return result
   */
  public static double word(final int token, final double length) {
    return min(1, log(1 + LOG * token / length));
  }

  /**
   * Inverses the scoring value for FTNot.
   * @param value scoring value
   * @return score
   */
  public static double not(final double value) {
    return 1 - value;
  }

  /**
   * Returns an average scoring value.
   * @param sum summarized scoring value
   * @param count number of values
   * @return score
   */
  public static double avg(final double sum, final int count) {
    return sum / count;
  }

  /**
   * Calculates the score for a text node.
   * Used if no index score is available.
   * @param number number of pos values
   * @param size total number of index entries
   * @param token token length
   * @param length text length
   * @return score
   */
  public static double textNode(final int number, final int size, final int token,
      final int length) {
    return max((double) number / size, log(token * number + 1) / log(length + 1));
  }
}
