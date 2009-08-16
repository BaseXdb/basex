package org.basex.gui.view.tree;

import java.awt.Color;
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
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public final class TreeView extends View {
  /** Current font height. */
  private int fontHeight;
  /** The sum of all size values of a node line. */
  int sumNodeSizeInLine = 0;
  /** Array-list of array-list with the current rectangles. */
  private ArrayList<ArrayList<TreeRect>> rectsPerLevel = null;

  // Constants
  /** Color for nodes. **/
  int colorNode = 0xEDEFF7;
  /** Color for rectangles. **/
  int colorRect = 0xC9CFE7;
  /** Color distance per level. **/
  int colorDiff = 0x121008;
  /** Color marked nodes. **/
  Color markColor = new Color(0x035FC7);
  /** Color text-nodes. **/
  Color textColor = new Color(0x000F87);
  /** Color highlighted nodes. **/
  Color highlightColor = new Color(0x5D6FB7);
  /** Minimum space in rectangles needed for tags. **/
  int minSpace = 35;

  // Options
  /** Draw only element nodes. **/
  private boolean onlyElementNodes = false;
  /** Show parent node. **/
  private boolean showParentNode = true;
  /** Show children nodes. **/
  private boolean showChildNodes = false;

  /** Nodes in current line. */
  int nodeCount = 0;
  /** Current mouse position x. */
  private int mousePosX = -1;
  /** Current mouse position y. */
  private int mousePosY = -1;
  /** Window width. */
  private int wwidth = -1;
  /** Window height. */
  private int wheight = -1;
  /** Currently focused rectangle. */
  private TreeRect focusedRect = null;
  /** Level of currently focused rectangle. */
  int focusedRectLevel = -1;
  /** Current Image of visualization. */
  private BufferedImage treeImage = null;
  /** Depth of the document. **/
  // private int documentDepth = -1;
  /** Notified focus flag. **/
  boolean refreshedFocus = false;
  /** Height of the rectangles. */
  int nodeHeight = -1;
  /** Distance between the nodes. */
  int nodeDistance = 22;

  /**
   * Default Constructor.
   * @param man view manager
   */
  public TreeView(final ViewNotifier man) {
    super(null, man);
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
    refreshedFocus = true;
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
  public boolean visible() {
    return gui.prop.is(GUIProp.SHOWTREE);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    BaseXLayout.antiAlias(g, gui.prop);
    g.setColor(Color.BLACK);
    g.setFont(GUIConstants.font);

    // timer
    final Performance perf = new Performance();
    perf.initTimer();

    // initializes sizes
    fontHeight = g.getFontMetrics().getHeight();
    nodeHeight = fontHeight;

    final Data data = gui.context.data();
    // documentDepth = data.meta.height;

    if (windowSizeChanged()) {

      treeImage = createImage();
      final Graphics tIg = treeImage.getGraphics();

      if (rectsPerLevel == null) {
        rectsPerLevel = new ArrayList<ArrayList<TreeRect>>();
      } else {
        rectsPerLevel.clear();
      }

      final Nodes curr = gui.context.current();
      for (int i = 0; i < curr.size(); i++) {
        treeView(curr.nodes[i], tIg, i);

      }
    }
    g.drawImage(treeImage, 0, 0, getWidth(), getHeight(), this);

    // highlights the focused node
    if (focus()) {
      highlightNode(g, markColor, focusedRect, focusedRectLevel,
          showParentNode, showChildNodes);
    }

    // highlights marked nodes
    if (rectsPerLevel.size() > 0 && gui.context.marked().size() > 0) {

      for (int k = 0; k < rectsPerLevel.size(); k++) {

        int y = getYperLevel(k);

        Iterator<TreeRect> it = rectsPerLevel.get(k).iterator();

        while (it.hasNext()) {
          final TreeRect r = it.next();
          int size = gui.context.marked().size();
          final int[] markedNodes = new int[size];
          System.arraycopy(gui.context.marked().nodes, 0, markedNodes, 0, size);

          for (int j = 0; j < size; j++) {
            for (int z = 0; z < r.multiPres.length; z++) {
              if (r.multiPres[z] == markedNodes[j]) {

                g.setColor(Color.RED);

                g.fillRect(r.x + 1, y + 1, r.w - 1, nodeHeight - 1);
                drawTextIntoRectangle(g, data.kind(r.pre), r.pre, r.x
                    + (int) (r.w / 2f), r.w, y);

                if (size < 2) {
                  return;
                } else if (j < size - 1) {
                  markedNodes[j] = markedNodes[size - 1];
                }
                size--;
                break;
              }
            }

          }
        }
      }
    }
  }

  /**
   * Creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, getWidth()),
        Math.max(1, getHeight()), Transparency.TRANSLUCENT);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if (gui.updating)
      return;
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
    if (!right && !left || focusedRect == null)
      return;

    if (left) {
      gui.notify.mark(0, null);
      if (e.getClickCount() > 1 && focusedRect.pre > 0) {
        gui.notify.context(gui.context.marked(), false, this);
        refreshContext(false, false);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if (gui.updating || gui.focused == -1)
      return;
    if (e.getWheelRotation() > 0)
      gui.notify.context(new Nodes(gui.focused, gui.context.data()), false,
          null);
    else
      gui.notify.hist(false);
  }

  /**
   * Highlights nodes.
   * @param g the graphics reference.
   * @param c the color.
   * @param r the rect to highlight.
   * @param level the level.
   * @param showParent show parent.
   * @param showChildren show children.
   */
  private void highlightNode(final Graphics g, final Color c,
      final TreeRect r, final int level, final boolean showParent,
      final boolean showChildren) {

    if (level == -1)
      return;

    final int y = getYperLevel(level);
    final int h = nodeHeight;

    final Data data = gui.context.data();
    final int pre = r.pre;
    final int kind = data.kind(pre);

    g.setColor(c);
    g.drawRect(r.x, y, r.w, h);

    if (showParent && pre > 0) {
      int par = data.parent(pre, kind);
      int l = level - 1;
      TreeRect parRect = null;

      if (l >= 0)
        parRect = searchRect(l, par);

      if (parRect != null) {
        g.drawLine((2 * parRect.x + parRect.w) / 2, getYperLevel(l)
            + nodeHeight + 1, (2 * r.x + r.w) / 2, y - 1);
        highlightNode(g, Color.RED, parRect, l, false, false);
      }
    }

    if (showChildren && data.size(pre, data.kind(pre)) > 0) {
      // TODO
    }

    String s = "";

    if (s.length() > 0)
      s += " | ";

    if (kind == Data.ELEM) {
      s += Token.string(data.tag(pre));
    } else {
      s += Token.string(data.text(pre));
    }

    // for(int j = 0; j < data.attSize(pre, data.kind(pre)) - 1; j++) {
    // s += " " + Token.string(data.attName(pre + y + 1)) + "=" + "\""
    // + Token.string(data.attValue(pre + y + 1)) + "\" ";
    // }

    int w = BaseXLayout.width(g, s);
    g.setColor(highlightColor);

    if (r.pre == 0) {

      g.fillRect(r.x, y + fontHeight + 1, w + 2, h);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, y + fontHeight + h - 2);

    } else {

      g.fillRect(r.x, y - fontHeight, w + 2, h);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (y - h / (float) fontHeight) - 2);

    }
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  public boolean focus() {

    if (refreshedFocus) {

      for (int i = 0; i < rectsPerLevel.size(); i++) {
        TreeRect rect = searchRect(i, gui.focused);

        if (rect != null) {
          focusedRect = rect;
          focusedRectLevel = i;

          refreshedFocus = false;
          return true;
        }

      }

    } else {

      int level = getLevelperY(mousePosY);

      if (level == -1 || level >= rectsPerLevel.size())
        return false;

      final Iterator<TreeRect> it = rectsPerLevel.get(level).iterator();
      while (it.hasNext()) {
        final TreeRect r = it.next();

        if (r.contains(mousePosX)) {

          focusedRect = r;
          focusedRectLevel = level;

          gui.notify.focus(r.pre, this);

          refreshedFocus = false;
          return true;
        }

      }
    }

    refreshedFocus = false;
    focusedRect = null;
    return false;
  }

  /**
   * Returns the y-axis value for a given level.
   * @param level the level.
   * @return the y-axis value.
   */
  public int getYperLevel(final int level) {
    return level * nodeHeight + level * nodeDistance;
  }

  /**
   * Determines the level of a y-axis value.
   * @param y the y-axis value.
   * @return the level if inside a node rectangle, -1 else.
   */
  public int getLevelperY(final int y) {
    double f = y / ((float) nodeDistance + nodeHeight);
    double b = nodeHeight / (float) (nodeDistance + nodeHeight);
    return f <= ((int) f + b) ? (int) f : -1;
  }

  /**
   * Controls the node temperature drawing.
   * @param root the root node
   * @param g the graphics reference
   * @param rootNum number of current root
   */
  private void treeView(final int root, final Graphics g, final int rootNum) {
    final Data data = gui.context.data();
    int level = 0;
    sumNodeSizeInLine = data.meta.size;
    int[] parentList = { root };
    nodeCount = 1;

    while (nodeCount > 0) {
      drawNodes(g, level, rootNum, parentList);
      parentList = getNextNodeLine(parentList);
      level++;
    }
    rectsPerLevel.trimToSize();
  }

  /**
   * Saves node line in parentList.
   * @param parentList array with nodes of the line before.
   * @return array filled with nodes of the current line.
   */
  private int[] getNextNodeLine(final int[] parentList) {
    final Data data = gui.context.data();
    final int l = parentList.length;
    final IntList temp = new IntList();
    int sumNodeSize = 0;
    int nCount = 0; // counts nodes

    for (int i = 0; i < l; i++) {
      final int p = parentList[i];

      if (p == -1) {
        continue;
      }

      final ChildIterator iterator = new ChildIterator(data, p);

      if (i > 0) {
        temp.add(-1);
      }

      while (iterator.more()) {
        final int pre = iterator.next();

        if (!onlyElementNodes || data.kind(pre) == Data.ELEM) {
          temp.add(pre);
          ++nCount;
        }
        sumNodeSize += data.size(pre, data.kind(pre));
      }
    }
    nodeCount = nCount;
    sumNodeSizeInLine = sumNodeSize;
    return temp.finish();
  }

  /**
   * Draws nodes per line.
   * @param g graphics reference
   * @param level the current level
   * @param rootNum number of current root
   * @param nodeList the node list.
   */
  private void drawNodes(final Graphics g, final int level, final int rootNum,
      final int[] nodeList) {

    final int numberOfRoots = gui.context.current().nodes.length;
    final Data data = gui.context.data();
    final int size = nodeList.length;
    ArrayList<TreeRect> rects = new ArrayList<TreeRect>();
    final HashMap<Integer, Double> temp = new HashMap<Integer, Double>();

    final int y = getYperLevel(level);
    final int h = nodeHeight;

    int screenWidth = getSize().width - 1;
    final double width = screenWidth / numberOfRoots;
    double x = (int) (rootNum * width);
    double ratio = width / size;
    IntList preList = new IntList();
    int factor = 1;
    double currRatio = ratio;
    while (currRatio < 2) {
      currRatio *= ++factor;
    }

    for (int i = 0; i < size; i++) {

      double boxMiddle = x + ratio / 2f;
      double w = ratio;

      int pre = nodeList[i];

      if (pre == -1) {
        x += ratio;
        continue;
      }

      int nodeKind = data.kind(pre);
      int nodeSize = data.size(pre, nodeKind);

      if (nodeSize > 0)
        temp.put(pre, boxMiddle);

      preList.add(pre);

      w = Math.max(2, ratio);
      TreeRect rect = new TreeRect();
      rect.x = (int) x;
      rect.w = (int) w;
      rect.pre = pre;
      rect.multiPres = preList.finish();
      preList.reset();
      rects.add(rect);

      g.setColor(new Color(colorRect -
          ((level < 11 ? level : 11) * colorDiff)));
      g.drawRect((int) x + 2, y, (int) w - 2, h);

      // if(level > 0) {
      // TreeRect parRect = searchRect(level - 1, par);
      //
      // double parentMiddle = parRect.x + parRect.w / 2f;
      //
      // g.setColor(new Color(colorRect));
      // g.drawLine((int) boxMiddle, y - 1, (int) parentMiddle, y - fontHeight
      // + 1);
      //
      // // final int line = Math.round(fontHeight / 4f);
      // // g.drawLine((int) boxMiddle, y, (int) boxMiddle, y - line);
      // // g.drawLine((int) boxMiddle, y - line,
      // // (int) parentMiddle, y - line);
      // // g.drawLine((int) parentMiddle, y - line, (int) parentMiddle, y
      // // - fontHeight);
      // }

      g.setColor(new Color(colorNode -
          ((level < 11 ? level : 11) * colorDiff)));
      g.fillRect((int) x + 1, y, (int) w - 1, h);

      drawTextIntoRectangle(g, nodeKind, pre, (int) boxMiddle, (int) w, y);

      x += ratio;
    }

    rects.trimToSize();
    rectsPerLevel.add(rects);
  }

  /**
   * Draws text into the rectangles.
   * @param g graphics reference
   * @param nodeKind the node kind.
   * @param pre the pre value.
   * @param boxMiddle the middle of the rectangle.
   * @param w the width of the.
   * @param y the y value;
   */
  private void drawTextIntoRectangle(final Graphics g, final int nodeKind,
      final int pre, final int boxMiddle, final int w, final int y) {

    if (w < minSpace)
      return;

    final Data data = gui.context.data();
    String s = "";

    switch (nodeKind) {
    case Data.ELEM:
      s = Token.string(data.tag(pre));
      g.setColor(Color.BLACK);
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
      g.setColor(Color.BLACK);
      break;
    default:
      s = Token.string(data.text(pre));
      g.setColor(textColor);
    }

    Token.string(data.text(pre));
    int textWidth = 0;

    while ((textWidth = BaseXLayout.width(g, s)) + 4 > w) {
      s = s.substring(0, s.length() / 2).concat("*");
    }
    g.drawString(s, boxMiddle - textWidth / 2, y + fontHeight - 2);
  }

  /**
   * Returns true if window-size has changed.
   * @return window-size has changed
   */
  private boolean windowSizeChanged() {
    if ((wwidth > -1 && wheight > -1)
        && (getHeight() == wheight && getWidth() == wwidth))
      return false;

    wheight = getHeight();
    wwidth = getWidth();
    return true;
  }

  /**
   * Uses binary search to find the rectangle with given pre value.
   * @param level the level to be searched.
   * @param pre the pre value to be found.
   * @return the rectangle containing the given pre value, null else.
   */
  private TreeRect searchRect(final int level, final int pre) {
    ArrayList<TreeRect> rList = rectsPerLevel.get(level);

    TreeRect rect = null;
    int l = 0;
    int r = rList.size() - 1;

    while (r >= l && rect == null) {
      int m = l + (r - l) / 2;

      if (rList.get(m).pre < pre) {
        l = m + 1;
      } else if (rList.get(m).pre > pre) {
        r = m - 1;
      } else {
        rect = rList.get(m);
      }
    }

    return rect;
  }
}
