package org.basex.gui.view.map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.view.ViewRect;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  MapView view;
  /** Marked position. */
  int mpos;
  
  /**
   * Constructor.
   * @param m map reference.
   */
  MapPainter(final MapView m) {
    view = m;
  }
  
  /**
   * Returns next color mark.
   * @param rects rectangle array
   * @param pre pre array
   * @param ri current position
   * @param rs array size
   * @return next color mark
   */
  Color nextMark(final ArrayList<ViewRect> rects, final int pre, final int ri,
      final int rs) {
    final Nodes marked = GUI.context.marked();
    // checks if the current node is a queried context node
    while(mpos < marked.size && marked.nodes[mpos] < pre) mpos++;
    if(mpos < marked.size) {
      if(marked.nodes[mpos] == pre) {
        // mark node
        return GUIConstants.colormark1;
      } else if(ri + 1 < rs && marked.nodes[mpos] < rects.get(ri + 1).pre) {
        // mark ancestor of invisible node
        return GUIConstants.colormark2;
      } 
    }
    return null;
  }

  /**
   * Paints node contents.
   * @param g graphics reference
   * @param rects rectangle array
   */
  abstract void drawRectangles(final Graphics g,
      final ArrayList<ViewRect> rects);

  /**
   * Checks mouse activity.
   * @param click mouse click
   * @param rect current rectangle
   * @param mx mouse x
   * @param my mouse y
   * @return true for mouse activity
   */
  abstract boolean highlight(ViewRect rect, int mx, int my, boolean click);

  /**
   * Initializes the skipping of nodes.
   * @param rects rectangle array
   */
  abstract void init(ArrayList<ViewRect> rects);

  /**
   * Resets the painter.
   */
  abstract void reset();

  /**
   * Closes the painter.
   */
  abstract void close();
}
