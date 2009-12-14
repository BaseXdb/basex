package org.basex.query.ft;

/**
 * Simple default scoring model, assembling all scoring calculations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Scoring {
  /** Scoring multiplier to store values as integers. */
  public static final int MP = 1000;
  /** Logarithmic base for calculating the score value. */
  private static final double LOG = Math.E - 1;
  /** Scoring step. */
  private static final double SCORESTEP = 0.8;

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
   * @param o old value
   * @param n new value
   * @return result
   */
  public double and(final double o, final double n) {
    return 1 - (1 - o) * (1 - n);
  }

  /**
   * Combines two scoring values.
   * @param o old value
   * @param n new value
   * @return result
   */
  public double or(final double o, final double n) {
    return and(o, n);
  }

  /**
   * Inverses the scoring value for FTNot.
   * @param d scoring value
   * @return inverse scoring value
   */
  public double not(final double d) {
    return 1 - d;
  }

  /**
   * Returns a score for the let clause.
   * @param s summed up scoring values
   * @param c number of values
   * @return new score value
   */
  public double let(final double s, final int c) {
    return s / c;
  }

  /**
   * Returns a tf-idf for the specified values.
   * Used definition: freq(i, j) / max(l, freq(l, j)) * log(1 + N / n(i)).
   * The result is multiplied with the {@link #MP} constant to yield
   * integer values. The value <code>2</code> is used as minimum score,
   * as the total minimum value will be subtracted by 1 to avoid eventual
   * <code>0</code> scores.
   *
   * @param freq frequency of the token. TF: freq(i, j)
   * @param mfreq maximum occurrence of a token. TF: max(l, freq(l, j))
   * @param docs number of documents in the collection. IDF: N
   * @param tokens number of documents containing the token. IDF: n(i)
   * @return score value
   */
  public static int tfIDF(final double freq, final double mfreq,
      final double docs, final double tokens) {
    return (int) Math.max(2, MP * freq / mfreq * Math.log(1 + docs / tokens));
  }

  /**
   * Returns the score for a text node.
   * Used when no index score is available.
   * @param npv number of pos values
   * @param is index size
   * @param tokl token length
   * @param tl text length
   * @return score value
   */
  public static double textNode(final double npv, final double is,
      final double tokl, final double tl) {
    return Math.max(npv / is, Math.log(tokl * npv + 1) / Math.log(tl + 1));
  }

  /**
   * Returns the scoring value for a phrase.
   * @param w1 score of word1
   * @param w2 score of word2
   * @return score of the phrase
   */
  public static double phrase(final double w1, final double w2) {
    return (w1 + w2) / 2;
  }

  /**
   * Returns the union value.
   * @param w1 score of word1
   * @param w2 score of word2
   * @return score of the phrase
   */
  public static double union(final double w1, final double w2) {
    return Math.max(w1, w2);
  }

  /**
   * Returns a score for a single step.
   * @param sc current score value
   * @return new score value
   */
  public static double step(final double sc) {
    return sc * SCORESTEP;
  }
}
