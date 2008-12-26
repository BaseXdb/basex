package org.basex.gui.view.real;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewRect;
import org.basex.query.ChildIterator;
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
  private HashMap<Integer, Double> parentPos = null;
  /** array of current rectangles. */
  private ArrayList<ViewRect[]> rects = null;
  /** nodes in current line. */
  private int rectCount = 0;
  /** current mouse position x. */
  private int mousePosX = -1;
  /** current mouse position y. */
  private int mousePosY = -1;
  /** window width. */
  private int wwidth = -1;
  /** window height. */
  private int wheight = -1;
  /** current focused rect. */
  private ViewRect focusedRect = null;
  /** current Image of visualization. */
  private BufferedImage realImage = null;

  /**
   * Default Constructor.
   */
  public RealView() {
    super(null);
    new BaseXPopup(this, GUIConstants.POPUP);
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshFocus() {
    repaint();
  }

  @Override
  protected void refreshInit() {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshLayout() {

    repaint();
  }

  @Override
  protected void refreshMark() {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshUpdate() {
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {

    final Data data = GUI.context.data();
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

    if(windowSizeChanged()) {

      switch(3) {
        case 1:
          drawTree(g, 0, 0, 0, getWidth());
          break;
        case 2:
          drawPrePost(g, 1, 1);
          break;
        case 3:
          realImage = createImage();
          Graphics rg = realImage.getGraphics();
          rects = new ArrayList<ViewRect[]>();
          Nodes curr = GUI.context.current();
          for(int i = 0; i < curr.size; i++) {
            temperature(curr.nodes[i], rg, i);
          }

      }
      focus();
    }
    g.drawImage(realImage, 0, 0, getWidth(), getHeight(), this);

    //highlights focused node
    if(focusedRect != null) {
      ViewRect r = focusedRect;
      g.drawRect(r.x, r.y, r.w, r.h);

      final int pre = r.pre;
      String s = "";

      if(data.kind(pre) == Data.ELEM) {
        s = Token.string(data.tag(pre));
      } else {
        s = Token.string(data.text(pre));
      }

      for(int y = 0; y < data.attSize(pre, data.kind(pre)) - 1; y++) {
        s += " " + Token.string(data.attName(pre + y + 1)) + "=" + "\""
            + Token.string(data.attValue(pre + y + 1)) + "\" ";
      }

      int w = BaseXLayout.width(g, s);

      g.setColor(Color.WHITE);
      g.fillRect(r.x, r.y - fontHeight, w, fontHeight);
      g.setColor(Color.BLACK);
      g.drawString(s, r.x, (int) (r.y - fontHeight / 4f));

    }

    //highlights marked nodes
    if(!rects.isEmpty() && GUI.context.marked().size > 0) {

      g.setColor(Color.GREEN);
      Iterator<ViewRect[]> it = rects.iterator();

      while(it.hasNext()) {
        final ViewRect[] r = it.next();
        int size = GUI.context.marked().size;
        final int[] markedNodes = new int[size];
        System.arraycopy(GUI.context.marked().nodes, 0, markedNodes, 0, size);

        for(int i = 0; i < r.length; i++) {

          for(int j = 0; j < size; j++) {
            if(r[i].pre == markedNodes[j]) {

              g.drawRect(r[i].x, r[i].y, r[i].w, r[i].h);

              if(size < 2) {
                return;
              } else if(j < size - 1) {
                markedNodes[j] = markedNodes[size - 1];
              }
              size--;
            }
          }
        }

      }

    }

  }

  /** creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
        Transparency.TRANSLUCENT);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(working) return;
    super.mouseMoved(e);
    // refresh mouse focus
    mousePosX = e.getX();
    mousePosY = e.getY();
    focus();
    repaint();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {

    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final boolean right = SwingUtilities.isRightMouseButton(e);
    if(!right && !left || focusedRect == null) return;

    if(left) {
      View.notifyMark(0, this);
      int pre = focusedRect.pre;
      if(e.getClickCount() > 1 && pre > -1) {
        View.notifyContext(GUI.context.marked(), false, this);
        refreshContext(false, false);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(working || focused == -1) return;
    if(e.getWheelRotation() > 0) notifyContext(new Nodes(focused, 
        GUI.context.data()), false, null);
    else notifyHist(false);
  }

  /**Finds rectangle at cursor position.
   * @return focused rectangle
   */
  public boolean focus() {
    //    int level = mousePosY / (fontHeight * 2);

    if(rects == null) return false;

    //    final Data data = GUI.context.data();
    final Iterator<ViewRect[]> it = rects.iterator();

    while(it.hasNext()) {
      final ViewRect[] r = it.next();

      for(int i = 0; i < r.length; i++) {
        if(r[i].contains(mousePosX, mousePosY)) {
          focusedRect = r[i];
          View.notifyFocus(r[i].pre, this);
          return true;
        }
      }

    }
    focusedRect = null;
    View.notifyFocus(-1, this);
    return false;
  }

  /**
   * controls the node temperature drawing.
   * @param root the root node
   * @param g the graphics reference
   * @param rootNum number of current root
   */
  private void temperature(final int root, 
      final Graphics g, final int rootNum) {
    final Data data = GUI.context.data();
    int level = 0;
    sumNodeSizeInLine = data.size;
    parentList = new IntList();
    parentList.add(root);
    rectCount = 1;
    parentPos = null;
    while(parentList.size > 0) {
      drawTemperature(g, level, rootNum);
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
    int rCount = 0; //counts nodes

    for(int i = 0; i < l; i++) {
      final int p = parentList.list[i];

      if(p == -1) {
        continue;
      }

      final ChildIterator iterator = new ChildIterator(data, p);

      if(i > 0) {
        temp.add(-1);
      }

      while(iterator.more()) {
        final int pre = iterator.next();
        //        if(data.kind(pre) == Data.ELEM) {
        temp.add(pre);
        ++rCount;
        //        }
        sumNodeSize += data.size(pre, data.kind(pre));
      }
    }
    rectCount = rCount;
    parentList = temp;
    sumNodeSizeInLine = sumNodeSize;
  }

  /**
   * Draws node temperature per line.
   * @param g graphics reference
   * @param level the current level
   * @param rootNum number of current root
   */
  private void drawTemperature(final Graphics g, final int level,
      final int rootNum) {

    final int numberOfRoots = GUI.context.current().nodes.length;
    final ViewRect[] tRect = new ViewRect[rectCount];
    final Data data = GUI.context.data();
    final int size = parentList.size;
    final HashMap<Integer, Double> temp = new HashMap<Integer, Double>();
    final int y = 1 * level * fontHeight * 2;
    final double width = (getSize().width - 1) / numberOfRoots;
    double x = (int) (rootNum * width);
    final double ratio = width / size;
    final int minSpace = 35; // minimum Space for Tags
    final boolean space = ratio > minSpace ? true : false;

    int r = 0;

    for(int i = 0; i < size; i++) {
      final int pre = parentList.list[i];

      if(pre == -1) {
        x += ratio;
        continue;
      }
      int nodeKind = data.kind(pre);

      final int nodeSize = data.size(pre, nodeKind);

      final double nodePercent = nodeSize / (double) sumNodeSizeInLine;
      g.setColor(Color.black);

      final int l = Math.max(2, (int) ratio); //rectangle length
      final int h = fontHeight; //rectangle height

      //g.drawRect((int) x, y, l, h);
      tRect[r++] = new ViewRect((int) x, y, l, h, pre, 0);

      int c = (int) Math.rint(255 * nodePercent * 40);
      c = c > 255 ? 255 : c;
      g.setColor(new Color(c, 0, 255 - c));
      g.fillRect((int) x + 1, y + 1, l - 1, h - 1);

      final double boxMiddle = x + ratio / 2f;

      if(space) {
        String s = "";

        switch(nodeKind) {
          case Data.ELEM:
            s = Token.string(data.tag(pre));
            g.setColor(Color.WHITE);
            break;
          case Data.COMM:
            s = Token.string(data.text(pre));
            g.setColor(Color.GREEN);
            break;
          case Data.PI:
            s = Token.string(data.text(pre));
            g.setColor(Color.PINK);
            break;
          case Data.DOC:
            s = Token.string(data.text(pre));
            g.setColor(Color.BLUE);
            break;
          default:
            s = Token.string(data.text(pre));
            g.setColor(Color.YELLOW);
        }

        Token.string(data.text(pre));
        int textWidth = 0;

        while((textWidth = BaseXLayout.width(g, s)) + 4 > ratio) {
          s = s.substring(0, s.length() / 2).concat("?");
        }

        g.drawString(s, (int) boxMiddle - textWidth / 2, y + fontHeight - 2);
      }

      if(parentPos != null) {

        final double parentMiddle = parentPos.get(data.parent(pre, nodeKind));

        g.setColor(new Color(192 + c / 4, 192, 255 - c / 4));
        g.drawLine((int) boxMiddle, y - 1, (int) parentMiddle, y - fontHeight
            + 1);

        //        final int line = Math.round(fontHeight / 4f);
        //        g.drawLine((int) boxMiddle, y, (int) boxMiddle, y - line);
        //        g.drawLine((int) boxMiddle, y - line, 
        //        (int) parentMiddle, y - line);
        //        g.drawLine((int) parentMiddle, y - line, (int) parentMiddle, y
        //            - fontHeight);

      }

      if(nodeSize > 0) temp.put(pre, boxMiddle);

      x += ratio;
    }
    rects.add(tRect);
    parentPos = temp;
  }

  /** Returns true if window-size has changed.
   * @return window-size has changed
   */
  boolean windowSizeChanged() {
    if((wwidth > -1 && wheight > -1)
        && (getHeight() == wheight && getWidth() == wwidth)) return false;

    wheight = getHeight();
    wwidth = getWidth();
    return true;
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
    final ChildIterator iterator = new ChildIterator(data, root);

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
      // [WM] improve line connection between nodes.
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
    final int lv = level;

    drawPrePostNode(g, pre, post, lv);

    final ChildIterator iterator = new ChildIterator(data, pre);

    while(iterator.more()) {
      final int newPre = iterator.next();

      drawPrePost(g, newPre, lv + 1);
    }

  }
}
