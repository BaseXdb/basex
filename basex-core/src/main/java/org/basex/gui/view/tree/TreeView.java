package org.basex.gui.view.tree;

import static org.basex.gui.view.tree.TreeConstants.*;
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
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class offers a real tree view.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Wolfgang Miller
 */
public final class TreeView extends View {
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
  private int width = -1;
  /** Window height. */
  private int height = -1;
  /** Window start. */
  private int start;
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
   * @param notifier view notifier
   */
  public TreeView(final ViewNotifier notifier) {
    super(TREEVIEW, notifier);
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
    final boolean v = gui.gopts.get(GUIOptions.SHOWTREE);
    if(!v) {
      sub = null;
      tr = null;
      paintType = Refresh.INIT;
    }
    return v;
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWTREE, v);
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
    else if(slimToTextChanged() && paintType == Refresh.VOID) paintType = Refresh.RESIZE;

    super.paintComponent(g);

    gui.painting = true;
    try {
      final DBNodes nodes = gui.context.current();
      roots = nodes.pres();
      final int rl = roots.length;
      if(rl == 0) return;

      for(int i = 0; !showAtts && i < rl; ++i) {
        if(roots[i] >= data.meta.size) break;
        if(data.kind(roots[i]) == Data.ATTR) {
          drawMessage(g, NO_ATTS);
          return;
        }
      }

      BaseXLayout.antiAlias(g);
      g.setFont(font);
      fontHeight = g.getFontMetrics().getHeight();

      if(paintType == Refresh.INIT) {
        sub = new TreeSubtree(data, showAtts);
        tr = new TreeRects(this);
      }
      tr.nodes = nodes;
      tr.g = g;

      if(paintType == Refresh.INIT || paintType == Refresh.CONTEXT) {
        sub.generateBorders(data, roots);
      }

      // if window-size changed
      final boolean winChange = windowSizeChanged();
      if(winChange && paintType == Refresh.VOID
          || paintType == Refresh.INIT || paintType == Refresh.CONTEXT
          || paintType == Refresh.RESIZE) {
        treedist = tr.generateRects(sub, start, width, slimToText);
        nes = treedist == -1;
        if(!nes) {
          markedImage = null;
          setLevelDistance();
          createMainImage();
          if(!gui.context.marked.isEmpty()) markNodes();
        }
      }

      if(nes) {
        drawMessage(g, NOT_ENOUGH_SPACE);
        return;
      }

      g.drawImage(treeImage, 0, 0, width, height, this);

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

      if(markedImage != null) g.drawImage(markedImage, 0, 0, width, height, this);

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
    } finally {
      gui.painting = false;
    }
  }

  /**
   * Creates new image and draws rectangles in it.
   */
  private void createMainImage() {
    treeImage = createImage();
    final Graphics tg = treeImage.getGraphics();
    final int rl = roots.length;
    tg.setFont(font);
    BaseXLayout.antiAlias(tg);

    for(int rn = 0; rn < rl; ++rn) {
      final int h = sub.subtreeHeight(rn);
      for(int lv = 0; lv < h; ++lv) {
        final boolean big = tr.bigRect(sub, rn, lv);
        final TreeRect[] lr = tr.treeRectsPerLevel(rn, lv);
        final int ll = lr.length;
        for(int i = 0; i < ll; ++i) {
          final TreeRect r = lr[i];
          final int pre = sub.prePerIndex(rn, lv, i);
          drawRectangle(tg, rn, lv, r, pre, Draw.RECTANGLE);
        }

        if(big) {
          final TreeRect r = lr[0];
          final int ww = r.x + r.w - 1;
          final int x = r.x + 1;
          drawBigRectSquares(tg, lv, x, ww, 4);
        }
      }

      final TreeRect rr = tr.treeRectPerIndex(rn, 0, 0);
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
    final int mw = width >> 1;
    final int mh = height >> 1;
    String message = "";
    switch(t) {
      case NOT_ENOUGH_SPACE: message = NO_PIXELS;
      break;
      case NO_ATTS: message = "Enable attributes in Tree Options.";
      break;
    }
    final int x = mw - (BaseXLayout.width(g, message) >> 1);
    final int y = mh + fontHeight;
    g.setColor(TEXT);
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
  private void drawBigRectSquares(final Graphics g, final int lv, final int x, final int w,
      final int ss) {

    int xx = x;
    final int y = getYperLevel(lv);
    int nh = nodeHeight;
    g.setColor(color(7));
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
      if(y >= h || y < 0 || x >= w || x < 0) return false;
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
  private void drawRectangle(final Graphics g, final int rn, final int lv, final TreeRect r,
      final int pre, final Draw t) {

    final int y = getYperLevel(lv), h = nodeHeight, xx = r.x, ww = r.w;
    final boolean marked = marked(xx, y), big = tr.bigRect(sub, rn, lv);

    boolean border = false, label = !big && fontHeight <= h + 2;
    Color borderColor = null, textColor = TEXT, fillColor;
    final boolean fill;
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
        final int rgb = lgray.getRGB();
        fillColor = new Color(rgb + alpha, true);
        if(h > 4) border = true;
        fill = !big && !marked;
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
        textColor = BACK;
        fill = !marked;
        border = true;
        if(h < 4) {
          fillColor = color(7);
          borderColor = fillColor;
          label = false;
        }
        break;
      case PARENT:
      default:
        fillColor = color(6);
        textColor = BACK;
        fill = !big && !marked;
        border = !big;
        if(h < 4) {
          fillColor = color(7);
          borderColor = color(8);
          label = false;
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
    if(label && fill) {
      g.setColor(textColor);
      drawRectangleText(g, lv, r, pre);
    }
  }

  /**
   * Draws text into rectangle.
   * @param g graphics reference
   * @param lv level
   * @param r rectangle
   * @param pre pre
   */
  private void drawRectangleText(final Graphics g, final int lv, final TreeRect r, final int pre) {
    String s = Token.string(tr.text(pre)).trim();
    if(r.w < BaseXLayout.width(g, s) && r.w < BaseXLayout.width(
        g, ".." + s.substring(s.length() - 1)) + MIN_TXT_SPACE) return;

    final int x = r.x;
    final int y = getYperLevel(lv);
    final int rm = x + r.w / 2;

    int tw = BaseXLayout.width(g, s);

    if(tw > r.w) {
      s += "..";
      while((tw = BaseXLayout.width(g, s)) + MIN_TXT_SPACE > r.w
          && s.length() > 3) {
        s = s.substring(0, (s.length() - 2) / 2) + "..";
      }
    }
    final double yy = y + (nodeHeight + fontHeight * 0.5) / 2;
    g.drawString(s, (int) (rm - tw / 2.0d + BORDER_PADDING), (int) yy);
  }

  /**
   * Returns draw color.
   * @param l the current level
   * @param fill if true it returns fill color, rectangle color else
   * @return draw color
   */
  private static Color getColorPerLevel(final int l, final boolean fill) {
    final int till = Math.min(l, CHANGE_COLOR_TILL);
    return color(fill ? till : till + 2);
  }

  /**
   * Marks nodes inside the dragged selection.
   */
  private void markSelectedNodes() {
    final int x = selectRect.w < 0 ? selectRect.x + selectRect.w : selectRect.x;
    final int y = selectRect.h < 0 ? selectRect.y + selectRect.h : selectRect.y;
    final int w = Math.abs(selectRect.w), h = Math.abs(selectRect.h);

    final int t = y + h;
    final int size = sub.maxSubtreeHeight();
    final IntList list = new IntList();
    final int rl = roots.length;

    final int rs = treePerX(x), re = treePerX(x + w);

    for(int r = Math.max(rs, 0); r <= re; ++r) {
      for(int i = 0; i < size; ++i) {
        final int yL = getYperLevel(i);

        if(i < sub.subtreeHeight(r) && (yL >= y || yL + nodeHeight >= y)
            && (yL <= t || yL + nodeHeight <= t)) {

          final TreeRect[] rlv = tr.treeRectsPerLevel(r, i);
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
              int sPrePos = (int) (s * (x - start) / (double) mw);
              int ePrePos = (int) (s * (x - start + w) / (double) mw);

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
    gui.notify.mark(new DBNodes(gui.context.data(), list.finish()), this);
  }

  /**
   * Creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, width), Math.max(1, height), Transparency.TRANSLUCENT);
  }

  /**
   * Highlights the marked nodes.
   */
  private void markNodes() {
    markedImage = createImage();
    final Graphics mg = markedImage.getGraphics();
    BaseXLayout.antiAlias(mg);
    mg.setFont(font);

    final int[] marked = gui.context.marked.pres();
    if(marked.length == 0) return;

    int rn = 0;
    final int rl = roots.length;
    while(rn < rl) {
      final int ml = marked.length;
      final LinkedList<Integer> marklink = new LinkedList<>();
      for(int m = 0; m < ml; ++m) marklink.add(m, marked[m]);

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
              final int x = (int) (rect.w * ix / (double) sub.levelSize(rn, lv));
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
  private int getBigRectPosition(final int rn, final int lv, final int pre, final TreeRect r) {
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
  private int drawNodeInBigRectangle(final Graphics g, final int rn, final int lv, final TreeRect r,
      final int pre) {

    final int y = getYperLevel(lv);
    final int np = getBigRectPosition(rn, lv, pre, r);
    g.setColor(color(7));
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
  private void drawParentConnection(final Graphics g, final int lv, final TreeRect r, final int px,
      final int brx) {

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
  private void highlightNode(final Graphics g, final int rn, final int lv, final TreeRect r,
      final int pre, final int px, final Draw t) {

    if(lv == -1) return;

    final boolean big = tr.bigRect(sub, rn, lv), root = roots[rn] == pre;
    final int h = sub.subtreeHeight(rn);
    final Data d = gui.context.data();
    final int k = d.kind(pre), size = d.size(pre, k);

    // rectangle center
    final int rc;
    if(big) {
      rc = drawNodeInBigRectangle(g, rn, lv, r, pre);
    } else {
      drawRectangle(g, rn, lv, r, pre, t);
      rc = getRectCenter(r);
    }

    // draw parent node connection
    if(px > -1 && levelDistance >= MIN_NODE_DIST_CONN) drawParentConnection(g, lv, r, px, rc);

    // if there are ancestors draw them
    if(!root) {
      final int par = d.parent(pre, k);
      final int lvp = lv - 1;
      final TreeRect parRect = tr.searchRect(sub, rn, lvp, par);
      if(parRect == null) return;
      highlightNode(g, rn, lvp, parRect, par, rc, Draw.PARENT);
    }

    // if there are descendants draw them
    if((t == Draw.CONNECTION || t == Draw.HIGHLIGHT) && size > 1 && lv + 1 < h)
      highlightDescendants(g, rn, lv, r, pre, rc, t);

    // draws node text
    if(t == Draw.HIGHLIGHT) drawThumbnails(g, lv, pre, r, root);
  }

  /**
   * Draws thumbnails.
   * @param g the graphics reference
   * @param lv level
   * @param pre pre
   * @param r rectangle
   * @param root root flag
   */
  private void drawThumbnails(final Graphics g, final int lv, final int pre, final TreeRect r,
      final boolean root) {

    final int x = r.x;
    final int y = getYperLevel(lv);
    final int h = nodeHeight;
    final String s = Token.string(tr.text(pre));
    final int w = BaseXLayout.width(g, s);

    g.setColor(color(8));

    final int fh = fontHeight;
    if(root) {
      g.fillRect(x, y + h, w + 2, fh + 2);
      g.setColor(color(6));
      g.drawRect(x - 1, y + h + 1, w + 3, fh + 1);
      g.setColor(BACK);
      g.drawString(s, r.x + 1, (int) (y + h + (double) fh) - 2);
    } else {
      g.fillRect(r.x, y - fh, w + 2, fh);
      g.setColor(color(6));
      g.drawRect(r.x - 1, y - fh - 1, w + 3, fh + 1);
      g.setColor(BACK);
      g.drawString(s, r.x + 1, (int) (y - h / (double) fh) - 2);
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
  private void highlightDescendants(final Graphics g, final int rn, final int lv, final TreeRect r,
      final int pre, final int px, final Draw t) {

    final Data d = gui.context.data();
    final boolean big = tr.bigRect(sub, rn, lv);
    if(!big && t != Draw.CONNECTION) drawRectangle(g, rn, lv, r, pre, t);

    final int lvd = lv + 1;
    final TreeBorder[] sbo = sub.subtree(d, pre);
    if(sub.subtreeHeight(rn) >= lvd && sbo.length >= 2) {
      if(tr.bigRect(sub, rn, lvd)) {
        drawBigRectDescendants(g, rn, lvd, sbo, px, t);
      } else {
        final TreeBorder bo = sbo[1], bos = sub.treeBorder(rn, lvd);
        final int bs = bo.start >= bos.start ? bo.start - bos.start : bo.start;
        for(int j = 0; j < bo.size; ++j) {
          final int dp = sub.prePerIndex(rn, lvd, j + bs);

          final TreeRect dr = tr.treeRectPerIndex(rn, lvd, j + bs);

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
  private void drawBigRectDescendants(final Graphics g, final int rn, final int lv,
      final TreeBorder[] subt, final int parc, final Draw t) {

    int lvv = lv;
    int cen = parc;

    final int sl = subt.length;
    for(int i = 1; i < sl && tr.bigRect(sub, rn, lvv); ++i) {
      final TreeBorder bos = sub.treeBorder(rn, lvv), bo = subt[i];

      final TreeRect r = tr.treeRectPerIndex(rn, lvv, 0);
      final int bs = bo.start - bos.start;
      final double sti = bs / (double) bos.size, eni = (bs + bo.size) / (double) bos.size;
      final int df = r.x + (int) (r.w * sti), dt = r.x + (int) (r.w * eni);
      final int ww = Math.max(dt - df, 2);

      if(levelDistance >= MIN_NODE_DIST_CONN)
        drawDescendantsConn(g, lvv, new TreeRect(df, ww), cen, t);

      cen = (2 * df + ww) / 2;
      if(t != Draw.CONNECTION) {
        g.setColor(color(7));
        if(nodeHeight > 2) {
          g.drawRect(df, getYperLevel(lvv) + 1, ww, nodeHeight - 2);
        } else {
          g.drawRect(df, getYperLevel(lvv), ww, nodeHeight);
        }
      }

      if(lvv + 1 < sub.subtreeHeight(rn) && !tr.bigRect(sub, rn, lvv + 1)) {
        final Data d = gui.context.data();
        for(int j = bs; j < bs + bo.size; ++j) {
          final int pre = sub.prePerIndex(rn, lvv, j);
          final int pos = getBigRectPosition(rn, lvv, pre, r);
          final int k = d.kind(pre);
          final int s = d.size(pre, k);
          if(s > 1) highlightDescendants(g, rn, lvv, r, pre, pos,
              t == Draw.HIGHLIGHT || t == Draw.DESCENDANTS ? Draw.DESCENDANTS : Draw.CONNECTION);
        }
      }
      ++lvv;
    }
  }

  /**
   * Returns connection color.
   * @param type draw type
   * @return color
   */
  private static Color getConnectionColor(final Draw type) {
    final int alpha, index;
    if(type == Draw.CONNECTION) {
      alpha = 0x20000000;
      index = 4;
    } else {
      alpha = 0x60000000;
      index = 8;
    }
    return new Color(color(index).getRGB() + alpha, true);
  }

  /**
   * Draws descendants connection.
   * @param g graphics reference
   * @param lv level
   * @param r TreeRect
   * @param parc parent center
   * @param t draw type
   */
  private void drawDescendantsConn(final Graphics g, final int lv, final TreeRect r, final int parc,
      final Draw t) {

    final int pary = getYperLevel(lv - 1) + nodeHeight;
    final int prey = getYperLevel(lv) - 1;
    final int boRight = r.x + r.w + BORDER_PADDING - 2;
    final int boLeft = r.x + BORDER_PADDING;
    final int boTop = prey + 1;

    final Color c = getConnectionColor(t);

    g.setColor(c);
    if(boRight - boLeft > 2) {
      g.fillPolygon(new int[] { parc, boRight, boLeft}, new int[] { pary, boTop, boTop}, 3);
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

      final int rl = roots.length;
      for(int r = 0; r < rl; ++r) {
        for(int i = 0; i < sub.subtreeHeight(r); ++i) {
          if(tr.bigRect(sub, r, i)) {
            final int index = sub.preIndex(r, i, fpre);

            if(index > -1) {
              frn = r;
              frect = tr.treeRectsPerLevel(r, i)[0];
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
      final int lv = levelPerY(mousePosY);
      if(lv < 0) return false;
      final int mx = mousePosX, rn = frn = treePerX(mx), h = sub.subtreeHeight(rn);
      if(h < 0 || lv >= h) return false;

      final TreeRect[] rL = tr.treeRectsPerLevel(rn, lv);
      final int rl = rL.length;
      for(int l = 0; l < rl; l++) {
        final TreeRect r = rL[l];

        if(r.contains(mx)) {
          frect = r;
          flv = lv;
          final int pre;

          // if multiple pre values, then approximate pre value
          pre = tr.bigRect(sub, rn, lv) ? tr.prePerXPos(sub, rn, lv, mx) :
            sub.prePerIndex(rn, lv, l);
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
  private int treePerX(final int x) {
    return (int) ((x - start) / treedist);
  }

  /**
   * Determines the level of a y-axis value.
   * @param y the y-axis value
   * @return the level if inside a node rectangle, -1 else
   */
  private int levelPerY(final int y) {
    final double f = (y - topMargin) / ((double) levelDistance + nodeHeight);
    final double b = nodeHeight / (double) (levelDistance + nodeHeight);
    return f <= (int) f + b ? (int) f : -1;
  }

  /**
   * Sets optimal distance between levels.
   */
  private void setLevelDistance() {
    final int h = height - BOTTOM_MARGIN;
    int lvs = 0;
    final int rl = roots.length;
    for(int r = 0; r < rl; ++r) {
      final int th = sub.subtreeHeight(r);
      if(th > lvs) lvs = th;
    }
    int nh = (int) (fontSize * 1.4);
    final int dist = nh <= BEST_NODE_HEIGHT ? MIN_LEVEL_DISTANCE : BEST_LEVEL_DISTANCE;
    while(true) {
      final double ld = (h - lvs * nh) / (lvs - 1.0d);
      if(ld >= dist || nh < MIN_NODE_HEIGHT) {
        levelDistance = Math.max(MIN_LEVEL_DISTANCE, Math.min(MAX_LEVEL_DISTANCE, (int) ld));
        break;
      }
      nh--;
    }
    nodeHeight = nh;
    topMargin = Math.max(TOP_MARGIN, (h - (levelDistance * (lvs - 1) + lvs * nh)) / 2);
  }

  /**
   * Returns true if show attributes has changed.
   * @return show attributes has changed
   */
  private boolean showAttsChanged() {
    final GUIOptions gopts = gui.gopts;
    if(gopts.get(GUIOptions.TREEATTS) == showAtts) return false;
    showAtts = !showAtts;
    return true;
  }

  /**
   * Returns true if slim to text has changed.
   * @return slim to text has changed
   */
  private boolean slimToTextChanged() {
    final GUIOptions gopts = gui.gopts;
    if(gopts.get(GUIOptions.TREESLIMS) == slimToText) return false;
    slimToText = !slimToText;
    return true;
  }

  /**
   * Returns true if window-size has changed.
   * @return window-size has changed
   */
  private boolean windowSizeChanged() {
    final int w = getWidth(), h = getHeight();
    if(width > -1 && height > -1 && h == height && w == width + 2 * LEFT_AND_RIGHT_MARGIN)
      return false;
    height = h;
    start = LEFT_AND_RIGHT_MARGIN;
    width = w - 2 * LEFT_AND_RIGHT_MARGIN;
    return true;
  }

  /**
   * Returns number of hit nodes.
   * @param rn root
   * @param lv level
   * @param r rectangle
   * @return size
   */
  private int getHitBigRectNodesNum(final int rn, final int lv, final TreeRect r) {
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
  private int getMostSizedNode(final Data d, final int rn, final int lv, final TreeRect r,
      final int p) {
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
    if(!inFocus || frect == null) return;

    if(SwingUtilities.isLeftMouseButton(e)) {
      if(flv >= sub.subtreeHeight(frn)) return;

      if(tr.bigRect(sub, frn, flv)) {
        final DBNodes ns = new DBNodes(gui.context.data());
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
    if(e.getWheelRotation() <= 0) gui.notify.context(new DBNodes(
        gui.context.data(), gui.context.focused), false, null);
    else gui.notify.hist(false);
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(gui.updating || e.isShiftDown()) return;

    if(selection) {
      final int x = e.getX();
      final int y = e.getY();
      selectRect.w = x - selectRect.x;
      selectRect.h = y - selectRect.y;
    } else {
      selection = true;
      selectRect = new ViewRect();
      selectRect.x = e.getX();
      selectRect.y = e.getY();
      selectRect.h = 1;
      selectRect.w = 1;
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
