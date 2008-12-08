package org.basex.index;

import org.basex.data.Data;

/**
 * Container for all information needed to determine whether an index is 
 * accessible or not. 
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */

public class FTIndexAcsbl {
  /** Data reference. */
  public Data data;
  /** Flag for indexuse. */
  public boolean iu;
  /** Flag for indexoptions. */
  public boolean io;
  /** Number of results. */
  public int indexSize = Integer.MAX_VALUE;
  /** Flag for ftnot exprs. */
  public boolean ftnot;
  /** Flag for sequential processing. */
  public boolean seq;
  
  /**
   * Constructor.
   */
  public FTIndexAcsbl() {
  }
  
  /**
   * Set indexUse flag and size.
   * 
   * @param indUse boolean 
   * @param indSize index size
   * @param indOpt boolean for indexoptions
   */
  public void set(final boolean indUse, final int indSize, 
      final boolean indOpt) {
    iu = indUse;
    indexSize = indSize;
    io = indOpt;
  }
}
