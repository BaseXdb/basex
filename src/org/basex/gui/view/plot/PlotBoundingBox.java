package org.basex.gui.view.plot;

/**
 * A bounding box to support item selection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public class PlotBoundingBox {
  /** X coordinate of upper left corner. */
  int x1;
  /** Y coordinate of upper left corner. */
  int y1;
  /** X coordinate of lower right corner. */
  int x2;
  /** Y coordinate of lower right corner. */
  int y2;
  /** Indicates if box is growing or not. Needed to solve some performance
   * issues concerning node marking operations.
   */
  boolean growing;
  
  
  /**
   * Setter for box start point. 
   * @param x x coordinate
   * @param y y coordinate
   */
  void setStart(final int x, final int y) {
    x1 = x;
    y1 = y;
  }
  
  /**
   * Setter for box end point.
   * @param x x coordinate 
   * @param y y coordinate
   */
  void setEnd(final int x, final int y) {
    growing = Math.abs(x1 - x) > Math.abs(x1 - x2) && 
      Math.abs(y1 - y) > Math.abs(y1 - y2); 
    x2 = x;
    y2 = y;
  }
  
  /**
   * Calculates box width.
   * @return box width
   */
  int getWidth() {
    return x2 - x1;
  }
  
  /**
   * Calculates box height.
   * @return box height
   */
  int getHeight() {
    return y2 - y1;
  }
  
  /**
   * Box is growing while being dragged.
   * @return growing or not
   */
  boolean isGrowing() {
    return growing;
  }
}
