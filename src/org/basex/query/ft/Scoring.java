package org.basex.query.ft;

import org.basex.data.Data;
import org.basex.data.FTMatch;
import org.basex.data.FTStringMatch;
import org.basex.query.item.DBNode;
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
  /** Scoring step. */
  private static final double SCORESTEP = 0.8;
  
//  public static boolean print = false;


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
    return s;
    //return Math.min(1, (int) (s * 1000) / 1000d);
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

    for(final FTMatch m1 : item1.all) {
      for(final FTStringMatch sm1 : m1) {
        if(sm1.n) continue;
        for(final FTMatch m2 : item2.all) {
          for(final FTStringMatch sm2 : m2) {
            if(sm2.n) continue;
            sum += Math.abs(sm1.s - sm2.s);
            count++;
          }
        }
      }
    }
    return count == 0 ? 0 : score / Math.sqrt((double) sum / count);
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

  /**
   * Returns the score for the specified key.
   * @param numdoc number of documents in the collection
   * @param numdocterm number of documents containing the term
   * @param max maximum occurrence a term
   * @param f frequency of the term
   * @return score value
   */
  public static int tfIDF(final double numdoc, final double numdocterm,
      final double max, final double f) {
//    if (print) {
//      System.out.println("freq. token in document: " + f);
//      System.out.println("max freq. any token in document: " + max);
//      System.out.println("numb documents: " + numdoc);
//      System.out.println("numb documents with token: " + numdocterm);
//      System.out.println((numdoc != numdocterm ?
//      Math.log(numdoc / numdocterm) : 1) * f /max);
//    }
    return (int) ((numdoc != numdocterm ?
        Math.log(numdoc / numdocterm) : 1) * f * 1000 / max);
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
   * Scoring the parent axis step.
   * @param sc current score value
   * @return new score value
   */
  public static double parentAxis(final double sc) {
    return sc * SCORESTEP;
  }

  /**
   * Scoring the child axis step.
   * @param sc current score value
   * @return new score value
   */
  public static double childAxis(final double sc) {
    return sc * SCORESTEP;
  }

  /**
   * Scoring the descendant axis step.
   * @param sc current score value
   * @return new score value
   */
  public static double descAxis(final double sc) {
    
    return sc * SCORESTEP;
  }

  /**
   * Scoring the parent axis step by using meta information.
   * @param data Data reference
   * @param nod current node
   * @return score value
   */
  public static double parentAxis(final Data data, final DBNode nod) {
    return nod.score() *
      (1d - (double) distToRoot(data, nod.pre) / data.meta.height);
  }

  /**
   * Determine distance to root node.
   * @param data Data reference
   * @param p pre value of current node
   * @return distance to root node
   */
  private static int distToRoot(final Data data, final int p) {
    int d = 0;
    int parent = data.parent(p, data.kind(p));
    while(parent > 0) {
      d++;
      parent = data.parent(parent, data.kind(parent));
    }
    return d;
  }
}
