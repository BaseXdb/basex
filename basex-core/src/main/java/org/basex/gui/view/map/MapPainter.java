package org.basex.gui.view.map;

import java.awt.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.view.*;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  final MapView view;
  /** GUI options. */
  final GUIOptions gopts;

  /**
   * Constructor.
   * @param m map reference
   * @param opts gui options
   */
  MapPainter(final MapView m, final GUIOptions opts) {
    view = m;
    gopts = opts;
  }

  /**
   * Returns next color mark.
   * @param rects rectangle array
   * @param ri current position
   * @return next color mark
   */
  final Color color(final MapRects rects, final int ri) {
    // find marked node
    final Nodes marked = view.gui.context.marked;
    if(marked != null) {
      final int p = -marked.find(rects.get(ri).pre) - 1;
      if(p >= 0) {
        // mark ancestor of invisible node;
        final int i = rects.find(rects.get(ri));
        return p < marked.size() && i + 1 < rects.size && marked.sorted[p] <
          rects.sorted[i + 1].pre ? GUIConstants.colormark2 : null;
      }
    }
    // no mark found
    return GUIConstants.colormark1;
  }

  /**
   * Draws the specified rectangles.
   * @param g graphics reference
   * @param r rectangle array
   * @param scale scale boarders using this factor
   */
  abstract void drawRectangles(final Graphics g, final MapRects r, final float scale);

  /**
   * Returns the content for the specified pre value.
   * @param data data reference
   * @param mr map rectangle
   * @return byte[] content
   */
  static byte[] content(final Data data, final MapRect mr) {
    return ViewData.content(data, mr.pre, false);
  }
}
