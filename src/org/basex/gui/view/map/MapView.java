package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.Text.*;
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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.SwingUtilities;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.dialog.DialogMapInfo;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.ViewNotifier;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewData;
import org.basex.index.FTTokenizer;
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
  private static final int[] ZS = { 0, 31, 113, 205, 356, 553, 844, 1226, 1745,
      2148, 2580, 3037, 3515, 4008, 4511, 5019, 5527, 6030, 6522, 6998, 7453,
      7883, 8283, 8749, 9158, 9456, 9650, 9797, 9885, 9973, 10000};
  /** Number of zooming steps. */
  private static final int ZOOMSIZE = ZS.length - 1;
  /** Maximum zooming step. */
  private static final int MAXZS = ZS[ZOOMSIZE];

  /** Array of current rectangles. */
  private ArrayList<MapRect> mainRects;
  /** Rectangles before layout-change. */
  private ArrayList<MapRect> oldRects;
  /** Data specific map layout. */
  private MapPainter painter;
  /** Determines Layout Algorithm. */
  public MapLayout layouter;

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

  /** Info Dialog. */
  private static DialogMapInfo mapInfo;
  /** Horizontal mouse position. */
  private int mouseX = -1;
  /** Vertical mouse position. */
  private int mouseY = -1;
  /** Drag tolerance. */
  private int dragTol;

  /** Currently focused rectangle. */
  private transient MapRect focusedRect;

  // some data used in tremap info dialog
  /** are these values initialized? true if values were assigned. */
  private static boolean infoinit = false;
  /**number of nodes before change. */
  private static int nno = 0; 
  /**number of nodes now. */
  private static int nnn;
  /** Rectsize before. */
  private static MapRect recto; 
  /** Rectsize now. */
  private static MapRect rectn;
  /** aar old. */
  private static double aaro;
  /** aar now. */
  private static double aarn;
  /** distance change between layouts. */
  private static double distance;
//  /** Hold old data instance. */
//  private static Data dataold;
  
  /** TreeMap. */
  private BufferedImage mainMap;
  /** Zoomed TreeMap. */
  private BufferedImage zoomMap;

  /**
   * Default Constructor.
   * @param man view manager
   * @param help help text
   */
  public MapView(final ViewNotifier man, final byte[] help) {
    super(man, help);
    setMode(Fill.NONE);
    new BaseXPopup(this, POPUP);
  }

  /**
   * Start info dialog, may fill with information.
   * 
   * @param gui reference to main window
   */
  public static void info(final GUI gui) {
    if(!GUIProp.mapinfo || !infoinit) {
      mapInfo = new DialogMapInfo(gui);
    } else if(infoinit) {
      mapInfo = new DialogMapInfo(gui, nno, nnn, recto, rectn, aaro, aarn, 
          distance);
    }
    mapInfo.validate();
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
    focusedRect = null;
    mainRects = null;
    zoomStep = 0;
    slide = false;
    if(painter != null) painter.close();

    final Data data = gui.context.data();
    if(data != null && getWidth() != 0) {
      if(!GUIProp.showmap) return;
      painter = data.fs != null ? new MapFS(this, data.fs) : new MapDefault(
          this);
      mainMap = createImage();
      zoomMap = createImage();
      refreshLayout();
      repaint();
    }
  }

  @Override
  public void refreshFocus() {
    if(mainRects == null || gui.updating) return;
    if(gui.focused == -1 && focusedRect != null) focusedRect = null;

    final MapRect m = focusedRect;
    for(int mi = 0, ms = mainRects.size(); mi < ms; mi++) {
      final MapRect rect = mainRects.get(mi);
      if(gui.focused == rect.pre || mi + 1 < ms
          && gui.focused < mainRects.get(mi + 1).pre) {
        focusedRect = rect;
        break;
      }
    }
    if(focusedRect != m) repaint();
  }

  @Override
  public void refreshMark() {
    if(getWidth() == 0 || mainMap == null) return;
    drawMap(mainMap, mainRects);
    repaint();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    if(!GUIProp.showmap) {
      mainRects = null;
      return;
    }

    // use simple zooming animation for result node filtering
    final Nodes context = gui.context.current();
    final int hist = gui.notify.hist;
    final boolean page = !more
        && rectHist[hist + 1] != null
        && rectHist[hist + 1].pre == 0
        || more
        && (context.size() != 1 || focusedRect == null 
        || context.nodes[0] != focusedRect.pre);
    if(page) focusedRect = new MapRect(0, 0, getWidth(), 1);

    zoom(more, quick);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void refreshLayout() {
    // initial rectangle
    final int w = getWidth(), h = getHeight();
    final MapRect rect = new MapRect(0, 0, w, h, 0, 0);
    mainRects = new ArrayList<MapRect>();
    rectn = rect;
    mainRects = new ArrayList<MapRect>();
    final Nodes nodes = gui.context.current();

    calc(rect, mainRects, nodes, mainMap);

    if(GUIProp.mapinfo) {
      nnn = mainRects.size();
      distance = 0;
      infoinit = true;
      if(mapInfo != null) {
        aaro = (oldRects != null) ? MapLayout.aar(oldRects) : 0;
        aarn = MapLayout.aar(mainRects);
        mapInfo.setValues(nno, nnn, recto, rect, aaro, aarn, distance);
        mapInfo.validate();
      }
    }
    // store Rectangle of this MapLayout
    oldRects = new ArrayList<MapRect>();
    oldRects = (ArrayList<MapRect>) mainRects.clone();
    recto = new MapRect(0, 0, w, h);
    nno = oldRects != null ? oldRects.size() : 0;
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
    gui.updating = !quick;
    zoomIn = more;

    // choose zooming rectangle
    final int hist = gui.notify.hist;
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
    refreshLayout();

    // calculate zooming speed (slower for large zooming scales)
    // [JH] division by zero if rect is to slight
    if(mainRect.w > 0 && mainRect.h > 0) {
      zoomSpeed = (int) (Math.log(128 * getWidth() / mainRect.w) + Math.log(128
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
    focusedRect = null;

    // run zooming
    while(zoomStep > 1) {
      Performance.sleep(zoomSpeed);
      zoomStep--;
      repaint();
    }
    // wait until current painting is finished
    while(gui.painting)
      Performance.sleep(zoomSpeed);

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
    int r = mainRects.size();
    while(--r >= 0) {
      final MapRect rect = mainRects.get(r);
      if(rect.contains(mouseX, mouseY)) break;
    }
    // don't focus top rectangles
    final MapRect fr = r >= 0 ? mainRects.get(r) : null;

    // find focused rectangle
    final boolean newFocus = focusedRect != fr || fr != null && fr.thumb;
    focusedRect = fr;

    if(fr != null) gui.cursor(painter.highlight(focusedRect, mouseX, mouseY,
        false) ? CURSORHAND : CURSORARROW);

    if(newFocus) {
      gui.notify.focus(focusedRect != null ? focusedRect.pre : -1, this);
    }

    return newFocus;
  }

  /**
   * Initializes the calculation of the main TreeMap.
   * 
   * @param rect initial space to layout rects in
   * @param rectangles List of divided rects
   * @param nodes Nodes to draw in the map
   * @param map image to draw rectangles on
   */
  private void calc(final MapRect rect, final ArrayList<MapRect> rectangles,
      final Nodes nodes, final BufferedImage map) {

    // calculate new main rectangles
    if(painter == null) return;
    painter.reset();

    // call recursive TreeMap algorithm
    switch(GUIProp.mapalgo) {
      case 0:
        layouter = new SplitLayout();
        break;
      case 1:
        layouter = new SliceDiceLayout();
        break;
      case 2:
        layouter = new SquarifiedLayout();
        break;
      case 3:
        layouter = new StripLayout();
        break;
      default:
        layouter = new SplitLayout();
        break;
    }

    layouter.calcMap(nodes.data, rect, rectangles, new MapList(nodes.nodes), 0,
        nodes.size(), 0);
    painter.init(rectangles);
    drawMap(map, rectangles);
    focus();

    /*
     * Screenshots: try { File file = new File("screenshot.png");
     * ImageIO.write(mainMap, "png", file); } catch(IOException e) {
     * e.printStackTrace(); }
     */
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Data data = gui.context.data();
    if(data == null || data.meta.size == 0) {
      super.paintComponent(g);
      return;
    }

    if(mainRects == null) {
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
      drawImage(g, img1, -zoomStep);
      drawImage(g, img2, zoomStep);
    } else {
      drawImage(g, mainMap, zoomStep);
    }

    // skip node path view
    if(focusedRect == null || mainRects.size() == 1
        && focusedRect == mainRects.get(0)) {
      gui.painting = false;
      return;
    }

    if(GUIProp.maplayout == 0) {
      g.setColor(COLORS[32]);
      int pre = mainRects.size();
      int par = ViewData.parent(data, focusedRect.pre);
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
      final int x = focusedRect.x;
      final int y = focusedRect.y;
      int w = focusedRect.w;
      int h = focusedRect.h;
      g.setColor(color6);
      g.drawRect(x, y, w, h);
      g.drawRect(x + 1, y + 1, w - 2, h - 2);

      // draw tag label
      if(data.kind(focusedRect.pre) == Data.ELEM) {
        g.setFont(font);
        String tt = Token.string(ViewData.tag(data, focusedRect.pre));
        if(tt.length() > 32) tt = tt.substring(0, 30) + DOTS;
        BaseXLayout.drawTooltip(g, tt, x, y, getWidth(), focusedRect.level + 5);
      }

      if(focusedRect.thumb) {
        final byte[] text = ViewData.content(gui.context.data(),
            focusedRect.pre, false);
        final FTTokenizer ftt = new FTTokenizer(text);
        final int[][] d = ftt.getInfo();
        focusedRect.x += 3;
        focusedRect.w -= 3;
        g.setColor(Color.black);
        switch(focusedRect.thumbal) {
          case 0:
            // MapRenderer.drawThumbnailsToken(g, focusedRect, d,
            // false, mouseX, mouseY);
            MapRenderer.calcThumbnailsToolTip(focusedRect, d, true,
                focusedRect.thumbf, mouseX, mouseY, getWidth(), g, true);

            break;
          case 1:
            MapRenderer.calcThumbnailsToolTip(focusedRect, d, true,
                focusedRect.thumbf, mouseX, mouseY, getWidth(), g, false);
            break;
          case 2:
            MapRenderer.calcThumbnailsToolTip(focusedRect, d, false,
                focusedRect.thumbf, mouseX, mouseY, getWidth(), g, false);
            break;
          default:
            MapRenderer.calcThumbnailsToolTip(focusedRect, d, false, Math.max(
                1.5, focusedRect.thumbf), mouseX, mouseY, getWidth(), g, false);
        }
        MapRenderer.drawToolTip(g, mouseX, mouseY, getX(), getY(), getHeight(),
            getWidth(), focusedRect.acol);
        focusedRect.x -= 3;
        focusedRect.w += 3;
      }

      // draw area round cursor position
      if(GUIProp.mapinteraction == 1) {
        // find out if position under cursor is out of view dimensions
        int myx, myy;
        if(mouseX - GUIProp.lenswidth < 0) myx = 0;
        else if(mouseX + GUIProp.lenswidth > getWidth()) myx = getWidth()
            - (GUIProp.lenswidth << 1);
        else myx = mouseX - GUIProp.lenswidth;

        if(mouseY - GUIProp.lensheight < 0) myy = 0;
        else if(mouseY + GUIProp.lensheight > getHeight()) myy = getHeight()
            - (GUIProp.lensheight << 1);
        else myy = mouseY - GUIProp.lensheight;

        // get area under cursor
        MapRect rectToZoom = new MapRect(myx + GUIProp.lenswidth
            - GUIProp.lensareawidth, myy + GUIProp.lensheight
            - GUIProp.lensareaheight, GUIProp.lensareawidth << 1,
            GUIProp.lensareaheight << 1);
        g.setColor(Color.red);
        g.drawRect(rectToZoom.x, rectToZoom.y, rectToZoom.w, rectToZoom.h);
        // get rectangles to zoom in
        int np = 0;
        final IntList il = new IntList();
        for(int r = 0, rl = mainRects.size(); r < rl; r++) {
          final MapRect rect = mainRects.get(r);
          if(mainRects.get(r).pre < np) continue;
          if(rectToZoom.contains(rect)) {
            il.add(rect.pre);
            np = rect.pre + data.size(rect.pre, data.kind(rect.pre));
          }
        }
        // draw lens border
        g.setColor(Color.black);
        g.drawRect(myx, myy, GUIProp.lenswidth << 1, GUIProp.lensheight << 1);

        // calculate initial rectangle
        w = GUIProp.lenswidth << 1;
        h = GUIProp.lensheight << 1;

        final MapRect rect = new MapRect(0, 0, w, h, 0, 0);
        ArrayList<MapRect> lensRects = new ArrayList<MapRect>();

        final Nodes nodes = new Nodes(gui.focused, data);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
        calc(rect, lensRects, nodes, bi);
        final int ac = AlphaComposite.SRC_OVER;
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(ac, 1.0f));
        drawMap(bi, lensRects);
        g.drawImage(bi, myx, myy, this);
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
    g.drawImage(img, r.x, r.y, r.x + r.w, r.y + r.h, 0, 0, getWidth(),
        getHeight(), this);
  }

  /**
   * Zooms the coordinates of the specified rectangle.
   * @param r rectangle to be zoomed
   * @param zs zooming step
   * 
   *          [JH] division by zero to resolve
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
   * 
   * @param map Image to draw the map on
   * @param rects calculated rectangles
   */
  void drawMap(final BufferedImage map, final ArrayList<MapRect> rects) {
    final Graphics g = map.getGraphics();
    g.setColor(COLORS[2]);
    BaseXLayout.antiAlias(g);
    if(rects != null) painter.drawRectangles(g, rects);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;
    super.mouseMoved(e);
    // refresh mouse focus
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if(gui.updating) return;
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    if(!left || focusedRect == null) return;

    // single/double click?
    if(painter.highlight(focusedRect, mouseX, mouseY, true)) return;
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(gui.updating) return;
    super.mousePressed(e);
    mouseX = e.getX();
    mouseY = e.getY();
    dragTol = 0;
    if(!focus() && gui.focused == -1) return;

    // left/right mouse click?
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final int pre = gui.focused;

    // add or remove marked node
    final Nodes marked = gui.context.marked();
    if(!left) {
      // right mouse button
      if(!marked.contains(pre)) gui.notify.mark(0, null);
    } else if(e.getClickCount() == 2) {
      if(mainRects.size() != 1) gui.notify.context(marked, false, null);
    } else if(e.isShiftDown()) {
      gui.notify.mark(1, null);
    } else if(e.isControlDown()) {
      gui.notify.mark(2, null);
    } else {
      if(!marked.contains(pre)) gui.notify.mark(0, null);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(gui.updating || ++dragTol < 8) return;

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
    for(int r = 0, rl = mainRects.size(); r < rl; r++) {
      final MapRect rect = mainRects.get(r);
      if(mainRects.get(r).pre < np) continue;
      if(selBox.contains(rect)) {
        il.add(rect.pre);
        np = rect.pre + data.size(rect.pre, data.kind(rect.pre));
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
    if(gui.updating || gui.focused == -1) return;
    if(e.getWheelRotation() > 0) gui.notify.context(new Nodes(gui.focused,
        gui.context.data()), false, null);
    else gui.notify.hist(false);
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    if(gui.updating) return;
    if(mainRects == null || e.isControlDown() || e.isAltDown()) return;

    final int key = e.getKeyCode();
    final boolean shift = e.isShiftDown();

    final Context context = gui.context;
    final Data data = context.data();
    final int size = data.meta.size;
    final Nodes current = context.current();

    if(key == KeyEvent.VK_R) {
      final Random rnd = new Random();
      int pre = 0;
      do {
        pre = rnd.nextInt(size);
      } while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre));
      gui.focused = pre;
      gui.notify.jump(new Nodes(gui.focused, data));
    } else if(key == KeyEvent.VK_N || key == KeyEvent.VK_B) {
      // jump to next node
      int pre = (current.nodes[0] + 1) % size;
      while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre))
        pre = (pre + 1) % size;
      gui.notify.jump(new Nodes(pre, data));
    } else if(key == KeyEvent.VK_P || key == KeyEvent.VK_Z) {
      // jump to previous node
      int pre = (current.nodes[0] == 0 ? size : current.nodes[0]) - 1;
      while(data.kind(pre) != Data.ELEM || !ViewData.isLeaf(data, pre))
        pre = (pre == 0 ? size : pre) - 1;
      gui.notify.jump(new Nodes(pre, data));
    } else if(key == KeyEvent.VK_S && !slide) {
      // slide show
      slide = true;
      new Action() {
        public void run() {
          while(slide) {
            int pre = context.current().nodes[0];
            if(slideForward) {
              pre = (pre + 1) % size;
              while(!ViewData.isLeaf(data, pre))
                pre = (pre + 1) % size;
            } else {
              pre = (pre == 0 ? size : pre) - 1;
              while(!ViewData.isLeaf(data, pre))
                pre = (pre == 0 ? size : pre) - 1;
            }
            gui.notify.jump(new Nodes(pre, data));
            Performance.sleep(slideSpeed);
          }
        }
      }.execute();
    }

    final boolean cursor = key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN
        || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT;
    if(!cursor) return;

    if(focusedRect == null) focusedRect = mainRects.get(0).clone();

    int o = GUIProp.fontsize + 4;
    if(key == KeyEvent.VK_UP) {
      mouseY = focusedRect.y + (shift ? focusedRect.h - GUIProp.fontsize : 0)
          - 1;
      if(shift) mouseX = focusedRect.x + (focusedRect.w >> 1);
    } else if(key == KeyEvent.VK_DOWN) {
      mouseY = focusedRect.y + (shift ? o : focusedRect.h + 1);
      if(shift) mouseX = focusedRect.x + (focusedRect.w >> 1);
    } else if(key == KeyEvent.VK_LEFT) {
      mouseX = focusedRect.x + (shift ? focusedRect.w - GUIProp.fontsize : 0)
          - 1;
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
    if(gui.updating) return;
    super.keyTyped(e);
    final char ch = e.getKeyChar();
    if(ch == '|') {
      GUIProp.maplayout = (GUIProp.maplayout + 1) % MAPLAYOUTCHOICE.length;
      gui.notify.layout();
    }
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    if(gui.updating) return;
    focusedRect = null;
    mainMap = createImage();
    zoomMap = createImage();
    refreshLayout();
  }
}
