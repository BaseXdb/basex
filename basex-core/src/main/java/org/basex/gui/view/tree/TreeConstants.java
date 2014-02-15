package org.basex.gui.view.tree;

import java.awt.*;

/**
 * This interface contains tree view constants.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Wolfgang Miller
 */
interface TreeConstants {
  /** Border padding value. */
  int BORDER_PADDING = 2;
  /** Margin to top. */
  int TOP_MARGIN = 6;
  /** Margin to bottom. */
  int BOTTOM_MARGIN = 9;
  /** Margin left and right. */
  int LEFT_AND_RIGHT_MARGIN = 4;
  /** Changes Color until given level. */
  int CHANGE_COLOR_TILL = 4;
  /** Highlight Color small space. */
  Color SMALL_SPACE_COLOR = Color.GREEN;

  /** Minimum rectangle space for text. */
  int MIN_TXT_SPACE = 4;
  /** Minimum space between the levels. */
  int MIN_LEVEL_DISTANCE = 2;
  /** Optimal space between the levels. */
  int BEST_LEVEL_DISTANCE = 16;
  /** Maximum level distance. */
  int MAX_LEVEL_DISTANCE = 100;
  /** Best node height. */
  int BEST_NODE_HEIGHT = 8;
  /** Minimum node height. */
  int MIN_NODE_HEIGHT = 1;
  /** Maximum node height. */
  int MAX_NODE_HEIGHT = 26;
  /** Minimum node distance to draw node connections. */
  int MIN_NODE_DIST_CONN = 5;

  /** Refresh mode. */
  enum Refresh {
    /** New initialization. */ INIT,
    /** New context. */        CONTEXT,
    /** New window-size. */    RESIZE,
    /** Void. */               VOID
  }

  /** Draw kinds. */
  enum Draw {
    /** Rectangle. */    RECTANGLE,
    /** Highlighting. */ HIGHLIGHT,
    /** Mark. */         MARK,
    /** Descendants. */  DESCENDANTS,
    /** Parents. */      PARENT,
    /** Connections. */  CONNECTION
  }

  /** Show not enough space text. */
  byte NOT_ENOUGH_SPACE = 0;
  /** Show no attributes text. */
  byte NO_ATTS = 1;
}
