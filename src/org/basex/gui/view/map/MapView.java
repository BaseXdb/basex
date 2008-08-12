package org.basex.gui.view.map;

import static org.basex.Text.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.SwingUtilities;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewData;
import org.basex.util.Action;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This view is a TreeMap implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MapView extends View implements Runnable {
  /** Dynamic zooming steps. */
  private static final int[] ZS = {
    0, 31, 113, 205, 356, 553, 844, 1226, 1745, 2148, 2580, 3037,
    3515, 4008, 4511, 5019, 5527, 6030, 6522, 6998, 7453, 7883,
    8283, 8749, 9158, 9456, 9650, 9797, 9885, 9973, 10000
  };
  /** Number of zooming steps. */
  private static final int ZOOMSIZE = ZS.length - 1;
  /** Maximum zooming step. */
  private static final int MAXZS = ZS[ZOOMSIZE];

  /** Layout rectangle. */
  MapRect layout;
  /** Array of current rectangles. */
  private MapRects mainRects;
  /** Data specific map layout. */
  private MapPainter painter;

  /** Rectangle history. */
  private final MapRect[] rectHist = new MapRect[MAXHIST];
  /** Current zooming Step (set to 0 when no zooming takes place). */
  private int zoomStep;
  /** Main rectangle. */
  private MapRect mainRect;
  /** Dragged rectangle. */
  private MapRect dragRect;
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
  private transient MapRect focusedRect;

  /** TreeMap. */
  private BufferedImage mainMap;
  /** Zoomed TreeMap. */
  private BufferedImage zoomMap;

  /**
   * Default Constructor.
   * @param help help text
   */
  public MapView(final byte[] help) {
    super(help);
    setMode(GUIConstants.FILL.NONE);
    mainMap = createImage();
    zoomMap = createImage();
    popup = new BaseXPopup(this, GUIConstants.POPUP);
  }

  /**
   * Creates a buffered image.
   * @return buffered image
   */
  private BufferedImage createImage() {
    final Dimension screen = getToolkit().getScreenSize();
    final int w = Math.max(1, screen.width);
    final int h = Math.max(1, screen.height);
    // Screenshot version: int w = {width}, h = {height}...

    final BufferedImage img = new BufferedImage(w, h,
        BufferedImage.TYPE_INT_BGR);
    final Graphics g = img.getGraphics();
    g.setColor(GUIConstants.COLORS[0]);
    g.fillRect(0, 0, w, h);
    return img;
  }

  @Override
  public void refreshInit() {
    focusedRect = null;
    mainRects = null;
    zoomStep = 0;
    slide = false;
    if(painter != null) painter.close();

    final Data data = GUI.context.data();
    if(data != null && getWidth() != 0) {
      if(!GUIProp.showmap) return;
      painter = data.deepfs ? new MapFS(this) : new MapDefault(this);
      calc();
      repaint();
    }
  }

  @Override
  public void refreshFocus() {
    if(mainRects == null || working) return;
    if(focused == -1 && focusedRect != null) focusedRect = null;

    final MapRect m = focusedRect;
    int mi = 0;
    for(final int ms = mainRects.size; mi < ms; mi++) {
      final MapRect rect = mainRects.get(mi);
      if(focused == rect.p || mi + 1 < ms &&
          focused < mainRects.get(mi + 1).p) {
        focusedRect = rect;
        break;
      }
    }
    if(focusedRect != m) repaint();
  }

  @Override
  public void refreshMark() {
    if(getWidth() == 0) return;
    drawMap();
    repaint();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    if(!GUIProp.showmap) {
      mainRects = null;
      return;
    }

    // use simple zooming animation for result node filtering
    final Nodes context = GUI.context.current();
    final boolean page = !more && rectHist[hist + 1] != null &&
      rectHist[hist + 1].p == 0 || more && (context.size != 1 ||
          focusedRect == null || context.pre[0] != focusedRect.p);
    if(page) focusedRect = new MapRect(0, 0, getWidth(), 1);

    zoom(more, quick);
  }

  @Override
  public void refreshLayout() {
    calc();
    repaint();
  }

  @Override
  public void refreshUpdate() {
    refreshContext(false, true);
  }

  /**
   * Zooms the focused rectangle.
   * @param more show more
   * @param quick context switch (no animation)
   */
  private void zoom(final boolean more, final boolean quick) {
    working = !quick;
    zoomIn = more;

    // choose zooming rectangle
    if(more) {
      rectHist[hist] = focusedRect.clone();
      mainRect = rectHist[hist];
    } else {
      mainRect = rectHist[hist + 1];
    }
    if(mainRect == null) mainRect = new MapRect(0, 0, getWidth(), getHeight());

    // reset data & start zooming
    final BufferedImage tmpMap = zoomMap;
    zoomMap = mainMap;
    mainMap = tmpMap;
    focusedRect = null;

    // create new context nodes
    calc();

    // calculate zooming speed (slower for large zooming scales)
    zoomSpeed = (int) (Math.log(128 * getWidth() / mainRect.w) +
        Math.log(128 * getHeight() / mainRect.h));

    if(quick) {
      working = false;
      focus();
      repaint();
    } else {
      zoomStep = ZOOMSIZE;
      new Thread(this).start();
    }
  }

  /**
   * Starts the zooming thread.
   */
  public void run() {
    focusedRect = null;

    // run zooming
    while(zoomStep > 1) {
      Performance.sleep(zoomSpeed);
      zoomStep--;
      repaint();
    }
    // wait until current painting is finished
    while(painting) Performance.sleep(zoomSpeed);

    // remove old rectangle and repaint map
    zoomStep = 0;
    working = false;
    focus();
    repaint();
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  private boolean focus() {
    if(working || mainRects == null) return false;

    /* Loop through all rectangles. As the rectangles are sorted by pre
     * order and small rectangles are descendants of bigger ones, the
     * focused rectangle can be found by simply parsing the array backward. */
    int r = mainRects.size;
    while(--r >= 0) {
      final MapRect rect = mainRects.get(r);
      if(rect.contains(mouseX, mouseY)) break;
    }
    // don't focus top rectangles
    final MapRect fr = r >= 0 ? mainRects.get(r) : null;

    // find focused rectangle
    final boolean newFocus = focusedRect != fr || fr != null && fr.thumb;
    focusedRect = fr;

    if(fr != null) GUI.get().cursor(painter.highlight(focusedRect, mouseX,
        mouseY, false) ? GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);

    if(newFocus) notifyFocus(focusedRect != null ? focusedRect.p : -1, this);
    return newFocus;
  }

  /**
   * Initializes the calculation of the main TreeMap.
   * @return performance string
   */
  private String calc() {
    // calculate initial rectangle
    final int w = getWidth(), h = getHeight();
    // Screenshots: int w = mainMap.getWidth(), h = mainMap.getHeight();
    final MapRect rect = new MapRect(0, 0, w, h, 0, 0);

    // calculate new main rectangles
    if(painter == null) return null;
    mainRects = new MapRects();
    painter.reset();

    layout = null;
    final int o = GUIProp.fontsize + 4;
    switch(GUIProp.maplayout) {
      case 0: layout = new MapRect(0, 0, 0, 0); break;
      case 1: layout = new MapRect(1, 1, 2, 2); break;
      case 2: layout = new MapRect(0, o, 0, o); break;
      case 3: layout = new MapRect(2, o - 1, 4, o + 1); break;
      case 4: layout = new MapRect(o >> 2, o, o >> 1, o + (o >> 2)); break;
      case 5: layout = new MapRect(o >> 1, o, o, o + (o >> 1)); break;
      default:
    }

    // call recursive TreeMap algorithm
    final Performance perf = new Performance();
    final Nodes nodes = GUI.context.current();
    calcMap(rect, new IntList(nodes.pre), 0, nodes.size, true);

    // output timing information
    final StringBuilder sb = new StringBuilder();
    sb.append(STATUSMAP1);
    sb.append(perf.getTimer());

    painter.init(mainRects);
    drawMap();
    sb.append(STATUSMAP2);
    sb.append(perf.getTimer());
    focus();

    /* Screenshots:
    try {
      File file = new File("screenshot.png");
      ImageIO.write(mainMap, "png", file);
    } catch(IOException e) {
      e.printStackTrace();
    }*/
    
    return sb.toString();
  }

  /**
   * Recursively splits rectangles on one level.
   * @param r parent rectangle
   * @param l children array
   * @param ns start array position
   * @param ne end array position
   * @param first first traversal
   */
  private void calcMap(final MapRect r, final IntList l,
      final int ns, final int ne, final boolean first) {

    // one rectangle left.. continue with children
    if(ne - ns == 1) {
      // calculate rectangle sizes
      final MapRect t = new MapRect(r.x, r.y, r.w, r.h, l.get(ns), r.l);
      mainRects.add(t);

      final int x = t.x + layout.x;
      final int y = t.y + layout.y;
      final int w = t.w - layout.w;
      final int h = t.h - layout.h;

      // get children
      final int o = GUIProp.fontsize + 4;
      // skip too small rectangles and leaf nodes (= meta data in deepfs)
      if((w >= o || h >= o) && w > 0 && h > 0 &&
          !ViewData.isLeaf(GUI.context.data(), t.p)) {
        final IntList ch = children(t.p);
        if(ch.size != 0) calcMap(new MapRect(x, y, w, h,
            l.get(ns), r.l + 1), ch, 0, ch.size - 1, false);
      }
    } else {
      // recursively split current nodes
      int nn = ne - ns;
      int ln = nn >> 1;
      int ni = ns + ln;

      // consider number of descendants to calculate split point
      if(!GUIProp.mapsimple && !first) {
        nn = l.get(ne) - l.get(ns);
        ni = ns + 1;
        for(; ni < ne - 1; ni++) if(l.get(ni) - l.get(ns) >= (nn >> 1)) break;
        ln = l.get(ni) - l.get(ns);
      }

      // determine rectangle orientation (horizontal/vertical)
      final int p = GUIProp.mapprop;
      final boolean v = p > 4 ? r.w > r.h * (p + 4) / 8 :
        r.w * (13 - p) / 8 > r.h;

      int xx = r.x;
      int yy = r.y;
      int ww = !v ? r.w : (int) ((long) r.w * ln / nn);
      int hh =  v ? r.h : (int) ((long) r.h * ln / nn);

      // paint both rectangles if enough space is left
      if(ww > 0 && hh > 0) calcMap(new MapRect(xx, yy, ww, hh, 0, r.l),
          l, ns, ni, first);
      if(v) {
        xx += ww;
        ww = r.w - ww;
      } else {
        yy += hh;
        hh = r.h - hh;
      }
      if(ww > 0 && hh > 0) calcMap(new MapRect(xx, yy, ww, hh, 0, r.l),
          l, ni, ne, first);
    }
  }

  /**
   * Returns all children of the specified node.
   * @param par parent node
   * @return children
   */
  private IntList children(final int par) {
    final IntList list = new IntList();
    final Data data = GUI.context.data();

    final int kind = data.kind(par);
    final int last = par + data.size(par, kind);
    int p = par + (GUIProp.mapatts ? 1 : data.attSize(par, kind));
    while(p != last) {
      list.add(p);
      p += data.size(p, data.kind(p));
    }

    // paint all children
    if(list.size != 0) list.add(p);
    return list;
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Data data = GUI.context.data();
    if(data == null) return;

    if(mainRects == null) {
      refreshInit();
      return;
    }

    // calculate map
    painting = true;

    // paint map
    final boolean in = zoomStep > 0 && zoomIn;
    final Image img1 = in ? zoomMap : mainMap;
    final Image img2 = in ? mainMap : zoomMap;
    if(zoomStep > 0) {
      drawImage(g, img1, -zoomStep);
      drawImage(g, img2, zoomStep);
    } else {
      drawImage(g, mainMap, zoomStep);
    }

    // skip node path view
    if(focusedRect == null || mainRects.size == 1 &&
        focusedRect == mainRects.get(0)) {
      painting = false;
      return;
    }

    if(GUIProp.maplayout == 0) {
      g.setColor(GUIConstants.COLORS[32]);
      int pre = mainRects.size;
      int par = ViewData.parent(data, focusedRect.p);
      while(--pre >= 0) {
        final MapRect rect = mainRects.get(pre);
        if(rect.p == par) {
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

    if(dragRect != null) {
      g.setColor(GUIConstants.colormark3);
      g.drawRect(dragRect.x, dragRect.y, dragRect.w, dragRect.h);
      g.drawRect(dragRect.x - 1, dragRect.y - 1, dragRect.w + 2,
          dragRect.h + 2);
    } else {
      // paint focused rectangle
      final int x = focusedRect.x;
      final int y = focusedRect.y;
      final int w = focusedRect.w;
      final int h = focusedRect.h;
      g.setColor(GUIConstants.color6);
      g.drawRect(x, y, w, h);
      g.drawRect(x - 1, y - 1, w + 2, h + 2);

      // draw tag label
      if(data.kind(focusedRect.p) == Data.ELEM) {
        g.setFont(GUIConstants.font);
        final byte[] tag = ViewData.tag(data, focusedRect.p);
        final int tw = BaseXLayout.width(g, tag);
        final int th = g.getFontMetrics().getHeight();
        final int xx = Math.min(getWidth() - tw - 8, x);

        g.setColor(GUIConstants.COLORS[focusedRect.l + 5]);
        g.fillRect(xx - 1, y - th, tw + 4, th);
        g.setColor(GUIConstants.color1);
        g.drawString(Token.string(tag), xx, y - 4);
      }

      // add interactions for current thumbnail rectangle...
      //if(focusedRect.thumb) ...
    }
    

    painting = false;
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
    g.drawImage(img, r.x, r.y, r.x + r.w, r.y + r.h, 0, 0,
        getWidth(), getHeight(), this);
  }

  /**
   * Zooms the coordinates of the specified rectangle.
   * @param r rectangle to be zoomed
   * @param zs zooming step
   */
  private void zoom(final MapRect r, final int zs) {
    int xs = r.x;
    int ys = r.y;
    int xe = xs + r.w;
    int ye = ys + r.h;

    // calculate zooming rectangle
    // get window size
    if(zs != 0) {
      final MapRect zr = mainRect;
      final int tw = getWidth();
      final int th = getHeight();
      if(zs > 0) {
        final long s = zoomIn ? ZS[zs] : ZS[ZOOMSIZE - zs];
        xs = (int) ((zr.x + xs * zr.w / tw - xs) * s / MAXZS);
        ys = (int) ((zr.y + ys * zr.h / th - ys) * s / MAXZS);
        xe += (int) ((zr.x + xe * zr.w / tw - xe) * s / MAXZS);
        ye += (int) ((zr.y + ye * zr.h / th - ye) * s / MAXZS);
      } else {
        final long s = 10000 - (zoomIn ? ZS[-zs] : ZS[ZOOMSIZE + zs]);
        xs = (int) (-xe * zr.x / zr.w * s / MAXZS);
        xe = (int) (xs + xe + xe * (xe - zr.w) / zr.w * s / MAXZS);
        ys = (int) (-ye * zr.y / zr.h * s / MAXZS);
        ye = (int) (ys + ye + ye * (ye - zr.h) / zr.h * s / MAXZS);
      }
    }
    r.x = xs;
    r.y = ys;
    r.w = xe - xs;
    r.h = ye - ys;
  }

  /**
   * Creates a buffered image for the treemap.
   */
  void drawMap() {
    final Graphics g = mainMap.getGraphics();
    g.setColor(GUIConstants.COLORS[2]);
    BaseXLayout.antiAlias(g);
    if(mainRects != null) painter.drawRectangles(g, mainRects);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(working) return;
    super.mouseMoved(e);
    // refresh mouse focus
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if(working) return;
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    if(!left || focusedRect == null) return;

    // single/double click?
    if(painter.highlight(focusedRect, mouseX, mouseY, true)) return;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(working) return;
    super.mousePressed(e);
    mouseX = e.getX();
    mouseY = e.getY();
    dragTol = 0;
    if(!focus() && focused == -1) return;

    // left/right mouse click?
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final int pre = focused;

    // add or remove marked node
    final Nodes marked = GUI.context.marked();
    if(!left) {
      // right mouse button
      if(marked.find(pre) < 0) notifyMark(0);
    } else if(e.getClickCount() == 2) {
      if(mainRects.size != 1) notifyContext(marked, false);
    } else if(e.isShiftDown()) {
      notifyMark(1);
    } else if(e.isControlDown()) {
      notifyMark(2);
    } else {
      if(marked.find(pre) < 0) notifyMark(0);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(working || ++dragTol < 8) return;
    super.mouseDragged(e);

    // refresh mouse focus
    int mx = mouseX;
    int my = mouseY;
    int mw = e.getX() - mx;
    int mh = e.getY() - my;
    if(mw < 0) mx -= mw = -mw;
    if(mh < 0) my -= mh = -mh;
    dragRect = new MapRect(mx, my, mw, mh);

    final Context context = GUI.context;
    final Data data = context.data();
    final IntList list = new IntList();
    int np = 0;
    int r = -1;
    while(++r < mainRects.size) {
      final MapRect rect = mainRects.get(r);
      if(mainRects.get(r).p < np) continue;
      if(dragRect.contains(rect)) {
        list.add(rect.p);
        np = rect.p + data.size(rect.p, data.kind(rect.p));
      }
    }
    final Nodes marked = new Nodes(list.get(), data);
    marked.size = list.size;
    View.notifyMark(marked);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(working) return;
    if(dragRect != null) {
      dragRect = null;
      repaint();
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(working || focused == -1) return;
    if(e.getWheelRotation() > 0) notifyContext(
        new Nodes(focused, GUI.context.data()), false);
    else notifyHist(false);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(working) return;
    if(mainRects == null || e.isControlDown() || e.isAltDown()) return;

    final int key = e.getKeyCode();
    final boolean shift = e.isShiftDown();

    final Context context = GUI.context;
    final Data data = context.data();
    final int size = data.size;
    final Nodes current = context.current();

    if(key == KeyEvent.VK_R) {
      final Random rnd = new Random();
      int pre = 0;
      do {
        pre = rnd.nextInt(size);
      } while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre));
      focused = pre;
      notifySwitch(new Nodes(focused, data));
    } else if(key == KeyEvent.VK_N || key == KeyEvent.VK_B) {
      // jump to next node
      int pre = (current.pre[0] + 1) % size;
      while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre))
        pre = (pre + 1) % size;
      notifySwitch(new Nodes(pre, data));
    } else if(key == KeyEvent.VK_P || key == KeyEvent.VK_Z) {
      // jump to previous node
      int pre = (current.pre[0] == 0 ? size : current.pre[0]) - 1;
      while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre))
        pre = (pre == 0 ? size : pre) - 1;
      notifySwitch(new Nodes(pre, data));
    } else if(key == KeyEvent.VK_S && !slide) {
      // slide show
      slide = true;
      new Action() {
        public void run() {
          while(slide) {
            int pre = context.current().pre[0];
            if(slideForward) {
              pre = (pre + 1) % size;
              while(!ViewData.isLeaf(data, pre)) pre = (pre + 1) % size;
            } else {
              pre = (pre == 0 ? size : pre) - 1;
              while(!ViewData.isLeaf(data, pre))
                pre = (pre == 0 ? size : pre) - 1;
            }
            notifySwitch(new Nodes(pre, data));
            Performance.sleep(slideSpeed);
          }
        }
      }.execute();
    }

    final boolean cursor = key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN ||
      key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT;
    if(!cursor) return;

    if(focusedRect == null) focusedRect = mainRects.get(0).clone();

    int o = GUIProp.fontsize + 4;
    if(key == KeyEvent.VK_UP) {
      mouseY = focusedRect.y + (shift ? focusedRect.h -
          GUIProp.fontsize : 0) - 1;
      if(shift) mouseX = focusedRect.x + (focusedRect.w >> 1);
    } else if(key == KeyEvent.VK_DOWN) {
      mouseY = focusedRect.y + (shift ? o : focusedRect.h + 1);
      if(shift) mouseX = focusedRect.x + (focusedRect.w >> 1);
    } else if(key == KeyEvent.VK_LEFT) {
      mouseX = focusedRect.x + (shift ? focusedRect.w -
          GUIProp.fontsize : 0) - 1;
      if(shift) mouseY = focusedRect.y + (focusedRect.h >> 1);
    } else if(key == KeyEvent.VK_RIGHT) {
      mouseX = focusedRect.x + (shift ? o : focusedRect.w + 1);
      if(shift) mouseY = focusedRect.y + (focusedRect.h >> 1);
    }
    o = mainRects.get(0).w == getWidth() ? (o >> 1) + 1 : 0;
    mouseX = Math.max(o, Math.min(getWidth() - o - 1, mouseX));
    mouseY = Math.max(o << 1, Math.min(getHeight() - o - 1, mouseY));
    if(focus()) repaint();
  }

  /** Slide show flag. */
  boolean slide;
  /** Slide show flag. */
  int slideSpeed = 2000;
  /** Slide show flag. */
  boolean slideForward = true;

  @Override
  public void keyTyped(final KeyEvent e) {
    if(working) return;
    super.keyTyped(e);
    final char ch = e.getKeyChar();
    if(ch == '|') {
      GUIProp.maplayout = (GUIProp.maplayout + 1) % MAPLAYOUTCHOICE.length;
      View.notifyLayout();
    }
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    if(working) return;
    focusedRect = null;
    GUI.get().status.setPerformance(calc());
    repaint();
  }
}
