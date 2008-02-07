package org.basex.query.xquery.util;

/**
 * Scoring class, assembling all scoring calculations.
 * Current scoring is very simple and mainly used for testing.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Scoring implements Cloneable {
  /** Default scoring value. */
  public static final double DEFAULT = 0;
  /** Logarithmic base for calculating the score value. */
  public static final double LOG = Math.E - 1;

  /** Private Constructor. */
  private Scoring() { }

  /**
   * Returns a scoring after a location step traversal.
   * @param s input value
   * @return result
   */
  public static double step(final double s) {
    return s;
  }

  /**
   * Calculates a score value, based on the token length
   * and complete text length.
   * @param tl token length
   * @param l complete length
   * @return result
   */
  public static double word(final int tl, final double l) {
    return 1 / Math.log(LOG + tl / l);
  }

  /**
   * Combines two scoring values.
   * @param s1 first value
   * @param s2 second value
   * @return result
   */
  public static double add(final double s1, final double s2) {
    return 1 - ((1 - s1) * (1 - s2));
  }

  /**
   * Combines two scoring values.
   * @param s1 first value
   * @param s2 second value
   * @return result
   */
  public static double and(final double s1, final double s2) {
    return 1 - ((1 - s1) * (1 - s2));
  }

  /**
   * Combines two scoring values.
   * @param s1 first value
   * @param s2 second value
   * @return result
   */
  public static double or(final double s1, final double s2) {
    return 1 - ((1 - s1) * (1 - s2));
  }

  /**
   * Creates a final scoring value.
   * @param s input value
   * @return result
   */
  public static double finish(final double s) {
    return Math.min(1, (int) (s * 1000) / 1000d);
  }
}
