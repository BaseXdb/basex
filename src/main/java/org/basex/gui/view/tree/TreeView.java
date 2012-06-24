package org.basex.gui.view.tree;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class offers a real tree view.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Wolfgang Miller
 */
public final class TreeView extends View implements TreeConstants {
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
  private Refresh paintType = Refresh.INIT;
  /** Array with current root nodes. */
  private int[] roots;
  /** Not enough space. */
  private boolean nes;
  /** If something is in focus. */
  private boolean inFocus;
  /** Show attributes. */
  private boolean showAtts;
  /** Slim rectangles to text length. */
  private boolean slimToText;

  /**
   * Default constructor.
   * @param man view manager
   */
  public TreeView(final ViewNotifier man) {
    super(TREEVIEW, man);
    new BaseXPopup(this, POPUP);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    paintType = sub == null ? Refresh.INIT : Refresh.CONTEXT;
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
    paintType = Refresh.INIT;
    repaint();
  }

  @Override
  public void refreshLayout() {
    paintType = Refresh.RESIZE;
    repaint();
  }

  @Override
  public void refreshMark() {
    if(nes) return;
    markNodes();
    repaint();
  }

  @Override
  public void refreshUpdate() {
    paintType = Refresh.INIT;
    repaint();
  }

  @Override
  public boolean visible() {
    final boolean v = gui.gprop.is(GUIProp.SHOWTREE);
    if(!v) {
      sub = null;
      tr = null;
      paintType = Refresh.INIT;
    }
    return v;
  }

  @Override
  public void visible(final boolean v) {
    gui.gprop.set(GUIProp.SHOWTREE, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Context c = gui.context;
    final Data data = c.data();
    if(data == null) return;
    if(showAttsChanged()) paintType = Refresh.INIT;
    if(slimToTextChanged() && paintType == Refresh.VOID)
      paintType = Refresh.RESIZE;

    super.paintComponent(g);
    gui.painting = true;

    roots = gui.context.current().pres;
    if(roots.length == 0) return;

    for(int i = 0; !showAtts && i < roots.length; ++i) {
      if(roots[i] >= data.meta.size) break;
      if(data.kind(roots[i]) == Data.ATTR) {
        drawMessage(g, NO_ATTS);
        return;
      }
    }

    smooth(g);
    g.setFont(font);
    fontHeight = g.getFontMetrics().getHeight();

    if(paintType == Refresh.INIT) {
      sub = new TreeSubtree(data, showAtts);
      tr = new TreeRects();
    }

    if(paintType == Refresh.INIT || paintType == Refresh.CONTEXT)
      sub.generateBorders(c);

    // if window-size changed
    final boolean winChange = windowSizeChanged();
    if(winChange && paintType == Refresh.VOID
        || paintType == Refresh.INIT || paintType == Refresh.CONTEXT
        || paintType == Refresh.RESIZE) {
      treedist = tr.generateRects(sub, g, c, wstart, wwidth, slimToText);
      nes = treedist == -1;
      if(!nes) {
        markedImage = null;
        setLevelDistance();
        createMainImage();
        if(gui.context.marked.size() > 0) markNodes();
      }
    }

    if(nes) {
      drawMessage(g, NOT_ENOUGH_SPACE);
      return;
    }

    g.drawImage(treeImage, 0, 0, wwidth, wheight, this);

    if(selection) {
      if(selectRect != null) {
        // draw selection
        final int x = selectRect.w < 0 ? selectRect.x + selectRect.w : selectRect.x;
        final int y = selectRect.h < 0 ? selectRect.y + selectRect.h : selectRect.y;
        final int w = Math.abs(selectRect.w);
        final int h = Math.abs(selectRect.h);
        g.setColor(colormark1);
        g.drawRect(x, y, w, h);
      }
      markNodes();
    }

    if(markedImage != null) g.drawImage(markedImage, 0, 0, wwidth, wheight, this);

    // highlights the focused node
    inFocus = paintType == Refresh.VOID && focus();

    if(inFocus && !winChange) {
      if(!refreshedFocus && tr.bigRect(sub, frn, flv)) {
        final int f = getMostSizedNode(data, frn, flv, frect, fpre);
        if(f >= 0) fpre = f;
      }

      highlightNode(g, frn, flv, frect, fpre, -1, Draw.HIGHLIGHT);
      refreshedFocus = false;
    }
    paintType = Refresh.VOID;
    gui.painting = false;
  }

  /**
   * Creates new image and draws rectangles in it.
   */
  private void createMainImage() {
    treeImage = createImage();
    final Graphics tg = treeImage.getGraphics();
    final int rl = roots.length;
    tg.setFont(font);
    smooth(tg);

    for(int rn = 0; rn < rl; ++rn) {
      final int h = sub.subtreeHeight(rn);

      for(int lv = 0; lv < h; ++lv) {
        final boolean br = tr.bigRect(sub, rn, lv);
        final TreeRect[] lr = tr.getTreeRectsPerLevel(rn, lv);

        for(int i = 0; i < lr.length; ++i) {
          final TreeRect r = lr[i];
          final int pre = sub.prePerIndex(rn, lv, i);
          drawRectangle(tg, rn, lv, r, pre, Draw.RECTANGLE);
        }

        if(br) {
          final TreeRect r = lr[0];
          final int ww = r.x + r.w - 1;
          final int x = r.x + 1;
          drawBigRectSquares(tg, lv, x, ww, 4);
        }
      }

      final TreeRect rr = tr.getTreeRectPerIndex(rn, 0, 0);
      highlightDescendants(tg, rn, 0, rr, roots[rn], getRectCenter(rr), Draw.CONNECTION);
    }
  }

  /**
   * Displays Message if there is not enough space to draw all roots or an
   * attribute-context but no attributes in cache.
   * @param g graphics reference
   * @param t type
   */
  private void drawMessage(final Graphics g, final byte t) {
    final int mw = wwidth >> 1;
    final int mh = wheight >> 1;
    String message = "";
    switch(t){
      case NOT_ENOUGH_SPACE: message = NO_SPACE;
      break;
      case NO_ATTS: message = "Enable attributes in Tree Options.";
      break;
    }
    final int x = mw - (BaseXLayout.width(g, message) >> 1);
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
    g.setColor(color2A);
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
  private boolean marked(final int x, final int y) {
    if(markedImage != null) {
      final int h = markedImage.getHeight();
      final int w = markedImage.getWidth();
      if(y >= h || 0 > y || x >= w || 0 > x) return false;
      final Color markc = new Color(markedImage.getRGB(x, y));
      return markc.getRed() > 0 && markc.getBlue() == 0 && markc.getGreen()
      == 0;
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
   * @param t draw type
   */
  private void drawRectangle(final Graphics g, final int rn, final int lv,
      final TreeRect r, final int pre, final Draw t) {

    final int y = getYperLevel(lv);
    final int h = nodeHeight;
    final boolean br = tr.bigRect(sub, rn, lv);
    boolean txt = !br && fontHeight <= h;
    final boolean fill;
    boolean border = false;
    final int xx = r.x;
    final int ww = r.w;
    final boolean marked = marked(xx, y);

    Color borderColor = null;
    Color fillColor;
    Color textColor = Color.BLACK;

    switch(t) {
      case RECTANGLE:
        borderColor = getColorPerLevel(lv, false);
        fillColor = getColorPerLevel(lv, true);
        border = true;
        fill = true;
        break;
      case HIGHLIGHT:
        borderColor = color4;
        final int alpha = 0xDD000000;
        final int rgb = GUIConstants.LGRAY.getRGB();
        fillColor = new Color(rgb + alpha, true);
        if(h > 4) border = true;
        fill = !br && !marked;
        break;
      case MARK:
        borderColor = h > 2 && r.w > 4 ? colormark1A : colormark1;
        fillColor = colormark1;
        border = true;
        fill = true;
        break;
      case DESCENDANTS:
        final int alphaD = 0xDD000000;
        final int rgbD = color(6).getRGB();
        fillColor = new Color(rgbD + alphaD, true);
        borderColor = color(8);
        textColor = Color.WHITE;
        fill = !marked;
        border = true;
        if(h < 4) {
          fillColor = SMALL_SPACE_COLOR;
          borderColor = fillColor;
          txt = false;
        }
        break;
      case PARENT:
      default:
        fillColor = color(6);
        textColor = Color.WHITE;
        fill = !br && !marked;
        border = !br;
        if(h < 4) {
          fillColor = SMALL_SPACE_COLOR;
          borderColor = color(8);
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
    if(txt && fill) {
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

    String s = Token.string(TreeRects.getText(gui.context, rn, pre)).trim();
    if(r.w < BaseXLayout.width(g, s) && r.w < BaseXLayout.width(
        g, ".." + s.substring(s.length() - 1)) + MIN_TXT_SPACE) return;

    final int x = r.x;
    final int y = getYperLevel(lv);
    final int rm = x + (int) (r.w / 2f);

    int tw = BaseXLayout.width(g, s);

    if(tw > r.w) {
      s += "..";
      while((tw = BaseXLayout.width(g, s)) + MIN_TXT_SPACE > r.w
          && s.length() > 3) {
        s = s.substring(0, (s.length() - 2) / 2) + "..";
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
  private static Color getColorPerLevel(final int l, final boolean fill) {
    final int till = l < CHANGE_COLOR_TILL ? l : CHANGE_COLOR_TILL;
    return fill ? color(till) : color(till + 2);
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
    final int size = sub.maxSubtreeHeight();
    final IntList list = new IntList();
    final int rl = roots.length;

    final int rs = getTreePerX(x);
    final int re = getTreePerX(x + w);

    for(int r = rs < 0 ? 0 : rs; r <= re; ++r) {
      for(int i = 0; i < size; ++i) {
        final int yL = getYperLevel(i);

        if(i < sub.subtreeHeight(r) && (yL >= y || yL + nodeHeight >= y)
            && (yL <= t || yL + nodeHeight <= t)) {

          final TreeRect[] rlv = tr.getTreeRectsPerLevel(r, i);
          final int s = sub.levelSize(r, i);

          if(tr.bigRect(sub, r, i)) {

            if(rl > 1) {
              final TreeBorder tb = sub.treeBorder(r, i);
              final int si = tb.size;
              for(int n = 0; n < si; ++n) {
                list.add(sub.prePerIndex(r, i, n));
              }
            } else {
              final int mw = rlv[0].w;

              int sPrePos = (int) (s * (x - wstart) / (double) mw);
              int ePrePos = (int) (s * (x - wstart + w) / (double) mw);

              if(sPrePos < 0) sPrePos = 0;
              if(ePrePos >= s) ePrePos = s - 1;

              do {
                list.add(sub.prePerIndex(r, i, sPrePos));
              } while(sPrePos++ < ePrePos);
            }
          } else {
            for(int j = 0; j < s; ++j) {
              final TreeRect rect = rlv[j];
              if(rect.contains(x, w)) list.add(sub.prePerIndex(r, i, j));
            }
          }
        }
      }
    }
    gui.notify.mark(new Nodes(list.toArray(), gui.context.data()), this);
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

    final int[] mark = gui.context.marked.pres;
    if(mark.length == 0) return;

    int rn = 0;
    while(rn < roots.length) {

      final LinkedList<Integer> marklink = new LinkedList<Integer>();
      for(int i = 0; i < mark.length; ++i)
        marklink.add(i, mark[i]);

      for(int lv = 0; lv < sub.subtreeHeight(rn); ++lv) {

        final int y = getYperLevel(lv);
        final ListIterator<Integer> li = marklink.listIterator();

        if(tr.bigRect(sub, rn, lv)) {

          while(li.hasNext()) {
            final int pre = li.next();

            final TreeRect rect = tr.searchRect(sub, rn, lv, pre);
            final int ix = sub.preIndex(rn, lv, pre);

            if(ix > -1) {
              li.remove();
              final int x = (int) (rect.w * ix / (double) sub.levelSize(rn,
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
              drawRectangle(mg, rn, lv, rect, pre, Draw.MARK);
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
    final int idx = sub.preIndex(rn, lv, pre);
    final double ratio = idx / (double) sub.levelSize(rn, lv);
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
    g.setColor(nodeHeight < 4 ? SMALL_SPACE_COLOR : color(7));
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
    g.setColor(color(7));
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
      final TreeRect r, final int pre, final int px, final Draw t) {

    if(lv == -1) return;

    final boolean br = tr.bigRect(sub, rn, lv);
    final boolean root = roots[rn] == pre;
    final int height = sub.subtreeHeight(rn);

    final Data d = gui.context.data();
    final int k = d.kind(pre);
    final int size = d.size(pre, k);

    // rectangle center
    final int rc;

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
    if(!root) {
      final int par = d.parent(pre, k);
      final int lvp = lv - 1;
      final TreeRect parRect = tr.searchRect(sub, rn, lvp, par);
      if(parRect == null) return;
      highlightNode(g, rn, lvp, parRect, par, rc, Draw.PARENT);
    }

    // if there are descendants draw them
    if((t == Draw.CONNECTION || t == Draw.HIGHLIGHT) && size > 1 &&
        lv + 1 < height) highlightDescendants(g, rn, lv, r, pre, rc, t);

    // draws node text
    if(t == Draw.HIGHLIGHT) drawThumbnails(g, rn, lv, pre, r, root);
  }

  /**
   * Draws thumbnails.
   * @param g the graphics reference
   * @param rn root
   * @param lv level
   * @param pre pre
   * @param r rect
   * @param root root flag
   */
  private void drawThumbnails(final Graphics g, final int rn, final int lv,
      final int pre, final TreeRect r, final boolean root) {
    final int x = r.x;
    final int y = getYperLevel(lv);
    final int h = nodeHeight;
    final String s = Token.string(TreeRects.getText(gui.context, rn, pre));
    final int w = BaseXLayout.width(g, s);

    g.setColor(color(8));

    if(root) {
      g.fillRect(x, y + h, w + 2, fontHeight + 2);
      g.setColor(color(6));
      g.drawRect(x - 1, y + h + 1, w + 3, fontHeight + 1);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (y + h + (float) fontHeight) - 2);
    } else {
      g.fillRect(r.x, y - fontHeight, w + 2, fontHeight);
      g.setColor(color(6));
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
   * @param t draw type
   */
  private void highlightDescendants(final Graphics g, final int rn,
      final int lv, final TreeRect r, final int pre, final int px,
      final Draw t) {

    final Data d = gui.context.data();
    final boolean br = tr.bigRect(sub, rn, lv);

    if(!br && t != Draw.CONNECTION) drawRectangle(g, rn, lv, r, pre, t);

    final int lvd = lv + 1;
    final TreeBorder[] sbo = sub.subtree(d, pre);

    if(sub.subtreeHeight(rn) >= lvd && sbo.length >= 2) {
      final boolean brd = tr.bigRect(sub, rn, lvd);

      if(brd) {
        drawBigRectDescendants(g, rn, lvd, sbo, px, t);
      } else {
        final TreeBorder bo = sbo[1];
        final TreeBorder bos = sub.treeBorder(rn, lvd);

        final int start = bo.start >= bos.start ? bo.start - bos.start
            : bo.start;

        for(int j = 0; j < bo.size; ++j) {

          final int dp = sub.prePerIndex(rn, lvd, j + start);

          final TreeRect dr = tr.getTreeRectPerIndex(rn, lvd, j + start);

          if(levelDistance >= MIN_NODE_DIST_CONN)
            drawDescendantsConn(g, lvd, dr, px, t);

          highlightDescendants(g, rn, lvd, dr, dp, getRectCenter(dr),
              t == Draw.CONNECTION ? Draw.CONNECTION : Draw.DESCENDANTS);
        }
      }
    }
  }

  /**
   * Returns rectangle center.
   * @param r TreeRect
   * @return center
   */
  private static int getRectCenter(final TreeRect r) {
    return (2 * r.x + r.w) / 2;
  }

  /**
   * Draws descendants for big rectangles.
   * @param g graphics reference
   * @param rn root
   * @param lv level
   * @param subt subtree
   * @param parc parent center
   * @param t draw type
   */
  private void drawBigRectDescendants(final Graphics g, final int rn,
      final int lv, final TreeBorder[] subt, final int parc, final Draw t) {

    int lvv = lv;
    int cen = parc;
    int i;

    for(i = 1; i < subt.length && tr.bigRect(sub, rn, lvv); ++i) {
      final TreeBorder bos = sub.treeBorder(rn, lvv);
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
        case CONNECTION:
          break;
        default:
          final int rgb = color(7).getRGB();
          final int alpha = 0x33000000;
          g.setColor(nodeHeight < 4 ? SMALL_SPACE_COLOR : new Color(
              rgb + alpha, false));
          if(nodeHeight > 2) {
            g.drawRect(df, getYperLevel(lvv) + 1, ww, nodeHeight - 2);
          } else {
            g.drawRect(df, getYperLevel(lvv), ww, nodeHeight);
          }
      }

      if(lvv + 1 < sub.subtreeHeight(rn)
          && !tr.bigRect(sub, rn, lvv + 1)) {
        final Data d = gui.context.data();
        for(int j = start; j < start + bo.size; ++j) {
          final int pre = sub.prePerIndex(rn, lvv, j);
          final int pos = getBigRectPosition(rn, lvv, pre, r);
          final int k = d.kind(pre);
          final int s = d.size(pre, k);
          if(s > 1) highlightDescendants(g, rn, lvv, r, pre, pos,
              t == Draw.HIGHLIGHT || t == Draw.DESCENDANTS ?
                  Draw.DESCENDANTS : Draw.CONNECTION);
        }
      }
      ++lvv;
    }
  }

  /**
   * Returns connection color.
   * @param t draw type
   * @return color
   */
  private static Color getConnectionColor(final Draw t) {
    final int alpha;
    final int rgb;

    switch(t) {
      case CONNECTION:
        alpha = 0x20000000;
        rgb = color(4).getRGB();
        return new Color(rgb + alpha, true);
      default:
        alpha = 0x60000000;
        rgb = color(8).getRGB();
        return new Color(rgb + alpha, true);
    }
  }

  /**
   * Draws descendants connection.
   * @param g graphics reference
   * @param lv level
   * @param r TreeRect
   * @param parc parent center
   * @param t draw type
   */
  private void drawDescendantsConn(final Graphics g, final int lv,
      final TreeRect r, final int parc, final Draw t) {

    final int pary = getYperLevel(lv - 1) + nodeHeight;
    final int prey = getYperLevel(lv) - 1;
    final int boRight = r.x + r.w + BORDER_PADDING - 2;
    final int boLeft = r.x + BORDER_PADDING;
    final int boTop = prey + 1;

    final Color c = getConnectionColor(t);

    g.setColor(c);
    if(boRight - boLeft > 2) {
      g.fillPolygon(new int[] { parc, boRight, boLeft}, new int[] { pary,
          boTop, boTop}, 3);
    } else {
      g.drawLine((boRight + boLeft) / 2, boTop, parc, pary);
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
        for(int i = 0; i < sub.subtreeHeight(r); ++i) {

          if(tr.bigRect(sub, r, i)) {
            final int index = sub.preIndex(r, i, fpre);

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
      final int h = sub.subtreeHeight(rn);
      if(h < 0 || lv >= h) return false;

      final TreeRect[] rL = tr.getTreeRectsPerLevel(rn, lv);

      for(int i = 0; i < rL.length; ++i) {
        final TreeRect r = rL[i];

        if(r.contains(mx)) {
          frect = r;
          flv = lv;
          final int pre;

          // if multiple pre values, then approximate pre value
          if(tr.bigRect(sub, rn, lv)) {
            pre = tr.getPrePerXPos(sub, rn, lv, mx);
          } else {
            pre = sub.prePerIndex(rn, lv, i);
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
      final int th = sub.subtreeHeight(i);
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
   * Returns true if show attributes has changed.
   * @return show attributes has changed
   */
  private boolean showAttsChanged() {
    final GUIProp gprop = gui.gprop;
    if(gprop.is(GUIProp.TREEATTS) == showAtts) return false;
    showAtts =  !showAtts;
    return true;
  }

  /**
   * Returns true if slim to text has changed.
   * @return slim to text has changed
   */
  private boolean slimToTextChanged() {
    final GUIProp gprop = gui.gprop;
    if(gprop.is(GUIProp.TREESLIMS) == slimToText) return false;
    slimToText = !slimToText;
    return true;
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
    final int ls = sub.levelSize(rn, lv);
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
    final int idx = sub.preIndex(rn, lv, p);
    if(idx < 0) return -1;
    int dpre = -1;
    int si = 0;

    for(int i = 0; i < size; ++i) {
      final int pre = sub.prePerIndex(rn, lv, i + idx);
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
      if(flv >= sub.subtreeHeight(frn)) return;

      if(tr.bigRect(sub, frn, flv)) {
        final Nodes ns = new Nodes(gui.context.data());
        int sum = getHitBigRectNodesNum(frn, flv, frect);
        final int fix = sub.preIndex(frn, flv, fpre);
        if(fix + sum + 1 == sub.levelSize(frn, flv)) ++sum;
        final int[] m = new int[sum];
        for(int i = 0; i < sum; ++i) {
          final int pre = sub.prePerIndex(frn, flv, i + fix);
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
      if(!marked(mousePosX, mousePosY)) {
        gui.notify.mark(0, null);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(gui.updating || gui.context.focused == -1) return;
    if(e.getWheelRotation() <= 0) gui.notify.context(new Nodes(
        gui.context.focused, gui.context.data()), false, null);
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
