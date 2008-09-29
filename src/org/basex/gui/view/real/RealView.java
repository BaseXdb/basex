package org.basex.gui.view.real;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;

import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.query.fs.DirIterator;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller, Philipp Ziemer
 */
public final class RealView extends View {
  /** Var to show the scrollbar. */
  //private BaseXBar scroll = null;
  /** Horizontal Coords of the pointer. */
  private int pointerx;
  /** Vertical Coords of the pointer. */
  private int pointery;
  /** Current font height. */
  private int fontHeight;
  /** Distance from the node to the border. */
  private final int margin = 3;
  /** Arc Measures for the box. */
  //private final int arcWH = 10;
  /** Vertical Distance between node levels. */
  private final int lvdistance = 30;
  /** Distance of the tree from the top border. */
  private final int topdistance = 20;
  /** Color for element node. */
  private final Color elementColor = Color.BLACK;
  /** Color for attribute node. */
  private final Color attributeColor = Color.GREEN;
  /** Color for attribute node. */
  private final Color commentColor = Color.MAGENTA;
  /** Color for process instruction node. */
  private final Color piColor = Color.ORANGE;
  /** Color for text node. */
  private final Color textColor = Color.CYAN;
  /** multiplier for pre/post values. */
  private final int prePostMulti = 17;
  /** the sum of all size values of a node line. */
  private int sumNodeSizeInLine = 0;
  /** the list of all parent nodes of a node line. */
  private IntList parentList = null;
  /** array with position of the parent node. */
  private HashMap<Integer, Integer> parentPos = null;

  /**
   * Default Constructor.
   */
  public RealView() {
    super(null);
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    repaint();
  }

  @Override
  protected void refreshFocus() {
    repaint();
  }

  @Override
  protected void refreshInit() {
    repaint();
  }

  @Override
  protected void refreshLayout() {
    repaint();
  }

  @Override
  protected void refreshMark() {
    repaint();
  }

  @Override
  protected void refreshUpdate() {
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    /** Set the paint Component */
    super.paintComponent(g);
    BaseXLayout.antiAlias(g);
    g.setColor(Color.BLACK);
    g.setFont(GUIConstants.font);

    /** Timer */
    final Performance perf = new Performance();
    perf.initTimer();

    /** Initialize sizes */
    fontHeight = g.getFontMetrics().getHeight();

    /** Initialize the pointer */
    pointerx = getWidth() / 2;
    pointery = topdistance;

    //TODO: choose visualization here

    switch(3) {
      case 1:
        drawTree(g, 0, 0, 0, getWidth());
        break;
      case 2:
        drawPrePost(g, 1, 1);
        break;
      case 3:
        temperature(0, g);

    }

    //
    //  // TODO: overlap
    //

    //    System.out.println("Perf: " + perf.getTime());
  }

  /**
   * controls the node temperature drawing.
   * @param root the root node
   * @param g the graphics reference
   */
  private void temperature(final int root, final Graphics g) {
    final Data data = GUI.context.data();
    int level = 0;
    sumNodeSizeInLine = data.size;
    parentList = new IntList(root);
    parentPos = null;
    while(parentList.size > 0) {
      drawTemperature(g, level);
      getNextNodeLine();
      level++;
    }

  }

  /**
   * Saves node line in parentList.
   */
  private void getNextNodeLine() {
    final Data data = GUI.context.data();
    final int l = parentList.size;
    final IntList temp = new IntList();
    int sumNodeSize = 0;

    for(int i = 0; i < l; i++) {
      final int p = parentList.get(i);

      if(p == -1) {
        continue;
      }

      final DirIterator iterator = new DirIterator(data, p);

      if(i > 0) {
        temp.add(-1);
      }

      while(iterator.more()) {
        final int pre = iterator.next();
        if(data.kind(pre) == Data.ELEM) temp.add(pre);
        sumNodeSize += data.size(pre, data.kind(pre));
        //        System.out.print(Token.string(data.tag(pre)) + " ");
      }
      //      System.out.println();
    }
    parentList = temp;
    sumNodeSizeInLine = sumNodeSize;
  }

  /**
   * Draws node temperature per line.
   * @param g graphics reference
   * @param level the current level
   */
  private void drawTemperature(final Graphics g, final int level) {
    final Data data = GUI.context.data();
    final int size = parentList.size;
    final HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
    int x = 0;
    final int y = 1 * level * fontHeight * 2;
    final double width = this.getSize().width - 1;
    final int ratio = (int) Math.rint(width / size);
    final int minSpace = 39; // minimum Space for Tags
    final boolean space = ratio > minSpace ? true : false;

    for(int i = 0; i < size; i++) {
      final int pre = parentList.get(i);

      if(pre == -1) {
        x += ratio;
        continue;
      }

      final int nodeSize = data.size(pre, Data.ELEM);

      final double nodePercent = nodeSize / (double) sumNodeSizeInLine;
      g.setColor(Color.black);
      g.drawRect(x, y, ratio, fontHeight);
      int c = (int) Math.rint(255 * nodePercent * 40);
      c = c > 255 ? 255 : c;
      g.setColor(new Color(c, 0, 255 - c));
      g.fillRect(x + 1, y + 1, ratio - 1, fontHeight - 1);

      final int boxMiddle = x + Math.round(ratio / 2f);
      g.setColor(Color.black);

      if(space) {
        final String s = Token.string(data.tag(pre));
        final int textWidth = BaseXLayout.width(g, s);
        g.drawString(s, boxMiddle - textWidth / 2, y + fontHeight - 2);
      }

      //      System.out.println(boxMiddle);

      if(parentPos != null) {

        final int line = Math.round(fontHeight / 4f);
        final int parentMiddle = parentPos.get(data.parent(pre, Data.ELEM));
        g.drawLine(boxMiddle, y, boxMiddle, y - line);

        g.drawLine(boxMiddle, y - line, parentMiddle, y - line);
        g.drawLine(parentMiddle, y - line, parentMiddle, y - fontHeight);

      }

      if(nodeSize > 0) temp.put(pre, boxMiddle);

      x += ratio;
    }
    parentPos = temp;

    //    System.out.printf("%s\n", parentPos);

  }

  /**
   * Draws the node at the given Coordinates.
   * @param g graphics reference
   * @param pre pre value
   * @param x horizontal coordinate
   * @param y vertical coordinate
   * @param color node-color for different types
   */
  private void drawNode(final Graphics g, final int pre, final int x,
      final int y, final Color color) {
    final Data data = GUI.context.data();
    final String node = Token.string(data.tag(pre));
    final int textWidth = BaseXLayout.width(g, node);
    final int xstart = x - textWidth / 2;

    g.setColor(color);
    /** Defines the start of the painting */

    /** Draw String and Box */
    g.drawString(node, xstart, y);
    g.drawRect(xstart - margin, y - fontHeight, textWidth + 2 * margin,
        fontHeight + margin);

    //    g.drawRoundRect(xstart - margin, y - fontHeight, textWidth
    //    + 2 * margin,
    //        fontHeight + margin, arcWH, arcWH);
  }

  /**
   * Draws the Tree.
   * @param g graphics reference
   * @param root pre value of the current virtual root
   * @param lv current level
   * @param frameleft left border of the parent frame
   * @param space width of the parent frame
   */
  void drawTree(final Graphics g, final int root, final int lv,
      final int frameleft, final int space) {
    final Data data = GUI.context.data();
    final DirIterator iterator = new DirIterator(data, root);

    final int level = lv + 1;
    final int nodetype = Data.ELEM;
    int rootsize = data.size(root, nodetype);
    int pre;
    /** The x-value of the left frame border. */
    int border = frameleft;
    /** To engage inaccuracies during the measure method, this measures
     * the size that is left after a child got it's space. */
    int sizeleft = space;
    int childframewidth = -1;

    while(iterator.more()) {
      pre = iterator.next();

      if(data.kind(pre) == nodetype) {
        /** Measures the required space by the size value. */
        final double percent = (double) (data.size(pre, nodetype) + 1)
            / (double) rootsize;
        childframewidth = (int) (sizeleft * percent);

        pointerx = border + childframewidth / 2;
        pointery = topdistance + level * lvdistance;
        drawNode(g, pre, pointerx, pointery, elementColor);
        /** System.out.println(Token.string(data.tag(pre))
         *  + "; Percent: " + percent + "; Framewidth: "
         *  + getWidth() + "; framex1: " + (space - childframewidth)
         *  + "; space: " + space); */

        //        checkOverlap(g, pre, pointerx, pointery, elementColor);
        drawTree(g, pre, level, border, childframewidth);

        border += childframewidth;
        rootsize = rootsize - data.size(pre, nodetype) + 1;
        sizeleft = sizeleft - childframewidth;
      }

    }
  }

  /**
   * Calculates the post value of a node.
   * @param pre the pre value of the node
   * @param level the level of the node
   * @return calculated post value
   */
  private int calcPost(final int pre, final int level) {
    final Data data = GUI.context.data();
    return (data.size(pre, data.kind(pre)) - level) + pre;
  }

  /**
   * Draws pre and post values next to the node they belong to.
   * @param g graphics reference
   * @param pre the pre value
   * @param post the post value
   * @param textWidth the textWidth
   */
  void drawPreAndPostValues(final Graphics g, final int pre, final int post,
      final int textWidth) {
    final int x = pre * prePostMulti;
    final int y = post * prePostMulti;
    final int preStrLen = BaseXLayout.width(g, Integer.toString(pre) + " ");
    final int postStrLen = BaseXLayout.width(g, " ") + textWidth;
    final Font temp = getFont();
    final Font little = new Font(temp.getFontName(), Font.TRUETYPE_FONT,
        (int) (fontHeight / 1.5f));
    g.setFont(little);
    g.setColor(Color.BLUE);
    g.drawString(Integer.toString(pre), x - preStrLen, y + fontHeight / 4);
    g.setColor(Color.RED);
    g.drawString(Integer.toString(post), x + postStrLen, y + fontHeight / 4);
    g.setFont(temp);

  }

  /**
   * Draws the nodes for given pre and post value.
   * @param g graphics reference
   * @param pre the pre value for the node to draw
   * @param post the par value for the node to draw
   * @param level the level for the node to draw
   */
  private void drawPrePostNode(final Graphics g, final int pre, final int post,
      final int level) {
    final Data data = GUI.context.data();
    final int x = pre * prePostMulti;
    final int y = post * prePostMulti;
    int preTextWidth = -1;
    //int parTextWidth = -1;

    switch(data.kind(pre)) {
      case Data.ELEM:
        final String s = Token.string(data.tag(pre));
        g.drawString(s, x, y);
        preTextWidth = BaseXLayout.width(g, s);
        break;
      case Data.ATTR:
        g.setColor(attributeColor);
        g.drawString("A", x, y);
        preTextWidth = BaseXLayout.width(g, "A");
        break;
      case Data.COMM:
        g.setColor(commentColor);
        g.drawString("C", x, y);
        preTextWidth = BaseXLayout.width(g, "C");
        break;
      case Data.PI:
        g.setColor(piColor);
        g.drawString("PI", x, y);
        preTextWidth = BaseXLayout.width(g, "PI");
        break;
      case Data.TEXT:
        g.setColor(textColor);
        g.drawString("T", x, y);
        preTextWidth = BaseXLayout.width(g, "T");
    }
    drawPreAndPostValues(g, pre, post, preTextWidth);
    g.setColor(Color.BLACK);

    final int par = data.parent(pre, data.kind(pre));

    if(par > 0) {
      final int parPost = calcPost(par, level - 1);

      switch(data.kind(par)) {
        case Data.ELEM:
          //String s = Token.string(data.tag(pre));
          //parTextWidth = BaseXLayout.width(g, s);
          break;
        case Data.ATTR:
          //parTextWidth = BaseXLayout.width(g, "A");
          break;
        case Data.COMM:
          //parTextWidth = BaseXLayout.width(g, "C");
          break;
        case Data.PI:
          //parTextWidth = BaseXLayout.width(g, "PI");
          break;
        case Data.TEXT:
          //parTextWidth = BaseXLayout.width(g, "T");
      }

      final int parX = par * prePostMulti;
      final int parY = parPost * prePostMulti;
      //TODO: improve line connection between nodes.
      g.drawLine(parX, parY, x, y);
    }

  }

  /**
   * Draws pre-post-tree.
   * @param g graphics reference
   * @param pre the pre value of the current node
   * @param level the level of the current node
   */
  private void drawPrePost(final Graphics g, final int pre, final int level) {
    final Data data = GUI.context.data();
    final int post = calcPost(pre, level);
    System.out.println("pre: " + pre + " | post: " + post + " | tag: "
        + Token.string(data.tag(pre)) + " | level: " + level);
    final int lv = level;

    drawPrePostNode(g, pre, post, lv);

    final DirIterator iterator = new DirIterator(data, pre);

    while(iterator.more()) {
      final int newPre = iterator.next();

      drawPrePost(g, newPre, lv + 1);
    }

  }
}
