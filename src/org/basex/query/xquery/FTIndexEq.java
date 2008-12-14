package org.basex.query.xquery;

import org.basex.data.Data;
import org.basex.query.xquery.path.Step;

/**
 * Container for collecting all information needed, to find a index
 * equivalent query.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTIndexEq {
  /** Data reference. */
  public final Data data;
  /** Flag for sequential processing. */
  public final boolean seq;
  /** Current Step. */
  public final Step curr;

  /**
   * Constructor.
   * @param a index accessible information
   * @param c current step
   */
  public FTIndexEq(final FTIndexAcsbl a, final Step c) {
    data = a.data;
    seq = a.seq;
    curr = c;
  }
}
