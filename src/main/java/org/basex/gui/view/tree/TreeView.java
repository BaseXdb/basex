package org.basex.gui.view.tree;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.SwingUtilities;
import org.basex.core.Context;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Wolfgang Miller
 */
public final class TreeView extends View implements TreeViewOptions {
  /** TreeBorders Object, contains cached pre values and borders. */
  private TreeSubtree sub;
  /** TreeRects Object, contains cached rectangles. */
  private TreeRects tr;
  /** Current font height. */
  private int fontHeight;
  /** Current mouse position x. */
  private int mousePosX = -1;
  /** Current mouse position y. */
  private int mousePosY = -1;
  /** Window width. */
  private int wwidth = -1;
  /** Window height. */
  private int wheight = -1;
  /** Window start. */
  private int wstart;
  /** Current Image of visualization. */
  private BufferedImage treeImage;
  /** Notified focus flag. */
  private boolean refreshedFocus;
  /** Distance between the levels. */
  private int levelDistance;
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
  /** Distance between multiple trees. */
  private double treedist;
  /** Currently focused rectangle. */
  private TreeRect frect;
  /** Level of currently focused rectangle. */
  private int flv = -1;
  /** Focused root number. */
  private int frn;
  /** Focused pre. */
  private int fpre;
  /** New tree initialization. */
  private byte paintType;
  /** Array with current root nodes. */
  private int[] roots;
  /** Not enough space. */
  private boolean nes;
  /** If something is in focus. */
  private boolean inFocus;
  /** If window-size changed. */
  private boolean winChange;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TreeView(final ViewNotifier man) {
    super(TREEVIEW, HELPTREE, man);
    new BaseXPopup(this, GUIConstants.POPUP);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    paintType = sub == null ? PAINT_NEW_INIT : PAINT_NEW_CONTEXT;
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
    paintType = PAINT_NEW_INIT;
    repaint();
  }

  @Override
  public void refreshLayout() {
    paintType = PAINT_NEW_WINDOW_SIZE;
    repaint();
  }

  @Override
  public void refreshMark() {
    if(!nes) {
      markNodes();
      repaint();
    }
  }

  @Override
  public void refreshUpdate() {
    paintType = PAINT_NEW_INIT;
    repaint();
  }

  @Override
  public boolean visible() {
    final boolean v = gui.prop.is(GUIProp.SHOWTREE);
    if(!v) {
      sub = null;
      tr = null;
      paintType = PAINT_NEW_INIT;
    }
    return v;
  }

  @Override
  public void visible(final boolean v) {
    gui.prop.set(GUIProp.SHOWTREE, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Context c = gui.context;
    final Data data = c.data;
    if(data == null) return;

    super.paintComponent(g);
    gui.painting = true;

    roots = gui.context.current.nodes;
    if(roots.length == 0) return;

    smooth(g);
    g.setFont(font);
    fontHeight = g.getFontMetrics().getHeight();

    if(paintType == PAINT_NEW_INIT) {
      sub = new TreeSubtree(data);
      tr = new TreeRects(gui.prop);
    }

    if(paintType == PAINT_NEW_INIT || paintType == PAINT_NEW_CONTEXT)
      sub.generateBorders(c);

    if((winChange = windowSizeChanged()) && paintType == -1
        || paintType == PAINT_NEW_INIT || paintType == PAINT_NEW_CONTEXT
        || paintType == PAINT_NEW_WINDOW_SIZE) {
      treedist = tr.generateRects(sub, g, c, wstart, wwidth);
      nes = treedist == -1;
      if(!nes) {
        markedImage = null;
        setLevelDistance();
        createNewMainImage();
        if(gui.context.marked.size() > 0) markNodes();
      }
    }

    if(nes) {
      drawNotEnoughSpaceMessage(g);
      return;
    }

    g.drawImage(treeImage, 0, 0, wwidth, wheight, this);

    if(selection) {
      if(selectRect != null) {
        // draw selection
        final int x = selectRect.w < 0 ? selectRect.x + selectRect.w
            : selectRect.x;
        final int y = selectRect.h < 0 ? selectRect.y + selectRect.h
            : selectRect.y;
        final int w = Math.abs(selectRect.w);
        final int h = Math.abs(selectRect.h);
        g.setColor(colormark1);
        g.drawRect(x, y, w, h);
      }
      markNodes();
    }

    if(markedImage != null) g.drawImage(markedImage, 0, 0, wwidth, wheight,
        this);

    // highlights the focused node
    inFocus = paintType < 0 && focus();

    if(inFocus && !winChange) {

      if(!refreshedFocus && tr.isBigRectangle(sub, frn, flv)) {
        final int f = getMostSizedNode(data, frn, flv, frect, fpre);
        if(f >= 0) fpre = f;
      }

      highlightNode(g, frn, flv, frect, fpre, -1, DRAW_HIGHLIGHT);

      if(SHOW_EXTRA_INFO) {
        g.setColor(new Color(0xeeeeee));
        g.fillRect(0, wheight - fontHeight - 6, wwidth, fontHeight + 6);
        final Data d = gui.context.data;
        final int k = d.kind(fpre);
        final int s = d.size(fpre, k);
        final int as = d.attSize(fpre, k);
        g.setColor(Color.BLACK);
        g.drawString("pre: " + fpre + " level: " + flv + "  level-size: "
            + sub.getLevelSize(frn, flv) + "  node-size: " + (s - as)
            + "  node: " + Token.string(ViewData.tag(gui.prop, d, fpre)), 2,
            wheight - 6);
      }
      refreshedFocus = false;
    }
    paintType = -1;
    gui.painting = false;
  }

  /**
   * Creates new image and draws rectangles in it.
   */
  private void createNewMainImage() {
    treeImage = createImage();
    final Graphics tg = treeImage.getGraphics();
    final int rl = roots.length;
    tg.setFont(font);
    smooth(tg);

    for(int rn = 0; rn < rl; ++rn) {

      final int h = sub.getSubtreeHeight(rn);

      for(int lv = 0; lv < h; ++lv) {

        final boolean br = tr.isBigRectangle(sub, rn, lv);
        final TreeRect[] lr = tr.getTreeRectsPerLevel(rn, lv);

        for(int i = 0; i < lr.length; ++i) {
          final TreeRect r = lr[i];
          final int pre = sub.getPrePerIndex(rn, lv, i);
          drawRectangle(tg, rn, lv, r, pre, DRAW_RECTANGLE);
        }

        if(br) {
          final TreeRect r = lr[0];
          final int ww = r.x + r.w - 1;
          final int x = r.x + 1;
          drawBigRectSquares(tg, lv, x, ww, 4);
        }
      }
      if(SHOW_CONN_MI) {
        final TreeRect rr = tr.getTreeRectPerIndex(rn, 0, 0);
        highlightDescendants(tg, rn, 0, rr, roots[rn], getRectCenter(rr),
            DRAW_CONN);
      }
    }
  }

  /**
   * Draws Message if not enough space to draw all roots.
   * @param g graphics reference
   */
  private void drawNotEnoughSpaceMessage(final Graphics g) {

    final int mw = wwidth / 2;
    final int mh = wheight / 2;
    final String message = "Not enough space";
    final int tw = BaseXLayout.width(g, message);
    final int x = mw - tw / 2;
    final int y = mh + fontHeight;
    g.setColor(Color.BLACK);
    g.drawString(message, x, y);
  }

  /**
   * Draws the squares inside big rectangles.
   * @param g graphics reference
   * @param lv level
   * @param x x-coordinate
   * @param w width
   * @param ss square-size
   */
  private void drawBigRectSquares(final Graphics g, final int lv, final int x,
      final int w, final int ss) {
    int xx = x;
    final int y = getYperLevel(lv);
    int nh = nodeHeight;
    g.setColor(GUIConstants.color2A);
    while(nh > 0) {
      nh -= ss;
      if(nh < 0) nh = 0;
      g.drawLine(xx, y + nh, w, y + nh);
    }

    while(xx < w) {
      xx = xx + ss - 1 < w ? xx + ss : xx + ss - 1;
      g.drawLine(xx, y, xx, y + nodeHeight);
    }
  }

  /**
   * Return boolean if position is marked or not.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return position is marked or not
   */
  private boolean isMarked(final int x, final int y) {
    if(markedImage != null) {
      final int h = markedImage.getHeight();
      final int w = markedImage.getWidth();
      if(y > h || y < 0 || x > w || x < 0) return false;
      final int marc = markedImage.getRGB(x, y);
      return colormark1.equals(marc) || colormarkA.equals(marc);
    }
    return false;
  }

  /**
   * Draws Rectangles.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @param pre pre
   * @param t type
   */
  private void drawRectangle(final Graphics g, final int rn, final int lv,
      final TreeRect r, final int pre, final byte t) {

    final int y = getYperLevel(lv);
    final int h = nodeHeight;
    final boolean br = tr.isBigRectangle(sub, rn, lv);
    boolean txt = !br && fontHeight <= h;
    boolean fill = false;
    boolean border = false;
    final int xx = r.x;
    final int ww = r.w;
    final boolean marked = isMarked(xx, y);

    Color borderColor = null;
    Color fillColor = null;
    Color textColor = Color.BLACK;

    switch(t) {
      case DRAW_RECTANGLE:
        borderColor = getColorPerLevel(lv, false);
        fillColor = getColorPerLevel(lv, true);
        txt = txt & DRAW_NODE_TEXT;
        border = BORDER_RECTANGLES;
        fill = FILL_RECTANGLES;
        break;
      case DRAW_HIGHLIGHT:
        borderColor = color5;
        final int alpha = 0xDD000000;
        final int rgb = GUIConstants.COLORCELL.getRGB();
        fillColor = new Color(rgb + alpha, true);
        if(h > 4) border = true;
        fill = !br && !marked;
        break;
      case DRAW_MARK:
        borderColor = h > 2 && r.w > 4 ? colormarkA : colormark1;
        fillColor = colormark1;
        border = true;
        fill = true;
        break;
      case DRAW_DESCENDANTS:
        final int alphaD = 0xDD000000;
        final int rgbD = COLORS[6].getRGB();
        fillColor = new Color(rgbD + alphaD, true);
        borderColor = COLORS[8];
        textColor = Color.WHITE;
        fill = !marked;
        border = true;
        if(h < 4) {
          fillColor = SMALL_SPACE_COLOR;
          borderColor = fillColor;
          txt = false;
        }
        break;
      case DRAW_PARENT:
        fillColor = COLORS[6];
        textColor = Color.WHITE;
        fill = !br && !marked;
        border = !br;
        if(h < 4) {
          fillColor = SMALL_SPACE_COLOR;
          borderColor = COLORS[8];
          txt = false;
        }
        break;
    }

    if(border) {
      g.setColor(borderColor);
      g.drawRect(xx, y, ww, h);
    }
    if(fill) {
      g.setColor(fillColor);
      g.fillRect(xx + 1, y + 1, ww - 1, h - 1);
    }

    if(txt && (fill || !FILL_RECTANGLES)) {
      g.setColor(textColor);
      drawRectangleText(g, rn, lv, r, pre);
    }
  }

  /**
   * Draws text into rectangle.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @param pre pre
   */
  private void drawRectangleText(final Graphics g, final int rn, final int lv,
      final TreeRect r, final int pre) {

    String s = Token.string(tr.getText(gui.context, rn, pre)).trim();
    if(r.w < BaseXLayout.width(g, s)
        && r.w < BaseXLayout.width(g, "..".concat(s.substring(s.length() - 1)))
            + MIN_TXT_SPACE) return;

    final int x = r.x;
    final int y = getYperLevel(lv);
    final int rm = x + (int) (r.w / 2f);

    int tw = BaseXLayout.width(g, s);

    if(tw > r.w) {
      s = s.concat("..");
      while((tw = BaseXLayout.width(g, s)) + MIN_TXT_SPACE > r.w
          && s.length() > 3) {
        s = s.substring(0, (s.length() - 2) / 2).concat("..");
      }
    }
    final int yy = (int) (y + (nodeHeight + fontHeight - 4) / 2d);
    g.drawString(s, (int) (rm - tw / 2d + BORDER_PADDING), yy);
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
   * Marks nodes inside the dragged selection.
   */
  private void markSelectedNodes() {
    final int x = selectRect.w < 0 ? selectRect.x + selectRect.w : selectRect.x;
    final int y = selectRect.h < 0 ? selectRect.y + selectRect.h : selectRect.y;
    final int w = Math.abs(selectRect.w);
    final int h = Math.abs(selectRect.h);

    final int t = y + h;
    final int size = sub.getMaxSubtreeHeight();
    final IntList list = new IntList();
    final int rl = roots.length;

    final int rs = getTreePerX(x);
    final int re = getTreePerX(x + w);

    for(int r = rs < 0 ? 0 : rs; r <= re; ++r) {
      for(int i = 0; i < size; ++i) {
        final int yL = getYperLevel(i);

        if(i < sub.getSubtreeHeight(r) && (yL >= y || yL + nodeHeight >= y)
            && (yL <= t || yL + nodeHeight <= t)) {

          final TreeRect[] rlv = tr.getTreeRectsPerLevel(r, i);
          final int s = sub.getLevelSize(r, i);

          if(tr.isBigRectangle(sub, r, i)) {

            if(rl > 1) {
              final TreeBorder tb = sub.getTreeBorder(r, i);
              final int si = tb.size;
              for(int n = 0; n < si; ++n) {
                list.add(sub.getPrePerIndex(r, i, n));
              }
            } else {
              final int mw = rlv[0].w;

              int sPrePos = (int) (s * (x - wstart) / (double) mw);
              int ePrePos = (int) (s * (x - wstart + w) / (double) mw);

              if(sPrePos < 0) sPrePos = 0;
              if(ePrePos >= s) ePrePos = s - 1;

              do {
                list.add(sub.getPrePerIndex(r, i, sPrePos));
              } while(sPrePos++ < ePrePos);
            }
          } else {
            for(int j = 0; j < s; ++j) {
              final TreeRect rect = rlv[j];
              if(rect.contains(x, w)) list.add(sub.getPrePerIndex(r, i, j));
            }
          }
        }
      }
    }
    gui.notify.mark(new Nodes(list.toArray(), gui.context.data), this);
  }

  /**
   * Creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, wwidth), Math.max(1, wheight),
        Transparency.TRANSLUCENT);
  }

  /**
   * Highlights the marked nodes.
   */
  private void markNodes() {
    markedImage = createImage();
    final Graphics mg = markedImage.getGraphics();
    smooth(mg);
    mg.setFont(font);

    final int[] mark = gui.context.marked.nodes;
    if(mark.length == 0) return;

    int rn = 0;
    while(rn < roots.length) {

      final LinkedList<Integer> marklink = new LinkedList<Integer>();
      for(int i = 0; i < mark.length; ++i)
        marklink.add(i, mark[i]);

      for(int lv = 0; lv < sub.getSubtreeHeight(rn); ++lv) {

        final int y = getYperLevel(lv);
        final ListIterator<Integer> li = marklink.listIterator();

        if(tr.isBigRectangle(sub, rn, lv)) {

          while(li.hasNext()) {
            final int pre = li.next();

            final TreeRect rect = tr.searchRect(sub, rn, lv, pre);
            final int ix = sub.getPreIndex(rn, lv, pre);

            if(ix > -1) {
              li.remove();
              final int x = (int) (rect.w * ix / (double) sub.getLevelSize(rn,
                  lv));
              mg.setColor(colormark1);
              mg.fillRect(rect.x + x, y, 2, nodeHeight + 1);
            }
          }
        } else {
          while(li.hasNext()) {
            final int pre = li.next();

            final TreeRect rect = tr.searchRect(sub, rn, lv, pre);

            if(rect != null) {
              li.remove();
              drawRectangle(mg, rn, lv, rect, pre, DRAW_MARK);
            }
          }
        }
      }
      ++rn;
    }
  }

  /**
   * Returns position inside big rectangle.
   * @param rn root
   * @param lv level
   * @param pre pre
   * @param r rectangle
   * @return position
   */
  private int getBigRectPosition(final int rn, final int lv, final int pre,
      final TreeRect r) {
    final int idx = sub.getPreIndex(rn, lv, pre);
    final double ratio = idx / (double) sub.getLevelSize(rn, lv);
    return r.x + (int) Math.round(r.w * ratio) + 1;
  }

  /**
   * Draws node inside big rectangle.
   * @param g the graphics reference
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @param pre pre
   * @return node center
   */
  private int drawNodeInBigRectangle(final Graphics g, final int rn,
      final int lv, final TreeRect r, final int pre) {
    final int y = getYperLevel(lv);
    final int np = getBigRectPosition(rn, lv, pre, r);
    g.setColor(nodeHeight < 4 ? SMALL_SPACE_COLOR : COLORS[7]);
    g.drawLine(np, y, np, y + nodeHeight);
    return np;
  }

  /**
   * Draws parent connection.
   * @param g the graphics reference
   * @param lv level
   * @param r rectangle
   * @param px parent x
   * @param brx bigrect x
   */
  private void drawParentConnection(final Graphics g, final int lv,
      final TreeRect r, final int px, final int brx) {
    final int y = getYperLevel(lv);
    g.setColor(COLORS[7]);
    g.drawLine(px, getYperLevel(lv + 1) - 1, brx == -1 ? (2 * r.x + r.w) / 2
        : brx, y + nodeHeight + 1);
  }

  /**
   * Highlights nodes.
   * @param g the graphics reference
   * @param rn root
   * @param pre pre
   * @param r rectangle to highlight
   * @param lv level
   * @param px parent's x value
   * @param t highlight type
   */
  private void highlightNode(final Graphics g, final int rn, final int lv,
      final TreeRect r, final int pre, final int px, final byte t) {

    if(lv == -1) return;

    final boolean br = tr.isBigRectangle(sub, rn, lv);
    final boolean isRoot = roots[rn] == pre;
    final int height = sub.getSubtreeHeight(rn);

    final Data d = gui.context.data;
    final int k = d.kind(pre);
    final int size = d.size(pre, k);

    // rectangle center
    int rc = -1;

    if(br) {
      rc = drawNodeInBigRectangle(g, rn, lv, r, pre);
    } else {
      drawRectangle(g, rn, lv, r, pre, t);
      rc = getRectCenter(r);
    }

    // draw parent node connection
    if(px > -1 && MIN_NODE_DIST_CONN <= levelDistance) drawParentConnection(g,
        lv, r, px, rc);

    // if there are ancestors draw them
    if(!isRoot) {
      final int par = d.parent(pre, k);
      final int lvp = lv - 1;
      final TreeRect parRect = tr.searchRect(sub, rn, lvp, par);
      if(parRect == null) return;
      highlightNode(g, rn, lvp, parRect, par, rc, DRAW_PARENT);
    }

    // if there are descendants draw them
    if((t == DRAW_CONN || t == DRAW_HIGHLIGHT) && size > 1 && lv + 1 < height)
      highlightDescendants(
        g, rn, lv, r, pre, rc, t);

    // draws node text
    if(t == DRAW_HIGHLIGHT) drawThumbnails(g, rn, lv, pre, r, isRoot);
  }

  /**
   * Draws thumbnails.
   * @param g the graphics reference
   * @param rn root
   * @param lv level
   * @param pre pre
   * @param r rect
   * @param isRoot is root node?
   */
  private void drawThumbnails(final Graphics g, final int rn, final int lv,
      final int pre, final TreeRect r, final boolean isRoot) {
    final int x = r.x;
    final int y = getYperLevel(lv);
    final int h = nodeHeight;
    final String s = Token.string(tr.getText(gui.context, rn, pre));
    final int w = BaseXLayout.width(g, s);

    g.setColor(COLORS[8]);

    if(isRoot) {
      g.fillRect(x, y + h, w + 2, fontHeight + 2);
      g.setColor(COLORS[6]);
      g.drawRect(x - 1, y + h + 1, w + 3, fontHeight + 1);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (y + h + (float) fontHeight) - 2);
    } else {
      g.fillRect(r.x, y - fontHeight, w + 2, fontHeight);
      g.setColor(COLORS[6]);
      g.drawRect(r.x - 1, y - fontHeight - 1, w + 3, fontHeight + 1);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (y - h / (float) fontHeight) - 2);
    }
  }

  /**
   * Highlights descendants.
   * @param g the graphics reference
   * @param rn root
   * @param lv level
   * @param r rectangle to highlight
   * @param pre pre
   * @param px parent's x value
   * @param t highlight type
   */
  private void highlightDescendants(final Graphics g, final int rn,
      final int lv, final TreeRect r, final int pre, final int px,
      final byte t) {

    final Data d = gui.context.current.data;
    final boolean br = tr.isBigRectangle(sub, rn, lv);

    if(!br && t != DRAW_CONN) drawRectangle(g, rn, lv, r, pre, t);

    final int lvd = lv + 1;
    final TreeBorder[] sbo = sub.subtree(d, pre);

    if(sub.getSubtreeHeight(rn) >= lvd && sbo.length >= 2) {
      final boolean brd = tr.isBigRectangle(sub, rn, lvd);

      if(brd) {
        drawBigRectDescendants(g, rn, lvd, sbo, px, t);
      } else {
        final TreeBorder bo = sbo[1];
        final TreeBorder bos = sub.getTreeBorder(rn, lvd);

        final int start = bo.start >= bos.start ? bo.start - bos.start
            : bo.start;

        for(int j = 0; j < bo.size; ++j) {

          final int dp = sub.getPrePerIndex(rn, lvd, j + start);

          final TreeRect dr = tr.getTreeRectPerIndex(rn, lvd, j + start);

          if(SHOW_DESCENDANTS_CONN && levelDistance >= MIN_NODE_DIST_CONN)
            drawDescendantsConn(
              g, lvd, dr, px, t);

          highlightDescendants(g, rn, lvd, dr, dp, getRectCenter(dr),
              t == DRAW_CONN ? DRAW_CONN : DRAW_DESCENDANTS);
        }
      }
    }
  }

  /**
   * Returns rectangle center.
   * @param r TreeRect
   * @return center
   */
  private int getRectCenter(final TreeRect r) {
    return (2 * r.x + r.w) / 2;
  }

  /**
   * Draws descendants for big rectangles.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param subt subtree
   * @param parc parent center
   * @param t type
   */
  private void drawBigRectDescendants(final Graphics g, final int rn,
      final int lv, final TreeBorder[] subt, final int parc, final byte t) {

    int lvv = lv;
    int cen = parc;
    int i;

    for(i = 1; i < subt.length && tr.isBigRectangle(sub, rn, lvv); ++i) {
      final TreeBorder bos = sub.getTreeBorder(rn, lvv);
      final TreeBorder bo = subt[i];

      final TreeRect r = tr.getTreeRectPerIndex(rn, lvv, 0);
      final int start = bo.start - bos.start;
      final double sti = start / (double) bos.size;
      final double eni = (start + bo.size) / (double) bos.size;

      final int df = r.x + (int) (r.w * sti);
      final int dt = r.x + (int) (r.w * eni);
      final int ww = Math.max(dt - df, 2);

      if(MIN_NODE_DIST_CONN <= levelDistance) drawDescendantsConn(g, lvv,
          new TreeRect(df, ww), cen, t);
      cen = (2 * df + ww) / 2;
      switch(t) {
        case DRAW_CONN:
          break;
        default:
          final int rgb = COLORS[7].getRGB();
          final int alpha = 0x33000000;
          g.setColor(nodeHeight < 4 ? SMALL_SPACE_COLOR : new Color(
              rgb + alpha, false));
          if(nodeHeight > 2) {
            g.drawRect(df, getYperLevel(lvv) + 1, ww, nodeHeight - 2);
          } else {
            g.drawRect(df, getYperLevel(lvv), ww, nodeHeight);
          }
      }

      if(lvv + 1 < sub.getSubtreeHeight(rn)
          && !tr.isBigRectangle(sub, rn, lvv + 1)) {
        final Data d = gui.context.current.data;
        for(int j = start; j < start + bo.size; ++j) {
          final int pre = sub.getPrePerIndex(rn, lvv, j);
          final int pos = getBigRectPosition(rn, lvv, pre, r);
          final int k = d.kind(pre);
          final int s = d.size(pre, k);
          if(s > 1) highlightDescendants(g, rn, lvv, r, pre, pos,
              t == DRAW_HIGHLIGHT || t == DRAW_DESCENDANTS ? DRAW_DESCENDANTS
                  : DRAW_CONN);
        }
      }
      ++lvv;
    }
  }

  /**
   * Returns connection color.
   * @param t type
   * @return color
   */
  private Color getConnectionColor(final byte t) {
    int alpha;
    int rgb;
    Color c;

    switch(t) {
      case DRAW_CONN:
        alpha = 0x20000000;
        rgb = COLORS[4].getRGB();
        c = new Color(rgb + alpha, true);
        break;
      default:
      case DRAW_DESCENDANTS:
        alpha = 0x60000000;
        rgb = COLORS[8].getRGB();
        c = new Color(rgb + alpha, true);
        break;
    }
    return c;
  }

  /**
   * Draws descendants connection.
   * @param g graphics reference
   * @param lv level
   * @param r TreeRect
   * @param parc parent center
   * @param t type
   */
  private void drawDescendantsConn(final Graphics g, final int lv,
      final TreeRect r, final int parc, final byte t) {

    final int pary = getYperLevel(lv - 1) + nodeHeight;
    final int prey = getYperLevel(lv) - 1;
    final int boRight = r.x + r.w + BORDER_PADDING - 2;
    final int boLeft = r.x + BORDER_PADDING;
    final int boTop = prey + 1;

    final Color c = getConnectionColor(t);

    if(SHOW_3D_CONN) {
      final int boBottom = prey + nodeHeight + 1;
      final int parmx = r.x + (int) ((boRight - boLeft) / 2d);
      final int dis = 0x111111;
      final int alpha = 0x20000000;
      final int rgb = COLORS[4].getRGB();
      final Color ca = new Color(rgb + alpha - dis, false);
      final Color cb = new Color(rgb + alpha + dis, false);
      // g.setColor(new Color(0x444444, false));

      g.setColor(cb);
      // g.setColor(new Color(0x666666, false));
      g.drawPolygon(new int[] { parc, boRight, boRight}, new int[] { pary,
          boBottom, boTop}, 3);

      // g.setColor(new Color(0x666666, false));
      g.drawPolygon(new int[] { parc, boLeft, boLeft}, new int[] { pary,
          boBottom, boTop}, 3);

      g.setColor(c);
      // g.setColor(new Color(0x555555, false));
      g.drawPolygon(new int[] { parc, boLeft, boRight}, new int[] { pary,
          boTop, boTop}, 3);

      g.setColor(Color.BLACK);

      if(parmx < parc) g.drawLine(boRight, boBottom, parc, pary);
      else if(parmx > parc) g.drawLine(boLeft, boBottom, parc, pary);

      // g.setColor(cb);
      // // g.setColor(new Color(0x666666, false));
      // g.drawLine(boRight, boTop, parc, pary);
      // g.drawLine(boLeft, boTop, parc, pary);
    } else {
      g.setColor(c);
      if(boRight - boLeft > 2) {
        g.fillPolygon(new int[] { parc, boRight, boLeft}, new int[] { pary,
            boTop, boTop}, 3);
      } else {
        g.drawLine((boRight + boLeft) / 2, boTop, parc, pary);
      }
    }
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  private boolean focus() {
    if(refreshedFocus) {
      fpre = gui.context.focused;

      for(int r = 0; r < roots.length; ++r) {
        for(int i = 0; i < sub.getSubtreeHeight(r); ++i) {

          if(tr.isBigRectangle(sub, r, i)) {
            final int index = sub.getPreIndex(r, i, fpre);

            if(index > -1) {
              frn = r;
              frect = tr.getTreeRectsPerLevel(r, i)[0];
              flv = i;
              return true;
            }
          } else {

            final TreeRect rect = tr.searchRect(sub, r, i, fpre);

            if(rect != null) {
              frn = r;
              frect = rect;
              flv = i;
              return true;
            }
          }
        }
      }
    } else {
      final int lv = getLevelPerY(mousePosY);
      if(lv < 0) return false;
      final int mx = mousePosX;

      final int rn = frn = getTreePerX(mx);
      final int h = sub.getSubtreeHeight(rn);

      if(h < 0 || lv >= h) return false;

      final TreeRect[] rL = tr.getTreeRectsPerLevel(rn, lv);

      for(int i = 0; i < rL.length; ++i) {
        final TreeRect r = rL[i];

        if(r.contains(mx)) {
          frect = r;
          flv = lv;
          int pre = -1;

          // if multiple pre values, then approximate pre value
          if(tr.isBigRectangle(sub, rn, lv)) {
            pre = tr.getPrePerXPos(sub, rn, lv, mx);
          } else {
            pre = sub.getPrePerIndex(rn, lv, i);
          }
          fpre = pre;
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
   * Determines tree number.
   * @param x x-axis value
   * @return tree number
   */
  private int getTreePerX(final int x) {
    return (int) ((x - wstart) / treedist);
  }

  /**
   * Determines the level of a y-axis value.
   * @param y the y-axis value
   * @return the level if inside a node rectangle, -1 else
   */
  private int getLevelPerY(final int y) {
    final double f = (y - topMargin) / ((float) levelDistance + nodeHeight);
    final double b = nodeHeight / (float) (levelDistance + nodeHeight);
    return f <= (int) f + b ? (int) f : -1;
  }

  /**
   * Sets optimal distance between levels.
   */
  private void setLevelDistance() {
    final int h = wheight - BOTTOM_MARGIN;
    int lvs = 0;
    for(int i = 0; i < roots.length; ++i) {
      final int th = sub.getSubtreeHeight(i);
      if(th > lvs) lvs = th;
    }
    nodeHeight = MAX_NODE_HEIGHT;
    int lD;
    while((lD = (int) ((h - lvs * nodeHeight) / (double) (lvs - 1))) <
        (nodeHeight <= BEST_NODE_HEIGHT ? MIN_LEVEL_DISTANCE
        : BEST_LEVEL_DISTANCE)
        && nodeHeight >= MIN_NODE_HEIGHT)
      nodeHeight--;
    levelDistance = lD < MIN_LEVEL_DISTANCE ? MIN_LEVEL_DISTANCE
        : lD > MAX_LEVEL_DISTANCE ? MAX_LEVEL_DISTANCE : lD;
    final int ih = (int) ((h - (levelDistance * (lvs - 1) + lvs * nodeHeight))
        / 2d);
    topMargin = ih < TOP_MARGIN ? TOP_MARGIN : ih;
  }

  /**
   * Returns true if window-size has changed.
   * @return window-size has changed
   */
  private boolean windowSizeChanged() {
    if(wwidth > -1 && wheight > -1 && getHeight() == wheight
        && getWidth() == wwidth + 2 * LEFT_AND_RIGHT_MARGIN) return false;
    wheight = getHeight();
    wstart = LEFT_AND_RIGHT_MARGIN;
    wwidth = getWidth() - 2 * LEFT_AND_RIGHT_MARGIN;
    return true;
  }

  /**
   * Returns number of hit nodes.
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @return size
   */
  private int getHitBigRectNodesNum(final int rn, final int lv,
      final TreeRect r) {
    final int w = r.w;
    final int ls = sub.getLevelSize(rn, lv);
    return Math.max(ls / Math.max(w, 1), 1);
  }

  /**
   * Returns most sized node.
   * @param d the data reference
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @param p pre
   * @return deepest node pre
   */
  private int getMostSizedNode(final Data d, final int rn, final int lv,
      final TreeRect r, final int p) {
    final int size = getHitBigRectNodesNum(rn, lv, r);
    final int idx = sub.getPreIndex(rn, lv, p);
    if(idx < 0) return -1;
    int dpre = -1;
    int si = 0;

    for(int i = 0; i < size; ++i) {
      final int pre = sub.getPrePerIndex(rn, lv, i + idx);
      final int k = d.kind(pre);
      final int s = d.size(pre, k);
      if(s > si) {
        si = s;
        dpre = pre;
      }
    }
    return dpre;
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
  public void mousePressed(final MouseEvent e) {
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final boolean right = !left;
    if(!inFocus || !right && !left || frect == null) return;

    if(left) {
      if(flv >= sub.getSubtreeHeight(frn)) return;

      if(tr.isBigRectangle(sub, frn, flv)) {
        final Nodes ns = new Nodes(gui.context.data);
        int sum = getHitBigRectNodesNum(frn, flv, frect);
        final int fix = sub.getPreIndex(frn, flv, fpre);
        if(fix + sum + 1 == sub.getLevelSize(frn, flv)) ++sum;
        final int[] m = new int[sum];
        for(int i = 0; i < sum; ++i) {
          final int pre = sub.getPrePerIndex(frn, flv, i + fix);
          if(pre == -1) break;
          m[i] = pre;
        }
        ns.union(m);
        gui.notify.mark(ns, null);
      } else {
        gui.notify.mark(0, null);
      }
      if(e.getClickCount() > 1) {
        gui.notify.context(gui.context.marked, false, this);
        refreshContext(false, false);
      }
    } else {
      if(!isMarked(mousePosX, mousePosY)) {
        gui.notify.mark(0, null);
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
    markSelectedNodes();
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(gui.updating || gui.painting) return;
    selection = false;
    repaint();
  }
}
