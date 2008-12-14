package org.basex.query.xquery;

import org.basex.data.Data;

/**
 * Container for all information needed to determine whether an index is 
 * accessible or not. 
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTIndexAcsbl {
  /** Data reference. */
  public final Data data;
  /** Flag for index use. */
  public boolean iu;
  /** Flag for index options. */
  public boolean io = true;
  /** Number of estimated results. */
  public int is = Integer.MAX_VALUE;
  /** Flag for ftnot expressions. */
  public boolean ftnot;
  /** Flag for sequential processing. */
  public boolean seq;

  /**
   * Constructor.
   * @param d data reference
   */
  public FTIndexAcsbl(final Data d) {
    data = d;
  }
}
