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
import org.basex.gui.view.ViewData;
import org.basex.gui.view.ViewNotifier;
import org.basex.gui.view.ViewRect;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public final class TreeView extends View implements TreeViewOptions {
  /** Current font height. */
  private int fontHeight;
  /** TreeCaching Object, contains cached pre values. */
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
  /** Notified focus flag. */
  private boolean refreshedFocus;
  /** Notified mark flag. */
  private boolean refreshedMark;
  /** Distance between the levels. */
  private static int levelDistance;
  /** Image of the current marked nodes. */
  private BufferedImage markedImage;
  /** If something is selected. */
  private boolean selection;
  /** The selection rectangle. */
  private ViewRect selectRect;
  /** The node height. */
  private int nodeHeight;
  /** Top margin. */
  private int topMargin;

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
      cache.generateBordersAndRects(g, gui.context, getWidth());
      createNewMainImage();
    }

    g.drawImage(treeImage, 0, 0, getWidth(), getHeight(), this);

    // highlights marked nodes
    if(refreshedMark) markNodes();

    if(markedImage != null) g.drawImage(markedImage, 0, 0, getWidth(),
        getHeight(), this);

    // highlights the focused node
    if(focus()) {
      final int focused = gui.context.focused;

      highlightNode(g, 0, focused, focusedRect, focusedRectLevel, -1, false,
          SHOW_ANCESTORS, SHOW_DESCENDANTS);

      if(SHOW_EXTRA_INFO) {
        g.setColor(new Color(0xeeeeee));
        g.fillRect(0, getHeight() - fontHeight - 6, getWidth(), fontHeight + 6);

        final Data d = gui.context.data;
        final int k = d.kind(focused);
        final int s = d.size(focused, k);
        final int as = d.attSize(focused, k);

        g.setColor(Color.BLACK);
        g.drawString("level: " + focusedRectLevel + "  level-size: "
            + cache.getLevelSize(0, focusedRectLevel) + "  node-size: "
            + (s - as) + "  node: "
            + Token.string(ViewData.tag(gui.prop, d, focused)) + "  pre: "
            + focused, 2, getHeight() - 6);
      }
    }

    if(selection) markSelektedNodes(g);

  }

  /**
   * Creates new image and draws rectangles in it.
   */
  private void createNewMainImage() {
    treeImage = createImage();
    final Graphics tIg = treeImage.getGraphics();

    final int rn = 0;

    for(int i = 0; i < cache.getHeight(rn); i++) {

      final TreeRect[] lr = cache.getTreeRectsPerLevel(rn, i);

      for(final TreeRect r : lr) {
        drawRectangle(tIg, i, r.x, r.w, nodeHeight, BORDER_RECTANGLES,
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
   * @param h height
   * @param border draw rectangle border
   * @param fill fill rectangle
   * @param type use type
   */
  private void drawRectangle(final Graphics g, final int l, final int x,
      final int w, final int h, final boolean border, final boolean fill,
      final byte type) {

    final int y = getYperLevel(l);

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

    // [WM]

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
    final int rn = 0;

    for(int i = 0; i < size; i++) {
      final int yL = getYperLevel(i);

      if((yL >= f || yL + nodeHeight >= f) && (yL <= t ||
          yL + nodeHeight <= t)) {

        final TreeRect[] rl = cache.getTreeRectsPerLevel(rn, i);
        final int s = cache.getLevelSize(rn, i);

        if(cache.isBigRectangle(rn, i)) {
          final TreeRect mRect = rl[0];
          int sPrePos = (int) (s * (x / (double) mRect.w));
          int ePrePos = (int) (s * ((x + w) / (double) mRect.w));

          if(sPrePos < 0) sPrePos = 0;
          if(ePrePos >= s) ePrePos = s - 1;

          for(int j = sPrePos; j < ePrePos; j++) {
            list.add(cache.getPrePerIndex(0, s, j));
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
              list.add(cache.getPrePerIndex(rn, i, j));
              break;
            }

            if(b) {
              list.add(cache.getPrePerIndex(rn, i, j));
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

    if(gui.context.marked == null) return;

    final int size = gui.context.marked.size();

    final int[] marked = Arrays.copyOf(gui.context.marked.nodes, size);

    final int rn = 0;

    for(int i = 0; i < cache.getHeight(rn); i++) {

      final int y = getYperLevel(i);

      if(cache.isBigRectangle(rn, i)) {

        for(int j = 0; j < size; j++) {
          final int pre = marked[j];

          if(pre == -1) continue;

          final int index = cache.getPreIndex(rn, i, pre);

          if(index > -1) {

            final int x = (int) (getWidth() * index /
                (double) cache.getLevelSize(
                rn, i));

            mIg.setColor(Color.RED);
            mIg.drawLine(x, y, x, y + nodeHeight);
            marked[j] = -1;

          }
        }
      } else {

        for(int j = 0; j < size; j++) {
          final int pre = marked[j];

          final TreeRect rect = cache.searchRect(rn, i, pre);

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
   * @param rn root
   * @param pre pre
   * @param r the rectangle to highlight
   * @param l the level
   * @param childX the child's x value
   * @param fillNodes fill nodes
   * @param showParent show parent
   * @param showChildren show children
   */
  private void highlightNode(final Graphics g, final int rn, final int pre,
      final TreeRect r, final int l, final int childX, final boolean fillNodes,
      final boolean showParent, final boolean showChildren) {

    if(l == -1) return;

    final int y = getYperLevel(l);
    final int h = nodeHeight;

    final Data d = gui.context.data;
    final int kind = d.kind(pre);
    final int size = d.size(pre, kind);
    int multiPreX = -1;

    if(fillNodes) {
      drawRectangle(g, l, r.x, r.w, h, false, true, DRAW_HIGHLIGHT);
    } else {
      drawRectangle(g, l, r.x, r.w, h, true, false, DRAW_HIGHLIGHT);
    }

    if(cache.isBigRectangle(rn, l)) {
      final int index = cache.getPreIndex(rn, l, pre);
      final double ratio = index / (double) (cache.getLevelSize(rn, l) - 1);
      multiPreX = (int) (r.w * ratio);
      g.drawLine(multiPreX, y, multiPreX, y + nodeHeight);
    }

    if(childX > -1) {
      g.drawLine(childX, getYperLevel(l + 1) - 1,
          multiPreX == -1 ? (2 * r.x + r.w) / 2 :
            multiPreX, y + nodeHeight + 1);
    }

    if(showParent && pre > 0) {
      final int par = d.parent(pre, kind);
      final int lv = l - 1;
      TreeRect parRect = null;

      if(lv >= 0) {
        // TreeRect[] rList = cache.getTreeRectsPerLevel(lv);

        if(cache.isBigRectangle(rn, lv)) {
          // final TreeRect mPreRect = rList[0];
          // [WM] mPreRect.pre = par;

          // highlightNode(g, mPreRect, lv, multiPreX == -1 ? (2 * r.x + r.w) /
          // 2
          // : multiPreX, false, true, false);

        } else {

          parRect = cache.searchRect(rn, lv, par);

          if(parRect != null) {
            highlightNode(g, rn, par, parRect, lv,
                multiPreX == -1 ? (2 * r.x + r.w) / 2 : multiPreX, true, true,
                false);
          }
        }
      }
    }

    if(showChildren && size > 1 && l + 1 < cache.getHeight(rn)) {

      // @SuppressWarnings("unused")
      // final TreeRect[] rL = cache.getTreeRectsPerLevel(rn, lv);

      // System.out.println(pre);

      // TreeBorder[] sBo = cache.generateSubtreeBorders(d, pre);

      if(cache.isBigRectangle(rn, l + 1)) {
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

        // int lv = l + 1;
        //
        // if(gui.context.current.nodes[0] > 0)
        // System.out.println(gui.context.current.nodes[0]);
        //
        // for(int i = 1; i < sBo.length; i++) {
        // TreeBorder sub = sBo[i];
        //
        // for(int j = sub.start; j < sub.start + sub.size; j++) {
        //
        // int pi = cache.getPrePerIndex(rn, lv, j);
        //
        // final TreeRect sRect = cache.searchRect(rn, lv, pi);
        //
        // g.setColor(new Color(0x38323D4F, true));
        // final int parX = multiPreX == -1 ? (2 * r.x + r.w) / 2 : multiPreX;
        // g.fillPolygon(new int[] { parX, sRect.x, sRect.x + sRect.w,
        // sRect.x, sRect.x + sRect.w}, new int[] {
        // getYperLevel(l) + nodeHeight, getYperLevel(lv) - 1,
        // getYperLevel(lv) - 1, getYperLevel(lv) + nodeHeight,
        // getYperLevel(lv) + nodeHeight}, 5);
        //
        // highlightNode(g, rn, pi, sRect, lv, -1, true, false, false);
        // }
        // lv++;
        // }
      }

    }

    if(!(showParent && showChildren)) return;

    final String s = cache.getText(d, pre);

    final int w = BaseXLayout.width(g, s);

    if(pre == 0) {

      g.fillRect(r.x, y + fontHeight + 1, w + 2, fontHeight);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, y + fontHeight + h - 2);

    } else {
      g.fillRect(r.x, y - fontHeight, w + 2, fontHeight);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (y - h / (float) fontHeight) - 2);

    }
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  private boolean focus() {
    if(refreshedFocus) {
      final int pre = gui.context.focused;

      final int rn = 0;

      for(int i = 0; i < cache.getHeight(rn); i++) {

        if(cache.isBigRectangle(rn, i)) {
          final int index = cache.getPreIndex(rn, i, pre);

          if(index > -1) {
            focusedRect = cache.getTreeRectsPerLevel(rn, i)[0];
            focusedRectLevel = i;

            refreshedFocus = false;
            return true;
          }

        } else {

          final TreeRect rect = cache.searchRect(0, i, pre);

          if(rect != null) {
            focusedRect = rect;
            focusedRectLevel = i;

            refreshedFocus = false;
            return true;
          }
        }
      }
    } else {

      final int rn = 0;
      final int lv = getLevelPerY(mousePosY);

      if(lv < 0 || lv >= cache.getHeight(rn)) return false;

      final TreeRect[] rL = cache.getTreeRectsPerLevel(rn, lv);

      for(int i = 0; i < rL.length; i++) {
        final TreeRect r = rL[i];

        if(r.contains(mousePosX)) {
          focusedRect = r;
          focusedRectLevel = lv;
          int pre = cache.getPrePerIndex(rn, lv, i);

          // if multiple pre values, then approximate pre value
          if(cache.isBigRectangle(rn, lv)) {
            final double ratio = mousePosX / (double) r.w;
            final int index = (int) (cache.getLevelSize(0, lv) * ratio);
            pre = cache.getPrePerIndex(0, lv, index);
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
    return level * nodeHeight + level * levelDistance + topMargin;
  }

  /**
   * Determines the level of a y-axis value.
   * @param y the y-axis value
   * @return the level if inside a node rectangle, -1 else
   */
  private int getLevelPerY(final int y) {
    final double f = (y - topMargin) / ((float) levelDistance + nodeHeight);
    final double b = nodeHeight / (float) (levelDistance + nodeHeight);
    return f <= ((int) f + b) ? (int) f : -1;
  }

  /**
   * Sets optimal distance between levels.
   */
  private void setLevelDistance() {
    final int h = getHeight() - 5;
    final int lvs = gui.context.current.data.meta.height + 1;
    int lD = 0;
    while((lD = (int) ((h - lvs * nodeHeight) /
        (double) (lvs - 1))) < MINIMUM_LEVEL_DISTANCE
        && nodeHeight > MINIMUM_NODE_HEIGHT)
      nodeHeight--;
    levelDistance = lD < MINIMUM_LEVEL_DISTANCE ? MINIMUM_LEVEL_DISTANCE
        : lD > MAXIMUM_LEVEL_DISTANCE ? MAXIMUM_LEVEL_DISTANCE : lD;
    final int ih = (int) ((h - (levelDistance *
        (lvs - 1) + lvs * nodeHeight)) / 2d);
    topMargin = ih < TOP_MARGIN ? TOP_MARGIN : ih;
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
    repaint();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final boolean right = !left;
    if(!right && !left || focusedRect == null) return;

    if(left) {
      gui.notify.mark(0, null);
      if(e.getClickCount() > 1) {
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
