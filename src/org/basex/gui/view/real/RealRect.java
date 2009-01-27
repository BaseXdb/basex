package org.basex.gui.view.real;

import org.basex.gui.view.ViewRect;

/**
 * extends ViewRect, used for RealView.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller, Philipp Ziemer
 */
public class RealRect extends ViewRect {
  
  /** mPre array with multi pre values. */
  public int[] mPre = null;
  /** multiPre boolean, true if multi pres exist. */
  boolean multiPre = false;
  
  /** rectangle constructor, specifying additional rectangle
   * with multi pres.
  * @param xx x position
  * @param yy y position
  * @param ww width
  * @param hh height
  * @param pres rectangle multi pre values
  * @param l level
  */
  
  public RealRect(final int xx, final int yy, final int ww, final int hh, 
      final int [] pres, final int l) {
    x = xx;
    y = yy;
    w = ww;
    h = hh;
    mPre = pres;
    level = l;
    multiPre = true;
  }
  
  
  /** rectangle constructor, specifying additional rectangle
  * attributes such as id, level and orientation.
  * @param xx x position
  * @param yy y position
  * @param ww width
  * @param hh height
  * @param p rectangle pre value
  * @param l level
   */
  public RealRect(final int xx, final int yy, final int ww, 
      final int hh, final int p, final int l) {
    x = xx;
    y = yy;
    w = ww;
    h = hh;
    pre = p;
    level = l;
  }

}
