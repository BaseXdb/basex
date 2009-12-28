package org.basex.gui.view.tree;

import static org.basex.gui.GUIConstants.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.gui.view.ViewRect;
import org.basex.gui.view.tree.TreeCaching.TreeRect;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public final class TreeView extends View implements TreeViewOptions {
  /** Current font height. */
  private int fontHeight;
  /** Array-list of array-list with the current rectangles. */
  private TreeCaching cache;

  /** Current mouse position x. */
  private int mousePosX = -1;
  /** Current mouse position y. */
  private int mousePosY = -1;
  /** Window width. */
  private int wwidth = -1;
  /** Window height. */
  private int wheight = -1;
  /** Currently focused rectangle. */
  private TreeRect focusedRect;
  /** Level of currently focused rectangle. */
  private int focusedRectLevel = -1;
  /** Current Image of visualization. */
  private BufferedImage treeImage;
  /** Depth of the document. */
  // private int documentDepth = -1;
  /** Notified focus flag. */
  private boolean refreshedFocus;
  /** Notified mark flag. */
  @SuppressWarnings("unused")
  private boolean refreshedMark;

  /** Distance between the levels. */
  private static int levelDistance = -1;
  /** Image of the current marked nodes. */
  private BufferedImage markedImage;
  /** If something is selected. */
  private boolean selection;
  /** The selection rectangle. */
  private ViewRect selectRect;
  /** The node height. */
  private int nodeHeight = -1;

  /**
   * Default Constructor.
   * @param man view manager
   */
  public TreeView(final ViewNotifier man) {
    super(TREEVIEW, null, man);
    new BaseXPopup(this, GUIConstants.POPUP);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  public void refreshFocus() {
    refreshedFocus = true;
    repaint();
  }

  @Override
  public void refreshInit() {
    if(!visible()) return;
    wwidth = -1;
    wheight = -1;
    cache = new TreeCaching(gui.context.data);
    repaint();
  }

  @Override
  public void refreshLayout() {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  public void refreshMark() {
    markNodes();
    repaint();
  }

  @Override
  public void refreshUpdate() {
    repaint();
  }

  @Override
  public boolean visible() {
    return gui.prop.is(GUIProp.SHOWTREE);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    g.setFont(GUIConstants.font);

    // timer
    // final Performance perf = new Performance();
    // perf.initTimer();

    // initializes sizes
    nodeHeight = fontHeight = g.getFontMetrics().getHeight();

    setLevelDistance();

    if(windowSizeChanged()) {

      if(markedImage != null) {
        markedImage = null;
        refreshedMark = true;
      }

      focusedRect = null;
      createNewMainImage();

    }

    g.drawImage(treeImage, 0, 0, getWidth(), getHeight(), this);

    // highlights marked nodes
    // if(refreshedMark) markNodes();

    if(markedImage != null) g.drawImage(markedImage, 0, 0, getWidth(),
        getHeight(), this);

    // highlights the focused node

    if(focus()) {
      highlightNode(g, focusedRect, focusedRectLevel, -1, false,
          SHOW_ANCESTORS, SHOW_DESCENDANTS);
    }

    if(selection) markSelektedNodes(g);

  }

  /**
   * Creates new image and draws rectangles in it.
   */
  private void createNewMainImage() {
    treeImage = createImage();
    final Graphics tIg = treeImage.getGraphics();

    cache.generateRects(tIg, gui.context, getWidth());

    for(int i = 0; i < cache.maxLevel; i++) {
      final TreeRect[] lr = cache.getTreeRectsPerLevel(i);
      final int y = getYperLevel(i);

      for(final TreeRect r : lr) {
        drawRectangle(tIg, i, r.x, y, r.w, nodeHeight, BORDER_RECTANGLES,
            FILL_RECTANGLES, DRAW_RECTANGLE);
      }
    }
  }

  /**
   * Draws Rectangles.
   * @param g the graphics reference
   * @param l level
   * @param x x coordinate
   * @param w width
   * @param y y coordinate
   * @param h the height
   * @param border draw rectangle border
   * @param fill fill rectangle
   * @param type the use type
   */
  private void drawRectangle(final Graphics g, final int l, final int x,
      final int y, final int w, final int h, final boolean border,
      final boolean fill, final byte type) {

    Color borderColor = null;
    Color fillColor = null;

    switch(type) {
      case DRAW_RECTANGLE:
        borderColor = getColorPerLevel(l, false);
        fillColor = getColorPerLevel(l, true);
        break;
      case DRAW_HIGHLIGHT:
        borderColor = GUIConstants.colormark1;
        fillColor = GUIConstants.colormark2;
    }
    if(border) {
      g.setColor(borderColor);
      g.drawRect(x + BORDER_PADDING, y, w - BORDER_PADDING, h);
    }

    if(fill) {
      g.setColor(fillColor);
      g.fillRect(x + FILL_PADDING, y, w - FILL_PADDING, h);
    }
  }

  /**
   * Returns draw Color.
   * @param l the current level
   * @param fill if true it returns fill color, rectangle color else
   * @return draw color
   */
  private Color getColorPerLevel(final int l, final boolean fill) {
    final int till = l < CHANGE_COLOR_TILL ? l : CHANGE_COLOR_TILL;
    return fill ? COLORS[till] : COLORS[till + 2];
  }

  /**
   * Draws the dragged selection and marks the nodes inside.
   * @param g the graphics reference
   */
  private void markSelektedNodes(final Graphics g) {
    final int x = selectRect.w < 0 ? selectRect.x + selectRect.w : selectRect.x;
    final int y = selectRect.h < 0 ? selectRect.y + selectRect.h : selectRect.y;
    final int w = Math.abs(selectRect.w);
    final int h = Math.abs(selectRect.h);

    // draw selection
    g.setColor(Color.RED);
    g.drawRect(x, y, w, h);

    final int f = y;
    final int t = y + h;
    final int size = cache.maxLevel;
    final IntList list = new IntList();

    for(int i = 0; i < size; i++) {
      final int yL = getYperLevel(i);

      if((yL >= f || yL + nodeHeight >= f) &&
          (yL <= t || yL + nodeHeight <= t)) {

        final TreeRect[] rl = cache.getTreeRectsPerLevel(i);
        final int s = cache.getSizePerLevel(i);

        if(cache.isBigRectangle(i)) {
          final TreeRect mRect = rl[0];
          int sPrePos = (int) (s * (x / (double) mRect.w));
          int ePrePos = (int) (s * ((x + w) / (double) mRect.w));

          if(sPrePos < 0) sPrePos = 0;
          if(ePrePos >= s) ePrePos = s - 1;

          for(int j = sPrePos; j < ePrePos; j++) {
            list.add(cache.getPrePerLevelAndIndex(s, j));
          }

          gui.notify.mark(new Nodes(list.finish(), gui.context.data), this);
          markNodes();
          repaint();

        } else {

          boolean b = false;

          for(int j = 0; j < s; j++) {
            final TreeRect tr = rl[j];

            if(!b && tr.contains(x)) b = true;

            if(b && tr.contains(x + w)) {
              list.add(tr.pre);
              break;
            }

            if(b) {
              list.add(tr.pre);
            }

          }

          gui.notify.mark(new Nodes(list.finish(), gui.context.data), this);
          markNodes();
          repaint();
        }

      }

    }
  }

  /**
   * Creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
        Transparency.TRANSLUCENT);
  }

  /**
   * Highlights the marked nodes.
   */
  private void markNodes() {
    refreshedMark = false;
    markedImage = createImage();
    final Graphics mIg = markedImage.getGraphics();

    final int size = gui.context.marked.size();
    final int[] marked = Arrays.copyOf(gui.context.marked.sorted, size);

    for(int k = 0; k < cache.maxLevel; k++) {

      final int y = getYperLevel(k);
      final TreeRect[] rL = cache.getTreeRectsPerLevel(k);

      if(cache.isBigRectangle(k)) {
        final TreeRect currRect = rL[0];

        for(int j = 0; j < size; j++) {
          final int pre = marked[j];

          if(pre == -1) continue;

          final int index = cache.searchPreArrayPosition(j, pre);

          if(index > -1) {

            final int x = (int) (currRect.w * index /
                (double) cache.getSizePerLevel(k));

            mIg.setColor(Color.RED);
            mIg.drawLine(x, y, x, y + nodeHeight);
            marked[j] = -1;

          }

        }

      } else {

        for(int j = 0; j < size; j++) {
          final int pre = marked[j];

          final TreeRect rect = cache.searchRect(j, pre);

          if(rect != null) {
            mIg.setColor(Color.RED);
            mIg.fillRect(rect.x + 1, y, rect.w - 1, nodeHeight);
            drawTextIntoRectangle(mIg, pre, rect.x + (int) (rect.w / 2f),
                rect.w, y);
            marked[j] = -1;
          }
        }
      }
    }
  }

  /**
   * Highlights nodes.
   * @param g the graphics reference
   * @param r the rectangle to highlight
   * @param l the level
   * @param childX the child's x value
   * @param fillNodes fill nodes
   * @param showParent show parent
   * @param showChildren show children
   */
  private void highlightNode(final Graphics g, final TreeRect r, final int l,
      final int childX, final boolean fillNodes, final boolean showParent,
      final boolean showChildren) {

    if(l == -1) return;

    final int y = getYperLevel(l);
    final int h = nodeHeight;

    final Data data = gui.context.data;
    final int pre = r.pre;
    final int kind = data.kind(pre);
    final int size = data.size(pre, kind);
    int multiPreX = -1;

    if(fillNodes) {
      drawRectangle(g, l, r.x, y, r.w, h, false, true, DRAW_HIGHLIGHT);
    } else {
      drawRectangle(g, l, r.x, y, r.w, h, true, false, DRAW_HIGHLIGHT);
    }

    if(cache.isBigRectangle(l)) {
      final int index = cache.searchPreArrayPosition(l, pre);
      final double ratio = index / (double) (cache.getSizePerLevel(l) - 1);
      multiPreX = (int) (r.w * ratio);
      g.drawLine(multiPreX, y, multiPreX, y + nodeHeight);
    }

    if(childX > -1) {
      g.drawLine(childX, getYperLevel(l + 1) - 1,
          multiPreX == -1 ? (2 * r.x + r.w) / 2 :
            multiPreX, y + nodeHeight + 1);
    }

    if(showParent && pre > 0) {
      final int par = data.parent(pre, kind);
      final int lv = l - 1;
      TreeRect parRect = null;

      if(lv >= 0) {
        // TreeRect[] rList = cache.getTreeRectsPerLevel(lv);

        if(cache.isBigRectangle(lv)) {
          // final TreeRect mPreRect = rList[0];
          // [WM] mPreRect.pre = par;

          // highlightNode(g, mPreRect, lv, multiPreX == -1 ? (2 * r.x + r.w) /
          // 2
          // : multiPreX, false, true, false);

        } else {

          parRect = cache.searchRect(lv, par);

          if(parRect != null) {
            highlightNode(g, parRect, lv, multiPreX == -1 ? (2 * r.x + r.w) / 2
                : multiPreX, true, true, false);
          }
        }
      }
    }

    if(showChildren && size > 1 && l + 1 < cache.maxLevel) {

      final int lv = l + 1;
      @SuppressWarnings("unused")
      final TreeRect[] rL = cache.getTreeRectsPerLevel(lv);

      final ChildIterator chIt = new ChildIterator(data, pre);

      if(cache.isBigRectangle(lv)) {
        // [WM]
        // TreeRect cRect = rList.get(0);
        //
        // int firstChildPre = chIt.pre;
        // int lastChildPre = -1;
        //
        // while(chIt.more())
        // lastChildPre = chIt.next();
        //
        // final int drawFrom = searchPreArrayPosition(cRect.multiPres,
        // firstChildPre);
        //
        // final int drawTo = firstChildPre == lastChildPre ? drawFrom
        // : searchPreArrayPosition(cRect.multiPres, lastChildPre);
        //
        // final int mPreLength = cRect.multiPres.length - 1;
        // int cx = (int) (cRect.w * drawFrom / (double) mPreLength);
        // int cw = (int) (cRect.w * drawTo / (double) mPreLength);
        //
        // g.setColor(descendantHighlightColor);
        // g.fillRect(cx, getYperLevel(l), Math.max(cw - cx, 2), nodeHeight);

        // highlightNode(g, descendantHighlightColor, cRect, l, -1, true, false,
        // true);

      } else {

        while(chIt.more()) {

          final TreeRect sRect = cache.searchRect(lv, chIt.next());

          g.setColor(new Color(0x38323D4F, true));
          final int parX = multiPreX == -1 ? (2 * r.x + r.w) / 2 : multiPreX;
          g.fillPolygon(new int[] { parX, sRect.x, sRect.x + sRect.w, sRect.x,
              sRect.x + sRect.w}, new int[] { getYperLevel(l) + nodeHeight,
              getYperLevel(lv) - 1, getYperLevel(lv) - 1,
              getYperLevel(lv) + nodeHeight, getYperLevel(lv) + nodeHeight}, 5);

          highlightNode(g, sRect, lv, -1, true, false, true);
        }
      }
    }

    if(!(showParent && showChildren)) return;

    final String s = cache.getText(data, pre);

    final int w = BaseXLayout.width(g, s);

    if(r.pre == 0) {

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
    if(refreshedFocus) {
      final int pre = gui.context.focused;

      for(int i = 0; i < cache.maxLevel; i++) {

        if(cache.isBigRectangle(i)) {
          final int index = cache.searchPreArrayPosition(i, pre);

          if(index > -1) {
            focusedRect = cache.getTreeRectsPerLevel(i)[0];
            focusedRectLevel = i;

            refreshedFocus = false;
            return true;
          }

        } else {

          final TreeRect rect = cache.searchRect(i, pre);

          if(rect != null) {
            focusedRect = rect;
            focusedRectLevel = i;

            refreshedFocus = false;
            return true;
          }
        }
      }
    } else {

      final int level = getLevelPerY(mousePosY);

      if(level == -1 || level >= cache.maxLevel) return false;

      final TreeRect[] rL = cache.getTreeRectsPerLevel(level);

      for(final TreeRect r : rL) {
        if(r.contains(mousePosX)) {

          focusedRect = r;
          focusedRectLevel = level;
          int pre = r.pre;

          // if multiple pre values, then approximate pre value
          if(cache.isBigRectangle(level)) {
            final double ratio = mousePosX / (double) r.w;
            final int index = (int)
            ((cache.getSizePerLevel(level) - 1) * ratio);
            pre = cache.getPrePerLevelAndIndex(level, index);
            r.pre = pre;
          }

          gui.notify.focus(pre, this);

          refreshedFocus = false;
          return true;
        }

      }
    }
    refreshedFocus = false;
    return false;
  }

  /**
   * Returns the y-axis value for a given level.
   * @param level the level
   * @return the y-axis value
   */
  private int getYperLevel(final int level) {
    return level * nodeHeight + level * levelDistance + TOP_MARGIN;
  }

  /**
   * Determines the level of a y-axis value.
   * @param y the y-axis value
   * @return the level if inside a node rectangle, -1 else
   */
  private int getLevelPerY(final int y) {
    final double f = y / ((float) levelDistance + nodeHeight);
    final double b = nodeHeight / (float) (levelDistance + nodeHeight);
    return f <= ((int) f + b) ? (int) f : -1;
  }

  /**
   * Determines the optimal distance between the levels.
   */
  private void setLevelDistance() {
    final int levels = gui.context.data.meta.height + 1;
    final int height = getSize().height - 1;
    final int heightLeft = height - levels * nodeHeight;
    final int lD = (int) (heightLeft / (double) levels);
    levelDistance = lD < MINIMUM_LEVEL_DISTANCE ? MINIMUM_LEVEL_DISTANCE : lD;
  }

  /**
   * Returns true if window-size has changed.
   * @return window-size has changed
   */
  private boolean windowSizeChanged() {
    if((wwidth > -1 && wheight > -1)
        && (getHeight() == wheight && getWidth() == wwidth)) return false;

    wheight = getHeight();
    wwidth = getWidth();
    return true;
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;
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
    final boolean right = !left;
    if(!right && !left || focusedRect == null) return;

    if(left) {
      gui.notify.mark(0, null);
      if(e.getClickCount() > 1 && focusedRect.pre > 0) {
        gui.notify.context(gui.context.marked, false, this);
        refreshContext(false, false);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(gui.updating || gui.context.focused == -1) return;
    if(e.getWheelRotation() > 0) gui.notify.context(new Nodes(
        gui.context.focused, gui.context.data), false, null);
    else gui.notify.hist(false);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(gui.updating || e.isShiftDown()) return;

    if(!selection) {
      selection = true;
      selectRect = new ViewRect();
      selectRect.x = e.getX();
      selectRect.y = e.getY();
      selectRect.h = 1;
      selectRect.w = 1;

    } else {
      final int x = e.getX();
      final int y = e.getY();

      selectRect.w = x - selectRect.x;
      selectRect.h = y - selectRect.y;

    }
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(gui.updating || gui.painting) return;
    selection = false;
    repaint();
  }

  /**
   * Draws text into the rectangles.
   * @param g graphics reference
   * @param pre the pre value
   * @param boxMiddle the middle of the rectangle
   * @param w the width of the
   * @param y the y value
   */
  private void drawTextIntoRectangle(final Graphics g, final int pre,
      final int boxMiddle, final int w, final int y) {

    if(w < MIN_SPACE) return;

    final Data data = gui.context.data;
    final int nodeKind = data.kind(pre);
    String s = "";

    if(data.meta.deepfs) {

      // [WM]
      if(data.fs.isFile(pre)) s = Token.string(data.fs.name(pre));
      else s = "hi";

    } else {

      switch(nodeKind) {
        case Data.ELEM:
          s = Token.string(data.name(pre, nodeKind));
          g.setColor(Color.BLACK);
          break;
        case Data.COMM:
          s = Token.string(data.text(pre, true));
          g.setColor(Color.GREEN);
          break;
        case Data.PI:
          s = Token.string(data.text(pre, true));
          g.setColor(Color.PINK);
          break;
        case Data.DOC:
          s = Token.string(data.text(pre, true));
          g.setColor(Color.BLACK);
          break;
        default:
          s = Token.string(data.text(pre, true));
          g.setColor(new Color(0x000F87));
      }
    }

    int textWidth = 5;

    while((textWidth = BaseXLayout.width(g, s)) + 4 > w) {
      s = s.substring(0, s.length() / 2).concat("*");
    }
    g.drawString(s, boxMiddle - textWidth / 2, y + nodeHeight - 2);
  }

}
