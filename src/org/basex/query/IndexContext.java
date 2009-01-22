package org.basex.query;

import org.basex.data.Data;
import org.basex.query.path.Step;

/**
 * Container for all information needed to determine whether an index is 
 * accessible or not. 
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class IndexContext {
  /** Data reference. */
  public final Data data;
  /** Index Step. */
  public final Step step;
  
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
   * @param s index step
   */
  public IndexContext(final Data d, final Step s) {
    data = d;
    step = s;
  }
}
