package org.basex.gui.view.tree;

/**
 * This interface contains options for the tree view.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public interface TreeViewOptions {
  // Options
  /** Use ChildIterator to cache nodes. */
  boolean USE_CHILDITERATOR = true;
  /** Draw only element nodes. */
  boolean ONLY_ELEMENT_NODES = false;
  /** Show ancestor nodes. */
  boolean SHOW_ANCESTORS = true;
  /** Show descendant nodes. */
  boolean SHOW_DESCENDANTS = true;
  /** Draw rectangle border. */
  boolean BORDER_RECTANGLES = true;
  /** Fill rectangles. */
  boolean FILL_RECTANGLES = false;
  /** Slim rectangles to text length. */
  boolean SLIM_TO_TEXT = true;
  /** Show extra node information. */
  boolean SHOW_EXTRA_INFO = true;
  /** Border padding value. */
  int BORDER_PADDING = 2;
  /** Filling padding value. */
  int FILL_PADDING = 1;
  /** Margin to top. */
  int TOP_MARGIN = 2;
  /** Changes Color until given level. */
  int CHANGE_COLOR_TILL = 7;

  /** Minimum space in rectangles needed for tags. */
  int MIN_SPACE = 25;
  /** Minimum space between the levels. */
  int MINIMUM_LEVEL_DISTANCE = 8;
  /** Maximum level distance. */
  int MAXIMUM_LEVEL_DISTANCE = 40;
  /** Minimum node height. */
  int MINIMUM_NODE_HEIGHT = 1;

  /** Draw kind rectangle. */
  byte DRAW_RECTANGLE = 0x00;
  /** Draw kind highlighting. */
  byte DRAW_HIGHLIGHT = 0x01;
}
