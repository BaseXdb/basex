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
   * @param data data reference
   * @return next color mark
   */
  final Color color(final MapRects rects, final int ri, final Data data) {
    // find marked node
    final DBNodes marked = view.gui.context.marked;
    if(marked != null) {
      final int pre = rects.get(ri).pre;
      final int m = -marked.find(pre) - 1;
      if(m >= 0) {
        // mark ancestor of invisible node
        final int r = rects.find(pre);
        if(m < marked.size()) {
          // find pre value of next rectangle
          final int e;
          if(r + 1 < rects.size) {
            e = rects.sorted[r + 1].pre;
          } else {
            // rectangle is last one: get rectangle size via database
            final int spre = rects.sorted[r].pre;
            e = data.size(spre, data.kind(spre));
          }
          // mark rectangle if pre value is its descendant
          return marked.sorted(m) < e ? GUIConstants.colormark2 : null;
        }
        return null;
      }
    }
    // no mark found
    return GUIConstants.colormark1;
  }

  /**
   * Draws the specified rectangles.
   * @param g graphics reference
   * @param r rectangles to be drawn
   */
  abstract void drawRectangles(Graphics g, MapRects r);

  /**
   * Returns textual contents for a rectangle.
   * @param data data reference
   * @param mr map rectangle
   * @return byte[] content
   */
  static byte[] text(final Data data, final MapRect mr) {
    return ViewData.text(data, mr.pre, false);
  }
}
