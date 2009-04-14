package org.basex.ft;

/**
 * Simple default scoring model, assembling all scoring calculations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Scoring {
  /** Logarithmic base for calculating the score value. */
  private static final double LOG = Math.E - 1;

  /**
   * Returns a scoring after a location step traversal.
   * @param s input value
   * @return result
   */
  public double step(final double s) {
    return s;
  }

  /**
   * Calculates a score value, based on the token length
   * and complete text length.
   * @param tl token length
   * @param l complete length
   * @return result
   */
  public double word(final int tl, final double l) {
    return Math.min(1, Math.log(1 + LOG * tl / l));
  }

  /**
   * Combines two scoring values.
   * @param s1 first value
   * @param s2 second value
   * @return result
   */
  public double and(final double s1, final double s2) {
    return 1 - ((1 - s1) * (1 - s2));
  }

  /**
   * Combines two scoring values.
   * @param s1 first value
   * @param s2 second value
   * @return result
   */
  public double or(final double s1, final double s2) {
    return and(s1, s2);
  }

  /**
   * Creates a final scoring value.
   * @param s input value
   * @return result
   */
  public double finish(final double s) {
    return Math.min(1, (int) (s * 1000) / 1000d);
  }
}
