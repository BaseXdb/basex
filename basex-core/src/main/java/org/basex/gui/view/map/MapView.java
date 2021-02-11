package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * This view is a TreeMap implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Joerg Hauser
 * @author Bastian Lemke
 */
public final class MapView extends View {
  /** Dynamic zooming steps. */
  private static final int[] ZS = { 0, 0, 0, 0, 20, 80, 180, 320, 540, 840,
      1240, 1740, 2380, 3120, 4000, 4980, 5980, 6860, 7600, 8240, 8740, 9140,
      9440, 9660, 9800, 9900, 9960, 9980, 9980, 9980, 10000 };
  /** Number of zooming steps. */
  private static final int ZOOMSIZE = ZS.length - 1;
  /** Maximum zooming step. */
  private static final int MAXZS = ZS[ZOOMSIZE];

  /** Array of current rectangles. */
  private MapRects mainRects;
  /** Data specific map layout. */
  private transient MapPainter painter;
  /** Text lengths. */
  private int[] textLen;

  /** Rectangle history. */
  private final MapRect[] rectHist = new MapRect[ViewNotifier.MAXHIST];
  /** Current zooming Step (set to 0 when no zooming takes place). */
  private int zoomStep;
  /** Main rectangle. */
  private MapRect mainRect;
  /** Dragged rectangle. */
  private MapRect selBox;
  /** Flag for zooming in/out. */
  private boolean zoomIn;
  /** Zooming speed. */
  private int zoomSpeed;

  /** Horizontal mouse position. */
  private int mouseX = -1;
  /** Vertical mouse position. */
  private int mouseY = -1;
  /** Drag tolerance. */
  private int dragTol;

  /** Currently focused rectangle. */
  private MapRect focused;

  /** TreeMap. */
  private BufferedImage mainMap;
  /** Zoomed TreeMap. */
  private BufferedImage zoomMap;

  /** Keeps the whole map layout. */
  MapLayout layout;

  /**
   * Default constructor.
   * @param notifier view notifier
   */
  public MapView(final ViewNotifier notifier) {
    super(MAPVIEW, notifier);
    new BaseXPopup(this, POPUP);
  }

  /**
   * Creates a buffered image.
   * @return buffered image
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
        BufferedImage.TYPE_INT_BGR);
  }

  @Override
  public void refreshInit() {
    painter = null;
    mainRects = null;
    focused = null;
    textLen = null;
    zoomStep = 0;

    final Data data = gui.context.data();
    final GUIOptions gopts = gui.gopts;
    if(data != null && visible()) {
      painter = new MapPainter(this, gopts);
      mainMap = createImage();
      zoomMap = createImage();
      refreshLayout();
    }
  }

  @Override
  public void refreshFocus() {
    final int f = gui.context.focused;
    if(f == -1) focused = null;

    if(mainRects == null) return;
    final int ms = mainRects.size;
    for(int mi = 0; mi < ms; ++mi) {
      final MapRect rect = mainRects.get(mi);
      if(f == rect.pre || mi + 1 == ms || f < mainRects.get(mi + 1).pre) {
        focused = rect;
        repaint();
        break;
      }
    }
  }

  @Override
  public void refreshMark() {
    drawMap(mainMap, mainRects);
    repaint();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    // use simple zooming animation for result node filtering
    final DBNodes context = gui.context.current();
    final int hist = gui.notify.hist;
    final boolean page = !more && rectHist[hist + 1] != null &&
      rectHist[hist + 1].pre == 0 || more && (context.size() != 1 ||
      focused == null || context.pre(0) != focused.pre);
    if(page) focused = new MapRect(0, 0, getWidth(), 1);

    zoom(more, quick);
  }

  @Override
  public void refreshLayout() {
    if(painter == null) return;

    // calculate map
    final DBNodes curr = gui.context.current();
    calc(new MapRect(0, 0, getWidth(), getHeight(), 0, 0), curr, mainMap);
    repaint();
  }

  @Override
  public void refreshUpdate() {
    textLen = null;
    refreshContext(false, true);
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWMAP);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWMAP, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  /**
   * Zooms the focused rectangle.
   * @param more show more
   * @param quick context switch (no animation)
   */
  private void zoom(final boolean more, final boolean quick) {
    gui.updating = !quick;
    zoomIn = more;

    // choose zooming rectangle
    final int hist = gui.notify.hist;
    if(more) {
      rectHist[hist] = focused;
      mainRect = rectHist[hist];
    } else {
      mainRect = rectHist[hist + 1];
    }
    if(mainRect == null) mainRect = new MapRect(0, 0, getWidth(), getHeight());

    // reset data & start zooming
    final BufferedImage tmpMap = zoomMap;
    zoomMap = mainMap;
    mainMap = tmpMap;
    focused = null;

    // create new context nodes
    refreshLayout();

    // calculate zooming speed (slower for large zooming scales)
    if(mainRect.w > 0 && mainRect.h > 0) {
      zoomSpeed = (int) (StrictMath.log(64.0d * getWidth() / mainRect.w) +
        StrictMath.log(64.0d * getHeight() / mainRect.h));
    }

    if(quick) {
      gui.updating = false;
      focus();
      repaint();
    } else {
      zoomStep = ZOOMSIZE;
      new Thread(() -> {
        focused = null;

        // run zooming
        while(zoomStep > 1) {
          Performance.sleep(zoomSpeed);
          --zoomStep;
          repaint();
        }
        // wait until current painting is finished
        while(gui.painting) Performance.sleep(zoomSpeed);

        // remove old rectangle and repaint map
        zoomStep = 0;
        gui.updating = false;
        focus();
        repaint();
      }).start();
    }
  }

  /**
   * Finds the rectangle at the cursor position.
   * @return focused rectangle
   */
  private boolean focus() {
    if(gui.updating || mainRects == null) return false;

    /*
     * Loop through all rectangles. As the rectangles are sorted by pre order
     * and small rectangles are descendants of bigger ones, the focused
     * rectangle can be found by simply parsing the array backwards.
     */
    int r = mainRects.size;
    while(--r >= 0) {
      final MapRect rect = mainRects.get(r);
      if(rect.contains(mouseX, mouseY)) break;
    }
    // don't focus top rectangles
    final MapRect fr = r >= 0 ? mainRects.get(r) : null;

    // find focused rectangle
    final boolean nf = focused != fr || fr != null && fr.thumb;
    focused = fr;

    if(nf) gui.notify.focus(focused != null ? focused.pre : -1, this);
    return nf;
  }

  /**
   * Initializes the calculation of the main map.
   * @param rect initial space to layout rectangles in
   * @param nodes nodes to draw in the map
   * @param map image to draw rectangles on
   */
  private void calc(final MapRect rect, final DBNodes nodes, final BufferedImage map) {
    // calculate new main rectangles
    gui.cursor(CURSORWAIT);

    initLen();
    layout = new MapLayout(nodes.data(), textLen, gui.gopts);
    layout.makeMap(rect, new MapList(nodes.pres().clone()), 0, (int) nodes.size() - 1);
    // rectangles are copied to avoid synchronization issues
    mainRects = layout.rectangles.copy();

    drawMap(map, mainRects);
    focus();
    gui.cursor(CURSORARROW, true);
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Data data = gui.context.data();
    if(data == null) return;

    if(mainRects == null || mainRects.size == 0 || mainRects.get(0).w == 0) {
      super.paintComponent(g);
      if(mainRects == null || mainRects.size != 0) refreshInit();
      return;
    }

    // calculate map
    gui.painting = true;

    // paint map
    final MapRenderer renderer = new MapRenderer(g);
    final boolean in = zoomStep > 0 && zoomIn;
    final Image img1 = in ? zoomMap : mainMap;
    final Image img2 = in ? mainMap : zoomMap;
    if(zoomStep > 0) {
      drawImage(g, img1, -zoomStep);
      drawImage(g, img2, zoomStep);
    } else {
      drawImage(g, mainMap, zoomStep);
    }

    // check if focused rectangle is valid
    if(focused != null && focused.pre >= data.meta.size) focused = null;

    // skip node path view
    final MapRect f = focused;
    if(f == null || mainRects.size == 1 && f == mainRects.get(0)) {
      gui.painting = false;
      if(f == null || !f.thumb) return;
    }

    final GUIOptions gopts = gui.gopts;
    if(gopts.get(GUIOptions.MAPOFFSETS) == 0) {
      g.setColor(color(32));
      int pre = mainRects.size;
      int par = ViewData.parent(data, f.pre);
      while(--pre >= 0) {
        final MapRect rect = mainRects.get(pre);
        if(rect.pre == par) {
          final int x = rect.x;
          final int y = rect.y;
          final int w = rect.w;
          final int h = rect.h;
          g.drawRect(x, y, w, h);
          g.drawRect(x - 1, y - 1, w + 2, h + 2);
          par = ViewData.parent(data, par);
        }
      }
    }

    if(selBox != null) {
      g.setColor(colormark3);
      g.drawRect(selBox.x, selBox.y, selBox.w, selBox.h);
      g.drawRect(selBox.x - 1, selBox.y - 1, selBox.w + 2, selBox.h + 2);
    } else {
      // paint focused rectangle
      final int x = f.x, y = f.y, w = f.w, h = f.h;
      g.setColor(color4);
      g.drawRect(x, y, w, h);
      g.drawRect(x + 1, y + 1, w - 2, h - 2);

      // draw element label
      g.setFont(font);
      BaseXLayout.antiAlias(g);
      if(data.kind(f.pre) == Data.ELEM) BaseXLayout.drawTooltip(g,
          Token.string(ViewData.namedText(gopts, data, f.pre)), x, y, getWidth(), f.level + 5);

      if(f.thumb) {
        // draw tooltip for thumbnail
        f.x += 3;
        f.w -= 3;
        // read content from disk
        final byte[] text = MapPainter.text(data, f);
        // calculate tooltip
        final int[][] info = new FTLexer().init(text).info();
        final TokenList tl = renderer.computeToolTip(f, info, mouseX, mouseY, getWidth());
        final MapRect mr = new MapRect(getX(), getY(), getWidth(), getHeight());
        // draw calculated tooltip
        renderer.drawToolTip(mr, mouseX, mouseY, tl);
        f.x -= 3;
        f.w += 3;
      }
    }
    gui.painting = false;
  }

  /**
   * Draws image with correct scaling.
   * @param g graphics reference
   * @param img image to be drawn
   * @param zi zooming factor
   */
  private void drawImage(final Graphics g, final Image img, final int zi) {
    if(img == null) return;
    final MapRect r = new MapRect(0, 0, getWidth(), getHeight());
    zoom(r, zi);
    g.drawImage(img, r.x, r.y, r.x + r.w, r.y + r.h, 0, 0, getWidth(), getHeight(), this);
  }

  /**
   * Zooms the coordinates of the specified rectangle.
   * @param r rectangle to be zoomed
   * @param zs zooming step
   */
  private void zoom(final MapRect r, final int zs) {
    // calculate zooming rectangle
    // get window size
    long xs = r.x, ys = r.y, xe = xs + r.w, ye = ys + r.h;
    if(zs != 0) {
      final MapRect zr = mainRect;
      final int tw = getWidth();
      final int th = getHeight();
      if(zs > 0) {
        final long s = ZS[zoomIn ? zs : ZOOMSIZE - zs];
        xs = (zr.x + xs * zr.w / tw - xs) * s / MAXZS;
        ys = (zr.y + ys * zr.h / th - ys) * s / MAXZS;
        xe += (zr.x + xe * zr.w / tw - xe) * s / MAXZS;
        ye += (zr.y + ye * zr.h / th - ye) * s / MAXZS;
      } else {
        final long s = 10000 - (ZS[zoomIn ? -zs : ZOOMSIZE + zs]);
        if(zr.w == 0) zr.w = 1;
        if(zr.h == 0) zr.h = 1;
        xs = -xe * zr.x / zr.w * s / MAXZS;
        xe = xs + xe + xe * (xe - zr.w) / zr.w * s / MAXZS;
        ys = -ye * zr.y / zr.h * s / MAXZS;
        ye = ys + ye + ye * (ye - zr.h) / zr.h * s / MAXZS;
      }
    }
    r.x = (int) xs;
    r.y = (int) ys;
    r.w = (int) (xe - xs);
    r.h = (int) (ye - ys);
  }

  /**
   * Creates a buffered image for the treemap.
   * @param map image to draw the map on
   * @param rects rectangles to be drawn
   */
  private void drawMap(final BufferedImage map, final MapRects rects) {
    final Graphics g = map.getGraphics();
    BaseXLayout.antiAlias(g);
    painter.drawRectangles(g, rects);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(gui.updating) return;
    super.mousePressed(e);

    mouseX = e.getX();
    mouseY = e.getY();
    dragTol = 0;
    if(!focus() && gui.context.focused == -1) return;

    // add or remove marked node
    final DBNodes marked = gui.context.marked;
    if(e.getClickCount() == 2) {
      if(mainRects.size != 1) gui.notify.context(marked, false, null);
    } else if(e.isShiftDown()) {
      gui.notify.mark(1, null);
    } else if(sc(e) && SwingUtilities.isLeftMouseButton(e)) {
      gui.notify.mark(2, null);
    } else {
      if(!marked.contains(gui.context.focused)) gui.notify.mark(0, null);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(gui.updating || ++dragTol < 8 || mainRects.sorted != mainRects.list) return;

    // refresh mouse focus
    int mx = mouseX;
    int my = mouseY;
    int mw = e.getX() - mx;
    int mh = e.getY() - my;
    if(mw < 0) mx -= mw = -mw;
    if(mh < 0) my -= mh = -mh;
    selBox = new MapRect(mx, my, mw, mh);

    final Data data = gui.context.data();
    final IntList il = new IntList();
    int np = 0;
    final int rl = mainRects.size;
    for(int r = 0; r < rl; ++r) {
      final MapRect rect = mainRects.get(r);
      if(mainRects.get(r).pre < np) continue;
      if(selBox.contains(rect)) {
        il.add(rect.pre);
        np = rect.pre + ViewData.size(data, rect.pre);
      }
    }
    gui.notify.mark(new DBNodes(data, il.finish()), null);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(gui.updating) return;
    if(selBox != null) {
      selBox = null;
      repaint();
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(gui.updating || gui.context.focused == -1) return;
    if(e.getWheelRotation() <= 0) {
      final DBNodes m = new DBNodes(gui.context.data(), gui.context.focused);
      gui.context.marked = m;
      gui.notify.context(m, false, null);
    } else {
      gui.notify.hist(false);
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(gui.updating || mainRects == null || control(e)) return;

    final boolean cursor = PREVLINE.is(e) || NEXTLINE.is(e) ||
        PREVCHAR.is(e) || NEXTCHAR.is(e);
    if(!cursor) return;

    if(focused == null) focused = mainRects.get(0);

    final int fs = fontSize;
    int o = fs + 4;
    final boolean shift = e.isShiftDown();
    if(PREVLINE.is(e)) {
      mouseY = focused.y + (shift ? focused.h - fs : 0) - 1;
      if(shift) mouseX = focused.x + (focused.w >> 1);
    } else if(NEXTLINE.is(e)) {
      mouseY = focused.y + (shift ? o : focused.h + 1);
      if(shift) mouseX = focused.x + (focused.w >> 1);
    } else if(PREVCHAR.is(e)) {
      mouseX = focused.x + (shift ? focused.w - fs : 0) - 1;
      if(shift) mouseY = focused.y + (focused.h >> 1);
    } else if(NEXTCHAR.is(e)) {
      mouseX = focused.x + (shift ? o : focused.w + 1);
      if(shift) mouseY = focused.y + (focused.h >> 1);
    }

    o = mainRects.get(0).w == getWidth() ? (o >> 1) + 1 : 0;
    mouseX = Math.max(o, Math.min(getWidth() - o - 1, mouseX));
    mouseY = Math.max(o << 1, Math.min(getHeight() - o - 1, mouseY));

    if(focus()) repaint();
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    if(gui.updating) return;
    focused = null;
    mainMap = createImage();
    zoomMap = createImage();
    refreshLayout();
  }

  /**
   * Initializes the text lengths and stores them into an array.
   */
  private void initLen() {
    final Data data = gui.context.data();
    if(textLen != null || gui.gopts.get(GUIOptions.MAPWEIGHT) == 0) return;

    final int size = data.meta.size;
    textLen = new int[size];

    final IntList pars = new IntList();
    int l = 0;

    for(int pre = 0; pre < size; ++pre) {
      final int kind = data.kind(pre);
      final int par = data.parent(pre, kind);

      final int ll = l;
      while(l > 0 && pars.get(l - 1) > par) {
        textLen[pars.get(l - 1)] += textLen[pars.get(l)];
        --l;
      }
      if(l > 0 && ll != l) textLen[pars.get(l - 1)] += textLen[pars.get(l)];
      pars.set(l, pre);

      if(kind == Data.DOC || kind == Data.ELEM) {
        pars.set(++l, 0);
      } else {
        textLen[pre] = data.textLen(pre, kind != Data.ATTR);
      }
    }
    while(--l >= 0) textLen[pars.get(l)] += textLen[pars.get(l + 1)];
  }
}
