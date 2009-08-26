package org.basex.query.ft;

import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.query.item.FTItem;

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
   * Creates a final scoring value.
   * @param s input value
   * @return result
   */
  public double finish(final double s) {
    return Math.min(1, (int) (s * 1000) / 1000d);
  }

  /**
   * Determines a single scoring value out of two FTAnd combined terms.
   * The minimum scoring value is chosen and weighted by the average distance
   * of the single result terms in the text node.
   *
   * @param item1 item1
   * @param item2 item2
   * @return scoring value
   */
  public double ftAnd(final FTItem item1, final FTItem item2) {
    final double score = Math.min(item1.score(), item2.score());
    int sum = 0;
    int count = 0;
    
    for(int i = 0; i < item1.all.size; i++) {
      for(final FTStringMatch sm1 : item1.all.match[i]) {
        for(int j = 0; j < item2.all.size; j++) {
          for(final FTStringMatch sm2 : item2.all.match[j]) {
            sum += Math.abs(sm1.e - sm2.s);
            count++;
          }
        }
      }
    }
    final double avg = (double) sum / (double) count;
    return score / Math.sqrt(avg);
  }

  /**
   * Determines a single scoring value out of two FTOr combined terms.
   * The maximum scoring value is chosen and weighted by the number
   * of term in the current text node at the ratio of the
   * number of terms out of the query.
   *
   * @param item1 item1
   * @param item2 item2
   * @param n number of tokens in the or expression
   * @param m number of tokens int the entire query
   * @return double scoring value
   */
  public double ftOr(final FTItem item1, final FTItem item2, final int n,
      final int m) {

    final double score = Math.max(item1.score(), item2.score());
    final int[] p = new int[n];
    for(final FTMatch mi : item1.all) {
      for(final FTStringMatch sm : mi) {
        p[sm.q - 1] = -1;
      }
    }

    int count = 0;
    for(final int pp : p) if(pp == -1) count++;

    return score * (count > m ? 1 : count / m);
  }

  /**
   * Inverse the scoring value for FTNot.
   * @param d scoring value
   * @return inverse scoring value
   */
  public double ftNot(final double d) {
    return 1 - d;
  }
}
