package org.basex.gui.view.map;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewData;
import org.basex.gui.view.ViewNotifier;
import org.basex.io.IO;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.basex.util.Tokenizer;
import org.deepfs.fs.DeepFS;

/**
 * This view is a TreeMap implementation.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class MapView extends View implements Runnable {
  /** Dynamic zooming steps. */
  private static final int[] ZS = { 0, 0, 0, 0, 20, 80, 180, 320, 540, 840,
      1240, 1740, 2380, 3120, 4000, 4980, 5980, 6860, 7600, 8240, 8740, 9140,
      9440, 9660, 9800, 9900, 9960, 9980, 9980, 9980, 10000};
  /** Number of zooming steps. */
  private static final int ZOOMSIZE = ZS.length - 1;
  /** Maximum zooming step. */
  private static final int MAXZS = ZS[ZOOMSIZE];

  /** Array of current rectangles. */
  private MapRects mainRects;
  /** Array of huge rectangles. */
  private MapRects hugeRects;
  /** Data specific map layout. */
  private MapPainter painter;
  /** Keeps the whole map layout. */
  public MapLayout layout;
  /** Keeps Layout for focused rectangle. */
  public MapLayout tinyLayout;
  ///** Scaled Layout. */
  //public MapLayout hugeLayout;
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
  /** OnClick Zoom. */
  private BufferedImage tinyMap;
  /** Huge Map. */
  private BufferedImage hugeMap;
  /** Linear magnification. */
  private boolean constMag;
  /** Draw ThumbMap. */
  private boolean thumbMap;
  /** Map distortion. */
  private boolean mapdist;
  /** scaling. */
  private static int fkt = 4;
  /** Y pos tiny Map. */
  private int tinyy;
  /** X pos tiny Map. */
  private int tinyx;
  /** W tiny Map. */
  private int tinyw;
  /** H tiny Map. */
  private int tinyh;

  /**
   * Default constructor.
   * @param man view manager
   */
  public MapView(final ViewNotifier man) {
    super(MAPVIEW, HELPMAP, man);
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
    if(painter != null) painter.close();
    painter = null;
    mainRects = null;
    focused = null;
    textLen = null;
    zoomStep = 0;

    final Data data = gui.context.data;
    final GUIProp gprop = gui.prop;
    if(data != null && getWidth() != 0) {
      if(!visible()) return;
      painter = data.fs != null ? new MapFS(this, data.fs, gprop) :
        new MapDefault(this, gprop);
      mainMap = createImage();
      zoomMap = createImage();
      if(gprop.is(GUIProp.MAPINTERACTION)) hugeMap = new BufferedImage(fkt
          * getWidth(), fkt * getHeight(), BufferedImage.TYPE_INT_BGR);
      refreshLayout();
      repaint();
    }
  }

  @Override
  public void refreshFocus() {
    if(mainRects == null || gui.updating) return;
    if(mapdist) {
      focused = null;
      return;
    }
    final int f = gui.context.focused;
    if(f == -1 && focused != null) focused = null;

    final MapRect m = focused;
    final int ms = mainRects.size;
    for(int mi = 0; mi < ms; mi++) {
      final MapRect rect = mainRects.get(mi);
      if(f == rect.pre || mi + 1 < ms && f < mainRects.get(mi + 1).pre) {
        focused = rect;
        break;
      }
    }
    if(focused != m) repaint();
  }

  @Override
  public void refreshMark() {
    if(getWidth() == 0 || mainMap == null) return;

    if(!mapdist) drawMap(mainMap, mainRects, 1f);
    repaint();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    // use simple zooming animation for result node filtering
    final Nodes context = gui.context.current;
    final int hist = gui.notify.hist;
    final boolean page = !more && rectHist[hist + 1] != null
        && rectHist[hist + 1].pre == 0 || more && (context.size() != 1 ||
        focused == null || context.nodes[0] != focused.pre);
    if(page) focused = new MapRect(0, 0, getWidth(), 1);

    zoom(more, quick);
  }

  @Override
  public void refreshLayout() {
    if(painter == null) return;

    // initial rectangle
    final int w = getWidth(), h = getHeight();

    if(gui.prop.is(GUIProp.MAPINTERACTION)) {
      // [JH] use normal rectangles to calculate hugeMap
      // final Data data = gui.context.data();
      // MapLayout hugeLayout = new MapLayout(data, textLen);

      final MapRect rect = new MapRect(0, 0, w, h, 0, 0);
      calc(rect, gui.context.current, mainMap);

      //MapRect dest = new MapRect(0, 0, fkt * w, fkt * h, mainRects.get(0).pre,
      //  mainRects.get(0).level);
      //MapRect source = new MapRect(0, 0, w, h, mainRects.get(0).pre,
      //  mainRects.get(0).level);
      //dest.isLeaf = mainRects.get(0).isLeaf;
      //source.isLeaf = mainRects.get(0).isLeaf;
      //MapRects hugeRects = scaleRects(mainRects, 0, mainRects.size, dest,
      //  source);

      // simple method only scaling all the rectangles including the borders
      hugeRects = new MapRects();
      for(final MapRect r : mainRects) {
        hugeRects.add(new MapRect(fkt * r.x, fkt * r.y, fkt * r.w, fkt * r.h,
            r.pre, r.level));
      }

      painter.init(hugeRects);
      drawMap(hugeMap, hugeRects, fkt);
    } else {
      final MapRect rect = new MapRect(0, 0, w, h, 0, 0);
      calc(rect, gui.context.current, mainMap);
    }

    //  final MapRect hrect = new MapRect(0, 0, fkt * w, fkt * h, 0, 0);
    //  calc(hrect, gui.context.current(), hugeMap);
    //}
    //
    //final MapRect rect = new MapRect(0, 0, w, h, 0, 0);
    //calc(rect, gui.context.current(), mainMap);

    repaint();
  }

  @Override
  public void refreshUpdate() {
    textLen = null;
    refreshContext(false, true);
  }

  @Override
  public boolean visible() {
    return gui.prop.is(GUIProp.SHOWMAP);
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
      zoomSpeed = (int) (Math.log(64 * getWidth() / mainRect.w) + Math.log(64
          * getHeight() / mainRect.h));
    }

    if(quick) {
      gui.updating = false;
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
    focused = null;

    // run zooming
    while(zoomStep > 1) {
      Performance.sleep(zoomSpeed);
      zoomStep--;
      repaint();
    }
    // wait until current painting is finished
    while(gui.painting) Performance.sleep(zoomSpeed);

    // remove old rectangle and repaint map
    zoomStep = 0;
    gui.updating = false;
    focus();
    repaint();
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  private boolean focus() {
    if(gui.updating || mainRects == null) return false;

    /*
     * Loop through all rectangles. As the rectangles are sorted by pre order
     * and small rectangles are descendants of bigger ones, the focused
     * rectangle can be found by simply parsing the array backward.
     */
    int r = mainRects.size;
    while(--r >= 0) {
      final MapRect rect = mainRects.get(r);
      if(rect.contains(mouseX, mouseY)) break;
    }
    // don't focus top rectangles
    final MapRect fr = r >= 0 ? mainRects.get(r) : null;

    // find focused rectangle
    final boolean newFocus = focused != fr || fr != null && fr.thumb;
    focused = fr;

    if(fr != null) gui.cursor(painter.mouse(focused, mouseX, mouseY, false) ?
        CURSORHAND : CURSORARROW);

    if(newFocus) {
      gui.notify.focus(focused != null ? focused.pre : -1, this);
    }

    return newFocus;
  }

  /**
   * Initializes the calculation of the main TreeMap.
   * @param rect initial space to layout rects in
   * @param nodes Nodes to draw in the map
   * @param map image to draw rectangles on
   */
  private void calc(final MapRect rect, final Nodes nodes,
      final BufferedImage map) {

    for(int i = 0; i < 1; i++) {
      // calculate new main rectangles
      initLen();
      layout = new MapLayout(nodes.data, textLen, gui.prop);
      layout.makeMap(rect, new MapList(nodes.nodes.clone()),
          0, nodes.size() - 1);
      mainRects = layout.rectangles.copy();
      // [CG] GUI/MapView: check if the copy is needed; if yes, add comment..
      // mainRects = layout.rectangles;
    }

    painter.init(mainRects);
    drawMap(map, mainRects, 1f);
    focus();

    /*
     * Screenshots: try { File file = new File("screenshot.png");
     * ImageIO.write(mainMap, "png", file); } catch(IOException ex) {
     * ex.printStackTrace(); }
     */
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Data data = gui.context.data;
    final GUIProp gprop = gui.prop;

    if(mainRects == null || mainRects.size == 0) {
      super.paintComponent(g);
      refreshInit();
      return;
    }
    // calculate map
    gui.painting = true;

    // paint map
    final boolean in = zoomStep > 0 && zoomIn;
    final Image img1 = in ? zoomMap : mainMap;
    final Image img2 = in ? mainMap : zoomMap;
    if(zoomStep > 0) {
      tinyh = 0;
      tinyw = 0;
      drawImage(g, img1, -zoomStep);
      drawImage(g, img2, zoomStep);
    } else {
      drawImage(g, mainMap, zoomStep);
    }

    // check if focused rectangle is valid
    if(focused != null && focused.pre >= data.meta.size) focused = null;

    // skip node path view
    if(focused == null || mainRects.size == 1 && focused == mainRects.get(0)) {
      gui.painting = false;
      // return;
      if(focused == null || !focused.thumb) return;
    }

    if(gprop.num(GUIProp.MAPOFFSETS) == 0 && !mapdist && !constMag) {
      g.setColor(COLORS[32]);
      int pre = mainRects.size;
      int par = ViewData.parent(data, focused.pre);
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
    } else if(!mapdist && !constMag) {
      // paint focused rectangle
      final int x = focused.x;
      final int y = focused.y;
      final int w = focused.w;
      final int h = focused.h;
      g.setColor(color6);
      g.drawRect(x, y, w, h);
      g.drawRect(x + 1, y + 1, w - 2, h - 2);

      // draw tag label
      g.setFont(font);
      smooth(g);
      if(data.kind(focused.pre) == Data.ELEM) {
        String tt = Token.string(ViewData.tag(gprop, data, focused.pre));
        if(tt.length() > 32) tt = tt.substring(0, 30) + DOTS;
        BaseXLayout.drawTooltip(g, tt, x, y, getWidth(), focused.level + 5);
      }

      if(focused != null && focused.thumb) {
        focused.x += 3;
        focused.w -= 3;
        final byte[] text = ViewData.content(data, focused.pre, false);
        final TokenList tl = MapRenderer.calculateToolTip(focused,
            Tokenizer.getInfo(text), mouseX, mouseY, getWidth(), g);
        final MapRect mr = new MapRect(getX(), getY(), getWidth(), getHeight());
        MapRenderer.drawToolTip(g, mouseX, mouseY, mr, tl,
            gprop.num(GUIProp.FONTSIZE));
        focused.x -= 3;
        focused.w += 3;
      }
    }
    final int ac = AlphaComposite.SRC_OVER;
    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(ac,
        gprop.num(GUIProp.ZOOMBOXALPHA) / 100.0f));
    if(gprop.is(GUIProp.MAPINTERACTION) && thumbMap) {
      //// set up some alpha display for the overlaid picture
      //final int ac = AlphaComposite.SRC_OVER;
      //((Graphics2D) g).setComposite(AlphaComposite.getInstance(ac,
      //    gprop.num(ZOOMBOXALPHA) / 100.0f));

      g.drawImage(tinyMap, tinyx, tinyy, tinyw, tinyh, this);
      // draw border
      g.setColor(Color.black);
      g.drawRect(tinyx, tinyy, tinyw, tinyh);
    } else if(gprop.is(GUIProp.MAPINTERACTION) && constMag) {
      final int fishw = gprop.num(GUIProp.FISHW);
      final int fishh = gprop.num(GUIProp.FISHH);
      int dxstart = mouseX - fishw / 2;
      int dxend = mouseX + fishw / 2;
      if(dxstart < 0) {
        dxstart = 0;
        dxend = fishw;
      } else if(dxend > getWidth()) {
        dxend = getWidth();
        dxstart = getWidth() - fishw;
      }

      int dystart = mouseY - fishh / 2;
      int dyend = mouseY + fishh / 2;
      if(dystart < 0) {
        dystart = 0;
        dyend = fishh;
      } else if(dyend > getHeight()) {
        dyend = getHeight();
        dystart = getHeight() - fishh;
      }

      int sxstart = fkt * mouseX - fishw / 2;
      int sxend = fkt * mouseX + fishw / 2;
      if(sxstart < 0) {
        sxstart = 0;
        sxend = fishw;
      } else if(sxend > hugeMap.getWidth()) {
        sxend = hugeMap.getWidth();
        sxstart = hugeMap.getWidth() - fishw;
      }

      int systart = fkt * mouseY - fishh / 2;
      int syend = fkt * mouseY + fishh / 2;
      if(systart < 0) {
        systart = 0;
        syend = fishh;
      } else if(syend > hugeMap.getHeight()) {
        syend = hugeMap.getHeight();
        systart = hugeMap.getHeight() - fishh;
      }

      //BufferedImage map = distort(hugeMap.getSubimage(sxstart, systart,
      //    sxend - sxstart, syend - systart));
      //
      //try {
      //  File file = new File("baseXdistorted.png");
      //  ImageIO.write(map, "png", file);
      //} catch(IOException exc) {
      //  exc.printStackTrace();
      //}
      //
      //g.drawImage(map, dxstart, dystart, dxend, dyend,
      //    0, 0, map.getWidth(), map.getHeight(), this);

      g.drawImage(hugeMap, dxstart, dystart, dxend, dyend,
          sxstart, systart, sxend, syend, this);

      // draw border
      g.setColor(Color.black);
      g.drawRect(dxstart, dystart, fishw, fishh);
    }
    gui.painting = false;
  }

  ///**
  // * distorts the image using something like a tanh to the polar coordinates.
  // *
  // * @param image to distort
  // * @return distorted Image
  // */
  //private BufferedImage distort(final BufferedImage image) {
  //  BufferedImage i = new BufferedImage(image.getWidth(), image.getHeight(),
  //      BufferedImage.TYPE_INT_BGR);
  //  int width = image.getWidth(); int height = image.getHeight();
  //  for(int x = 0; x < width; x++) {
  //    for(int y = 0; y < height; y++) {
  //      int nX = (int) Math.tan(x - width / 2);
  //      int nY = (int) Math.tan(y - height / 2);
  //      if(nX > 0 && nX < width && nY > 0 && nY < height)
  //        i.setRGB(nX, nY, image.getRGB(x, y));
  //    }
  //  }
  //  return i;
  //}

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
    g.drawImage(img, r.x, r.y, r.x + r.w, r.y + r.h, 0, 0, getWidth(),
        getHeight(), this);
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
        if(zr.w == 0) zr.w = 1;
        if(zr.h == 0) zr.h = 1;
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
   * @param map Image to draw the map on
   * @param rects calculated rectangles
   * @param sc scale the rectangles
   */
  void drawMap(final BufferedImage map, final MapRects rects, final float sc) {
    final Graphics g = map.getGraphics();
    smooth(g);
    // [CG] GUI/MapView: check if null reference is necessary...
    if(rects != null) painter.drawRectangles(g, rects, sc);
  }

  /**
   * Transforms coordinates into distorted coordinates.
   * @param v coordinate
   * @param m mouse position
   * @param s width/height
   * @return new coordinate
   */
  private double transfer(final double v, final double m, final double s) {
    //double l = 0.5;
    //return (s + s * Math.tanh(0.02 * (v - m))) / 2 * l + v * (1.0 - l);
    return s * Math.tanh(0.008 * (v - m)) + m;
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;

    super.mouseMoved(e);
    // refresh mouse focus
    mouseX = e.getX();
    mouseY = e.getY();

    if(!gui.prop.is(GUIProp.MAPINTERACTION) || !sc(e)) {
      if(focus()) {
        if(!(mouseX > tinyx && mouseX < tinyx + tinyw && mouseY > tinyy &&
            mouseY < tinyy + tinyh)) {
          thumbMap = false;
        }
        repaint();
      } else {
        constMag = false;
        mapdist = false;
        refreshMark();
      }
    } else if(mapdist) {
      mapdist = true;
      final Graphics g = mainMap.getGraphics();
      g.setColor(Color.black);
      g.fillRect(0, 0, getWidth(), getHeight());

      final MapRects distRects = new MapRects();
      for(final MapRect r : mainRects) {
        //int x = (int) (getWidth() + (getWidth() *
        //    Math.tanh(0.01 * (r.x - mouseX))) / 2);
        //int w = (int) (getWidth() *
        //    Math.tanh(0.01 * (r.x + r.w - mouseX)) - x);
        //int y = (int) (getHeight() * Math.tanh(0.01 * (r.y - mouseY)));
        //int h = (int) (getHeight() * Math.tanh(
        //    0.01 * (r.y + r.h - mouseY)) - y);*/
        final int x = (int) transfer(r.x, mouseX, getWidth());
        final int y = (int) transfer(r.y, mouseY, getHeight());
        final int w = (int) transfer(r.x + r.w, mouseX, getWidth()) - x;
        final int h = (int) transfer(r.y + r.h, mouseY, getHeight()) - y;
        distRects.add(new MapRect(x, y, w, h, r.pre, r.level));
      }
      mainMap.flush();
      painter.init(distRects);
      drawMap(mainMap, distRects, 1);
      repaint();
    } else if(gui.prop.is(GUIProp.MAPDIST)) {
      mapdist = true;
      repaint();
    } else {
      constMag = true;
      repaint();
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if(gui.updating) return;

    // process mouse clicks by the specific painter
    if(SwingUtilities.isLeftMouseButton(e) && focused != null)
      painter.mouse(focused, mouseX, mouseY, true);

    if(gui.prop.is(GUIProp.MAPINTERACTION) && !mapdist) {
      thumbMap = true;
      final Data data = gui.context.data;
      int screensize = getWidth() * getHeight();
      screensize = screensize / gui.prop.num(GUIProp.MAPTHUMBSIZE);

      tinyw = (int) Math.sqrt(getWidth() * screensize / getHeight());
      tinyh = screensize / tinyw;

      // tinyy = focused.y + tinyy < getHeight() ? focused.y :
      // focused.y + focused.h - tinyh;
      if(focused.x + focused.w + tinyw < getWidth()) tinyx = focused.x
          + focused.w;
      else if(focused.x - tinyw > 0) tinyx = focused.x - tinyw;
      else tinyx = (getWidth() - tinyw) / 2;

      // tinyx = focused.x + tinyx < getWidth() ? focused.x :
      // focused.x + focused.w - tinyw;
      if(focused.y - tinyh > 0 && mouseY < focused.y + focused.h / 2)
        tinyy = focused.y - tinyh;
      else if(focused.y + focused.h + tinyh < getHeight()) tinyy = focused.y
          + focused.h;
      else tinyy = (getHeight() - tinyh) / 2;

      final MapRect rect = new MapRect(0, 0, tinyw, tinyh, 0, 0);

      final Nodes nodes = new Nodes(focused.pre, data);
      tinyMap = new BufferedImage(tinyw, tinyh, BufferedImage.TYPE_INT_BGR);

      tinyLayout = new MapLayout(nodes.data, textLen, gui.prop);
      tinyLayout.makeMap(rect, new MapList(nodes.nodes.clone()), 0,
          nodes.size() - 1);

      final MapRects rects = new MapRects();
      rects.add(focused);

      painter.init(tinyLayout.rectangles.copy());
      drawMap(tinyMap, tinyLayout.rectangles, 1f);
      repaint();
    } else if(mapdist) {
      mouseMoved(e);
    }
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
    final Nodes marked = gui.context.marked;
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
    if(gui.updating || ++dragTol < 8 || mainRects.sorted != mainRects.list)
      return;

    // refresh mouse focus
    int mx = mouseX;
    int my = mouseY;
    int mw = e.getX() - mx;
    int mh = e.getY() - my;
    if(mw < 0) mx -= mw = -mw;
    if(mh < 0) my -= mh = -mh;
    selBox = new MapRect(mx, my, mw, mh);

    final Data data = gui.context.data;
    final IntList il = new IntList();
    int np = 0;
    final int rl = mainRects.size;
    for(int r = 0; r < rl; r++) {
      final MapRect rect = mainRects.get(r);
      if(mainRects.get(r).pre < np) continue;
      if(selBox.contains(rect)) {
        il.add(rect.pre);
        np = rect.pre + ViewData.size(data, rect.pre);
      }
    }
    gui.notify.mark(new Nodes(il.finish(), data), null);
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
    // [CG] MapView: strange behaviour of mouse wheel
    if(e.getWheelRotation() <= 0) gui.notify.context(
        new Nodes(gui.context.focused, gui.context.data), false, null);
    else gui.notify.hist(false);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(gui.updating || mainRects == null || ignoreTyped(e)) return;

    if(pressed(ESCAPE, e)) {
      tinyw = 0;
      tinyh = 0;
      repaint();
    }

    final boolean cursor = pressed(PREVLINE, e) || pressed(NEXTLINE, e) ||
      pressed(PREV, e) || pressed(NEXT, e);
    if(!cursor) return;

    if(focused == null) focused = mainRects.get(0);

    final int fs = gui.prop.num(GUIProp.FONTSIZE);
    int o = fs + 4;
    final boolean shift = e.isShiftDown();
    if(pressed(PREVLINE, e)) {
      mouseY = focused.y + (shift ? focused.h - fs : 0) - 1;
      if(shift) mouseX = focused.x + (focused.w >> 1);
    } else if(pressed(NEXTLINE, e)) {
      mouseY = focused.y + (shift ? o : focused.h + 1);
      if(shift) mouseX = focused.x + (focused.w >> 1);
    } else if(pressed(PREV, e)) {
      mouseX = focused.x + (shift ? focused.w - fs : 0) - 1;
      if(shift) mouseY = focused.y + (focused.h >> 1);
    } else if(pressed(NEXT, e)) {
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
    painter.reset();

    final Data data = gui.context.current.data;
    if(textLen != null || gui.prop.num(GUIProp.MAPWEIGHT) == 0) return;

    final int size = data.meta.size;
    textLen = new int[size];

    final int[] parStack = new int[IO.MAXHEIGHT];
    int l = 0;
    int par = 0;

    for(int pre = 0; pre < size; pre++) {
      final int kind = data.kind(pre);
      par = data.parent(pre, kind);

      final int ll = l;
      while(l > 0 && parStack[l - 1] > par) {
        textLen[parStack[l - 1]] += textLen[parStack[l]];
        --l;
      }
      if(l > 0 && ll != l) textLen[parStack[l - 1]] += textLen[parStack[l]];

      parStack[l] = pre;

      if(data.fs != null) {
        if(DeepFS.isFileOrDir(data, pre)) {
          textLen[pre] = Token.toInt(data.atom(pre + 2));
          if(DeepFS.isFile(data, pre))
            pre += data.size(pre, kind) - 1; // skip file content
          l++;
        }
      } else {
        if(kind == Data.TEXT || kind == Data.COMM || kind == Data.PI ||
            kind == Data.ATTR) {
          textLen[pre] = data.textLen(pre, kind != Data.ATTR);
        } else if((kind == Data.ELEM || kind == Data.DOC) &&
            data.size(pre, kind) > 1) {
          l++;
        }
      }
    }
    while(--l >= 0)
      textLen[parStack[l]] += textLen[parStack[l + 1]];
  }
}
