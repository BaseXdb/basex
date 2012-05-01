package org.basex.util.ft;

import static java.lang.StrictMath.*;

/**
 * Default scoring model, assembling all score calculations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Scoring {
  /** Scoring multiplier to store values as integers. */
  private static final int MP = 1000;
  /** Logarithmic base for calculating the score value. */
  private static final double LOG = Math.E - 1;
  /** Scoring step. */
  private static final double SCORESTEP = 0.8;

  /** Private constructor. */
  private Scoring() { }

  /**
   * Calculates a score value, based on the token length
   * and complete text length.
   * @param tl token length
   * @param l complete length
   * @return result
   */
  public static double word(final int tl, final double l) {
    return min(1, log(1 + LOG * tl / l));
  }

  /**
   * Combines two scoring values.
   * @param o old value
   * @param n new value
   * @return result
   */
  public static double and(final double o, final double n) {
    return 1 - (1 - o) * (1 - n);
  }

  /**
   * Combines two scoring values.
   * @param o old value
   * @param n new value
   * @return result
   */
  public static double or(final double o, final double n) {
    return and(o, n);
  }

  /**
   * Inverses the scoring value for FTNot.
   * @param d scoring value
   * @return inverse scoring value
   */
  public static double not(final double d) {
    return 1 - d;
  }

  /**
   * Returns a score for the let clause.
   * @param s summed up scoring values
   * @param c number of values
   * @return new score value
   */
  public static double let(final double s, final int c) {
    return s / c;
  }

  /**
   * <p>Calculates a TF-IDF value for the specified values.
   * Used definition:</p>
   * <p>{@code freq(i, j) / max(l, freq(l, j)) * log(1 + N / n(i))}</p>
   * <p>The result is multiplied with the {@link #MP} constant to yield
   * integer values. The value {@code 2} is used as minimum score,
   * as the total minimum value will be subtracted by 1 to avoid eventual
   * {@code 0} scores.</p>
   * @param freq frequency of the token. TF: freq(i, j)
   * @param mfreq maximum occurrence of a token. TF: max(l, freq(l, j))
   * @param docs number of documents in the collection. IDF: N
   * @param tokens number of documents containing the token. IDF: n(i)
   * @return score value
   */
  public static int tfIDF(final double freq, final double mfreq,
      final double docs, final double tokens) {
    return (int) max(2, MP * freq / mfreq * log(1 + docs / tokens));
  }

  /**
   * Calculates the score for a text node.
   * Used if no index score is available.
   * @param npv number of pos values
   * @param is total number of index entries
   * @param tokl token length
   * @param tl text length
   * @return score value
   */
  public static double textNode(final int npv, final int is, final int tokl,
      final int tl) {
    return max((double) npv / is, log(tokl * npv + 1) / log(tl + 1));
  }

  /**
   * Returns the scoring value for a phrase.
   * @param w1 score of word1
   * @param w2 score of word2
   * @return score of the phrase
   */
  public static double intersect(final double w1, final double w2) {
    return (w1 + w2) / 2;
  }

  /**
   * Returns the union value.
   * @param w1 score of word1
   * @param w2 score of word2
   * @return score of the phrase
   */
  public static double union(final double w1, final double w2) {
    return max(w1, w2);
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
