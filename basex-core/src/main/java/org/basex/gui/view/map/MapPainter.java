package org.basex.gui.view.map;

import java.awt.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.view.*;
import org.basex.query.value.seq.*;

/**
 * Provides an interface for data specific TreeMap visualizations.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
abstract class MapPainter {
  /** Graphics reference. */
  final MapView view;
  /** GUI options. */
  final GUIOptions gopts;

  /**
   * Constructor.
   * @param view map reference
   * @param gopts gui options
   */
  MapPainter(final MapView view, final GUIOptions gopts) {
    this.view = view;
    this.gopts = gopts;
  }

  /**
   * Returns color mark.
   * @param rects rectangles to be drawn
   * @param ri current position
   * @return next color mark
   */
  final Color color(final MapRects rects, final int ri) {
    // find marked node
    final DBNodes marked = view.gui.context.marked;
    if(marked != null) {
      final int pre = rects.get(ri).pre;
      final int m = -marked.find(pre) - 1;
      if(m >= 0) {
        // mark ancestor of invisible node
        final int r = rects.find(pre);
        return m < marked.size() && r + 1 < rects.size && marked.sorted(m) <
          rects.sorted[r + 1].pre ? GUIConstants.colormark2 : null;
      }
    }
    // no mark found
    return GUIConstants.colormark1;
  }

  /**
   * Draws the specified rectangles.
   * @param g graphics reference
   * @param r rectangles to be drawn
   * @param scale scale boarders using this factor
   */
  abstract void drawRectangles(Graphics g, MapRects r, float scale);

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
