package org.basex.gui.view.tree;

/**
 * This interface contains options for the tree view.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
public interface TreeViewOptions {
  // Options
  /** Use ChildIterator to cache nodes. */
  boolean USE_CHILDITERATOR = false;
  /** Draw only element nodes. */
  boolean ONLY_ELEMENT_NODES = true;
  /** Show ancestor nodes. */
  boolean SHOW_ANCESTORS = true;
  /** Show descendant nodes. */
  boolean SHOW_DESCENDANTS = true;
  /** Show descendant connection. */
  boolean SHOW_DESCENDANTS_CONN = true;
  /** Draw rectangle border. */
  boolean BORDER_RECTANGLES = true;
  /** Fill rectangles. */
  boolean FILL_RECTANGLES = true;
  /** Slim rectangles to text length. */
  boolean SLIM_TO_TEXT = true;
  /** Show extra node information. */
  boolean SHOW_EXTRA_INFO = false;
  /** Draw node text. */
  boolean DRAW_NODE_TEXT = true;
  /** Show 3d descendant connection. */
  boolean SHOW_3D_CONN = false;
  /** Border padding value. */
  int BORDER_PADDING = 2;
  /** Margin to top. */
  int TOP_MARGIN = 2;
  /** Changes Color until given level. */
  int CHANGE_COLOR_TILL = 4;

  /** Minimum rectangle space for text. */
  int MIN_TXT_SPACE = 25;
  /** Minimum space between the levels. */
  int MIN_LEVEL_DISTANCE = 4;
  /** Maximum level distance. */
  int MAX_LEVEL_DISTANCE = 40;
  /** Minimum node height. */
  int MIN_NODE_HEIGHT = 1;
  /** Maximum node height. */
  int MAX_NODE_HEIGHT = 20;
  /** Minimum node distance to draw node connections. */
  int MIN_NODE_DIST_CONN = 10;
  /** Draw kind rectangle. */
  byte DRAW_RECTANGLE = 0x00;
  /** Draw kind highlighting. */
  byte DRAW_HIGHLIGHT = 0x01;
  /** Draw mark. */
  byte DRAW_MARK = 0x02;
  /** Draw kind descendant highlighting. */
  byte DRAW_DESCENDANT = 0x03;
  /** Draw kind parent highlighting. */
  byte DRAW_PARENT = 0x04;
}
