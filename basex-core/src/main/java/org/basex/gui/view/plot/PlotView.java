package org.basex.gui.view.plot;

import static org.basex.core.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;
import org.basex.index.stats.*;
import org.basex.util.list.*;

/**
 * A scatter plot visualization of the database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class PlotView extends View {
  /** Whitespace between captions. */
  static final int CAPTIONWHITESPACE = 10;
  /** Rotate factor. */
  private static final double ROTATE = Math.sin(30);
  /** Plot margin: top, left, bottom, right margin. */
  private static final int[] MARGIN = new int[4];
  /** Maximum length of axis caption text. */
  private static final int MAXL = 11;
  /** Position where over-length text is cut off. */
  private static final int CUTOFF = 10;

  /** X axis selector. */
  final BaseXCombo xCombo;
  /** Y axis selector. */
  final BaseXCombo yCombo;
  /** Item selector combo. */
  final BaseXCombo itemCombo;
  /** Dot size in plot view. */
  final BaseXSlider dots;

  /** Data reference. */
  PlotData plotData;
  /** Keeps track of changes in the plot. */
  boolean plotChanged;
  /** Indicates if global marked nodes should be drawn. */
  boolean drawSubNodes;
  /** Indicates if the buffered image for marked nodes has to be redrawn. */
  boolean markingChanged;

  /** Logarithmic display. */
  private final BaseXCheckBox xLog;
  /** Logarithmic display. */
  private final BaseXCheckBox yLog;
  /** Bounding box which supports selection of multiple items. */
  private final ViewRect selectionBox;

  /** Item image. */
  private BufferedImage itemImg;
  /** Marked item image. */
  private BufferedImage itemImgMarked;
  /** Focused item image. */
  private BufferedImage itemImgFocused;
  /** Child node of marked node. */
  private BufferedImage itemImgSub;
  /** Buffered plot image. */
  private BufferedImage plotImg;
  /** Buffered image of marked items. */
  private BufferedImage markedImg;
  /** X coordinate of mouse pointer. */
  private int mouseX;
  /** Y coordinate of mouse pointer. */
  private int mouseY;
  /** Current plot height. */
  private int plotHeight;
  /** Current plot width. */
  private int plotWidth;

  /** Flag for mouse dragging actions. */
  private boolean dragging;
  /** Indicates if a filter operation is self implied or was triggered by
   * another view. */
  private boolean rightClick;
  /** Context which is displayed in the plot after a context change which was
   * triggered by the plot itself. */
  private Nodes nextContext;

  /**
   * Default constructor.
   * @param man view manager
   */
  public PlotView(final ViewNotifier man) {
    super(PLOTVIEW, man);
    border(5).layout(new BorderLayout());

    final BaseXBack panel = new BaseXBack(Fill.NONE).layout(new BorderLayout());

    Box box = new Box(BoxLayout.X_AXIS);
    xLog = new BaseXCheckBox(PLOTLOG, false, gui);
    xLog.setSelected(gui.gopts.get(GUIOptions.PLOTXLOG));
    xLog.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        gui.gopts.invert(GUIOptions.PLOTXLOG);
        refreshUpdate();
      }
    });
    dots = new BaseXSlider(-6, 6, gui.gopts.get(GUIOptions.PLOTDOTS), gui, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        gui.gopts.set(GUIOptions.PLOTDOTS, dots.value());
        refreshLayout();
      }
    });
    BaseXLayout.setWidth(dots, 40);
    yLog = new BaseXCheckBox(PLOTLOG, false, gui);
    yLog.setSelected(gui.gopts.get(GUIOptions.PLOTYLOG));
    yLog.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        gui.gopts.invert(GUIOptions.PLOTYLOG);
        refreshUpdate();
      }
    });
    box.add(yLog);
    box.add(Box.createHorizontalGlue());
    box.add(dots);
    box.add(Box.createHorizontalGlue());
    box.add(xLog);
    panel.add(box, BorderLayout.NORTH);

    box = new Box(BoxLayout.X_AXIS);
    xCombo = new BaseXCombo(gui);
    xCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        setAxis(plotData.xAxis, xCombo);
      }
    });
    yCombo = new BaseXCombo(gui);
    yCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        setAxis(plotData.yAxis, yCombo);
      }
    });
    itemCombo = new BaseXCombo(gui);
    itemCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String item = itemCombo.getSelectedItem();
        plotData.xAxis.log = gui.gopts.get(GUIOptions.PLOTXLOG);
        plotData.yAxis.log = gui.gopts.get(GUIOptions.PLOTYLOG);
        if(plotData.setItem(item)) {
          plotChanged = true;
          markingChanged = true;

          final String[] keys =
            plotData.getCategories(token(item)).toStringArray();
          xCombo.setModel(new DefaultComboBoxModel(keys));
          yCombo.setModel(new DefaultComboBoxModel(keys));
          if(keys.length > 0) {
            // choose name category as default for horizontal axis
            xCombo.setSelectedIndex(Math.min(1, keys.length));
            yCombo.setSelectedIndex(0);
          }
        }
        drawSubNodes = true;
        markingChanged = true;
        repaint();
      }
    });
    box.add(yCombo);
    box.add(Box.createHorizontalStrut(3));
    box.add(new BaseXLabel("Y"));
    box.add(Box.createHorizontalGlue());
    box.add(itemCombo);
    box.add(Box.createHorizontalGlue());
    box.add(new BaseXLabel("X"));
    box.add(Box.createHorizontalStrut(3));
    box.add(xCombo);
    panel.add(box, BorderLayout.SOUTH);
    add(panel, BorderLayout.SOUTH);

    new BaseXPopup(this, POPUP);
    selectionBox = new ViewRect();
    refreshLayout();
  }

  /**
   * Changes the axis assignment.
   * @param ax plot axis
   * @param cb combo box
   */
  void setAxis(final PlotAxis ax, final BaseXCombo cb) {
    final String cs = cb.getSelectedItem();
    if(!ax.setAxis(cs)) return;
    plotChanged = true;
    markingChanged = true;
    repaint();

    // prevent both combo boxes to show the same category
    final BaseXCombo ocb = cb == xCombo ? yCombo : xCombo;
    if(cs.equals(ocb.getSelectedItem())) {
      final int i = ocb.getSelectedIndex();
      ocb.setSelectedIndex(i > 0 ? i - 1 : i + 1);
    }
  }

  /**
   * Creates a buffered image for items.
   * @param focus create image of focused item if true
   * @param marked create image of marked item
   * @param markedSub child node of marked node
   * @return item image
   */
  private BufferedImage itemImage(final boolean focus, final boolean marked,
      final boolean markedSub) {

    final int size = Math.max(1, fontSize +
        gui.gopts.get(GUIOptions.PLOTDOTS) - (focus ? 2 : marked || markedSub ? 4 : 6));
    final BufferedImage img = new BufferedImage(size, size, Transparency.TRANSLUCENT);

    final Graphics g = img.getGraphics();
    smooth(g);

    Color c = color1A;
    if(marked) c = colormark1A;
    if(markedSub) c = colormark2A;
    if(focus) c = color4;

    g.setColor(c);
    g.fillOval(0, 0, size, size);
    return img;
  }

  /**
   * Precalculates the plot and returns the result as buffered image.
   */
  private void createPlotImage() {
    plotImg = new BufferedImage(getWidth(), getHeight(), Transparency.BITMASK);
    final Graphics g = plotImg.getGraphics();
    smooth(g);

    // draw axis and grid
    drawAxis(g, true);
    drawAxis(g, false);

    // draw items
    g.setColor(color4);
    for(int i = 0; i < plotData.pres.length; ++i) {
      drawItem(g, plotData.xAxis.co[i], plotData.yAxis.co[i], false, false, false);
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    if(plotData == null) {
      refreshInit();
      return;
    }

    final int w = getWidth();
    final int h = getHeight();
    plotWidth = w - (MARGIN[1] + MARGIN[3]);
    plotHeight = h - (MARGIN[0] + MARGIN[2]);
    final int sz = sizeFactor();

    g.setFont(font);
    g.setColor(Color.black);
    final Data data = gui.context.data();
    final boolean nd = data == null;
    if(nd || plotWidth - sz < 0 || plotHeight - sz < 0) {
      BaseXLayout.drawCenter(g, nd ? NO_DATA : NO_PIXELS, w, h / 2 - MARGIN[0]);
      return;
    }

    // draw buffered plot image
    if(plotImg == null || plotChanged) createPlotImage();
    g.drawImage(plotImg, 0, 0, this);

    // draw buffered image of marked items
    if(markingChanged || markedImg == null) createMarkedNodes();
    g.drawImage(markedImg, 0, 0, this);
    if(plotData.pres.length < 1) return;

    gui.painting = true;

    /*
     * Possibly, the focused node is not shown in this view (if it was focused
     * in another view). In this case, the ancestor with the smallest distance
     * to the focused node is selected.
     */
    int focused = gui.context.focused;
    if(focused != -1) {
      final int itmID = data.tagindex.id(plotData.item);
      int k = data.kind(focused);
      int name = data.name(focused);
      while(focused > 0 && itmID != name) {
        focused = data.parent(focused, k);
        if(focused > -1) {
          k = data.kind(focused);
          name = data.name(focused);
        }
      }
    }

    // draw focused item
    final int f = plotData.findPre(focused);
    if(f > -1) {
      // determine number of overlapping nodes (plotting second)
      final int ol = overlappingNodes(f).length;
      if(!dragging) {
        final double x1 = plotData.xAxis.co[f];
        final double y1 = plotData.yAxis.co[f];
        drawItem(g, x1, y1, true, false, false);
        // draw focused x and y value
        g.setFont(font);
        final int textH = g.getFontMetrics().getHeight();

        final String x = formatString(true, focused);
        final String y = formatString(false, focused);
        String label = x.length() > 16 ? x.substring(0, 14) + ".." : x;
        if(!x.isEmpty() && !y.isEmpty()) label += " | ";
        label += y.length() > 16 ? y.substring(0, 14) + ".." : y;
        final int xa = calcCoordinate(true, x1) + 15;
        int ya = calcCoordinate(false, y1) + gui.gopts.get(GUIOptions.PLOTDOTS);
        final int ww = getWidth();

        final int id = ViewData.nameID(data);
        final byte[] nm = data.attValue(id, focused);
        String name = nm != null ? string(nm) : "";
        if(!name.isEmpty() && plotData.xAxis.attrID != id &&
            plotData.yAxis.attrID != id) {

          if(ol > 1) name = ol + "x: " + name + ", ...";
          final int lw = BaseXLayout.width(g, label);
          if(ya < MARGIN[0] + textH && xa < w - lw) {
            ya += 2 * textH - gui.gopts.get(GUIOptions.PLOTDOTS);
          }
          if(xa > w - lw)
            BaseXLayout.drawTooltip(g, name + COLS + label, xa, ya, ww, 10);
          else {
            BaseXLayout.drawTooltip(g, name, xa, ya - textH, ww, 10);
            BaseXLayout.drawTooltip(g, label, xa, ya, ww, 10);
          }
        } else {
          if(ol > 1) label = label.isEmpty() ? ol + "x" :
            ol + "x: " + label + ", ...";
          BaseXLayout.drawTooltip(g, label, xa, ya, ww, 10);
        }
      }
    }

    // draw selection box
    if(dragging) {
      g.setColor(color2A);
      final int selW = selectionBox.w;
      final int selH = selectionBox.h;
      final int x1 = selectionBox.x;
      final int y1 = selectionBox.y;
      g.fillRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH,
          Math.abs(selW), Math.abs(selH));
      g.setColor(color3A);
      g.drawRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH,
          Math.abs(selW), Math.abs(selH));
    }
    markingChanged = false;
    plotChanged = false;
    gui.painting = false;
  }

  /**
   * Draws marked nodes.
   */
  private void createMarkedNodes() {
    final Data data = gui.context.data();
    markedImg = new BufferedImage(getWidth(), getHeight(), Transparency.BITMASK);
    final Graphics gi = markedImg.getGraphics();
    smooth(gi);

    final Nodes marked = gui.context.marked;
    if(marked.size() == 0) return;
    final int[] m = Arrays.copyOf(marked.pres, marked.pres.length);
    int i = 0;

    // no child nodes of the marked context nodes are marked
    if(!drawSubNodes) {
      while(i < m.length) {
        final int pi = plotData.findPre(m[i]);
        if(pi > -1) drawItem(gi, plotData.xAxis.co[pi], plotData.yAxis.co[pi], false, true, false);
        ++i;
      }
      return;
    }

    // if nodes are marked in another view, the given nodes as well as their
    // descendants are checked for intersection with the nodes displayed in
    // the plot
    Arrays.sort(m);
    final int[] p = plotData.pres;
    int k = plotData.findPre(m[0]);

    if(k > -1) {
      drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k], false, true, false);
      ++k;
    } else {
      k = -k;
      --k;
    }

    // context change (triggered by another view).
    // descendants of marked node set are also checked for intersection
    // with currently plotted nodes
    while(i < m.length && k < p.length) {
      final int a = m[i];
      final int b = p[k];
      final int ns = data.size(a, data.kind(a)) - 1;
      if(a == b) {
        drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k], false, true, false);
        ++k;
      } else if(a + ns >= b) {
        if(a < b) drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k], false, false, true);
        ++k;
      } else {
        ++i;
      }
    }
  }

  /**
   * Draws a plot item at the given position. If the item to be drawn is
   * focused the focused item buffered image is used.
   * @param g graphics reference
   * @param x x coordinate
   * @param y y coordinate
   * @param focus a focused item is drawn
   * @param marked item is marked
   * @param sub item is a child of a marked node
   */
  private void drawItem(final Graphics g, final double x, final double y, final boolean focus,
      final boolean marked, final boolean sub) {
    final int x1 = calcCoordinate(true, x);
    final int y1 = calcCoordinate(false, y);

    final BufferedImage img = focus ? itemImgFocused : marked ?
        itemImgMarked : sub ? itemImgSub : itemImg;
    final int size = img.getWidth() / 2;
    g.drawImage(img, x1 - size, y1 - size, this);
  }

  /**
   * Draws the x axis of the plot.
   * @param g graphics reference
   * @param drawX drawn axis is x axis
   */
  private void drawAxis(final Graphics g, final boolean drawX) {
    g.setColor(color2A);

    final int sz = sizeFactor();
    // the painting space provided for items which lack no value
    final int pWidth = plotWidth - sz;
    final int pHeight = plotHeight - sz;

    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;

    // drawing horizontal axis line
    if(drawX) {
      if(plotChanged) {
        if(plotData.pres.length > 0) axis.calcCaption(pWidth);
        final StatsType type = plotData.xAxis.type;
        xLog.setEnabled((type == StatsType.DOUBLE ||
            type == StatsType.INTEGER) &&
            Math.abs(axis.min - axis.max) >= 1);
      }
    } else {
      // drawing vertical axis line
      if(plotChanged) {
        if(plotData.pres.length > 0) axis.calcCaption(pHeight);
        final StatsType type = plotData.yAxis.type;
        yLog.setEnabled((type == StatsType.DOUBLE ||
            type == StatsType.INTEGER) &&
            Math.abs(axis.min - axis.max) >= 1);
      }
    }
    if(plotData.pres.length < 1) {
      drawCaptionAndGrid(g, drawX, "", 0);
      drawCaptionAndGrid(g, drawX, "", 1);
      return;
    }

    // getting some axis specific data
    final StatsType type = axis.type;
    final int nrCaptions = axis.nrCaptions;
    final double step = axis.actlCaptionStep;
    final double capRange = 1.0d / (nrCaptions - 1);
    g.setFont(font);

    // draw axis and assignment for TEXT data
    if(type == StatsType.TEXT) {
      final int nrCats = axis.nrCats;
      final double[] coSorted = Arrays.copyOf(axis.co, axis.co.length);
      // draw min / max caption
      drawCaptionAndGrid(g, drawX, nrCats > 1 ? string(axis.firstCat) : "", 0);
      drawCaptionAndGrid(g, drawX, nrCats > 1 ? string(axis.lastCat) : "", 1);
      // return if insufficient plot space
      if(nrCaptions == 0) return;

      // get sorted axis item coordinates
      Arrays.sort(coSorted);
      // optimum caption position
      double op = capRange;
      final int cl = coSorted.length;
      int i = 0;
      // find first non .0d coordinate value
      while(i < cl && coSorted[i] == 0) ++i;
      // find nearest position for next axis caption
      while(i < cl && op < 1.0d - 0.4d * capRange) {
        if(coSorted[i] > op) {
          final double distL = Math.abs(coSorted[i - 1] - op);
          final double distG = Math.abs(coSorted[i] - op);
          op = distL < distG ? coSorted[i - 1] : coSorted[i];

          int j = 0;
          // find value for given plot position
          while(j < axis.co.length && axis.co[j] != op) ++j;
          drawCaptionAndGrid(g, drawX, string(axis.getValue(plotData.pres[j])), op);
          // increase to next optimum caption position
          op += capRange;
        }
        ++i;
      }
      if(nrCats == 1) {
        op = .5d;
        int j = 0;
        // find value for given plot position
        while(j < axis.co.length && axis.co[j] != op) ++j;
        drawCaptionAndGrid(g, drawX, string(axis.getValue(plotData.pres[j])), op);
      }
      // axis is drawn for numerical data, type INT/DBL
    } else {
      final boolean noRange = axis.max - axis.min == 0;
      // draw min and max grid line
      drawIntermediateGridLine(g, drawX, 0, null);
      drawIntermediateGridLine(g, drawX, 1, null);

      // return if insufficient plot space
      if(nrCaptions == 0) return;

      // if min equal max, draw min in plot middle
      if(noRange) {
        drawCaptionAndGrid(g, drawX, BaseXLayout.value(axis.min), .5d);
        return;
      }

      int c = 0;

      // draw LOGARITHMIC SCALE
      if(axis.log) {
        int l;
        double a;
        double b;

        // draw labels for negative values
        if(axis.min < 0) {
          l = 0;
          a = -1;
          while(a >= axis.min) {
            if(a <= axis.max && adequateDistance(drawX, a, 0)) {
              drawCaptionAndGrid(g, drawX, BaseXLayout.value(a), axis.calcPosition(a));
            }
            final int lim = (int) (-1 * Math.pow(10, l + 1));
            double last = a;
            b = 2 * a;
            while(b > lim && b >= axis.min) {
              if(adequateDistance(drawX, last, b) &&
                  adequateDistance(drawX, lim, b) &&
                  b < axis.max) {
                drawIntermediateGridLine(g, drawX, axis.calcPosition(b), BaseXLayout.value(b));
                last = b;
              }
              b += a;
            }

            ++l;
            a = -1 * Math.pow(10, l);
          }
        }

        // draw 0 label if necessary
        if(0 >= axis.min && 0 <= axis.max)
          drawCaptionAndGrid(g, drawX, BaseXLayout.value(0), axis.calcPosition(0));

        // draw labels > 0
        if(axis.max > 0) {
          l = 0;
          a = 1;
          while(a <= axis.max) {
            if(a >= axis.min && adequateDistance(drawX, a, 0)) {
              drawCaptionAndGrid(g, drawX, BaseXLayout.value(a), axis.calcPosition(a));
            }
            final int lim = (int) Math.pow(10, l + 1);
            double last = a;
            b = 2 * a;
            while(b < lim && b <= axis.max) {
              if(adequateDistance(drawX, last, b) &&
                  adequateDistance(drawX, lim, b) &&
                  b > axis.min) {
                drawIntermediateGridLine(g, drawX, axis.calcPosition(b),
                    BaseXLayout.value(b));
                last = b;
              }
              b += a;
            }

            ++l;
            a = Math.pow(10, l);
          }
        }
        // draw LINEAR SCALE
      } else {
        // draw captions between min and max
        double d = axis.calcPosition(axis.startvalue);
        double f = axis.startvalue;
        while(d < 1.0d - .25d / nrCaptions) {
          ++c;
          drawCaptionAndGrid(g, drawX, BaseXLayout.value(f), d);
          f += step;
          d = axis.calcPosition(f);
        }
        // draw min/max labels if little space available
        if(c < 2) {
          drawCaptionAndGrid(g, drawX, BaseXLayout.value(axis.min), 0.0);
          drawCaptionAndGrid(g, drawX, BaseXLayout.value(axis.max), 1.0);
        }
      }
    }
  }

  /**
   * Determines if two points on the axis have an adequate distance.
   * @param drawX drawX
   * @param a first point
   * @param b second point
   * @return a and b have adequate distance
   */
  private boolean adequateDistance(final boolean drawX, final double a, final double b) {
    final double t = drawX ? 1.8d : 1.3d;
    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;
    return Math.abs(calcCoordinate(drawX, axis.calcPosition(a)) -
    calcCoordinate(drawX, axis.calcPosition(b))) >= sizeFactor() / t;
  }

  /**
   * Draws an axis caption to the specified position.
   * @param g graphics reference
   * @param drawX draw caption on x axis
   * @param caption given caption string
   * @param d relative position in plot view depending on axis
   */
  private void drawCaptionAndGrid(final Graphics g, final boolean drawX, final String caption,
      final double d) {
    String cap = caption;
    // if label is too long, it is is chopped to the first characters
    if(cap.length() > MAXL) cap = cap.substring(0, CUTOFF) + "..";

    final int pos = calcCoordinate(drawX, d);
    final int h = getHeight();
    final int w = getWidth();
    final int textH = g.getFontMetrics().getHeight();
    final int fs = fontSize;
    final int imgW = BaseXLayout.width(g, cap) + fs;
    final BufferedImage img = createCaptionImage(g, cap, false, imgW);

    // ... after that
    // the image and the grid line are drawn beside x / y axis
    g.setColor(color2A);
    if(drawX) {
      final int y = h - MARGIN[2];
      g.drawImage(img, pos - imgW + textH - fs + 3, y, this);
      g.drawLine(pos, MARGIN[0], pos, y + fs / 2);
      g.drawLine(pos - 1, MARGIN[0], pos - 1, y + fs / 2);
    } else {
      g.drawImage(img, MARGIN[1] - imgW - fs / 2, pos - fs, this);
      g.drawLine(MARGIN[1] - fs / 2, pos, w - MARGIN[3], pos);
      g.drawLine(MARGIN[1] - fs / 2, pos + 1, w - MARGIN[3], pos + 1);
    }
  }

  /**
   * Creates a buffered image for a given string which serves as axis caption.
   * @param g Graphics reference
   * @param caption caption string
   * @param im intermediate caption (lighter color)
   * @param imgW image width
   * @return buffered image
   */
  private BufferedImage createCaptionImage(final Graphics g, final String caption, final boolean im,
      final int imgW) {

    final int textH = g.getFontMetrics().getHeight();
    final int fs = fontSize;

    // caption labels are rotated, for both x and y axis. first a buffered
    // image is created which displays the rotated label ...
    final int imgH = 160;
    final BufferedImage img = new BufferedImage(imgW, imgH, Transparency.BITMASK);
    final Graphics2D g2d = img.createGraphics();
    smooth(g2d);
    g2d.rotate(ROTATE, imgW, textH);
    g2d.setFont(font);
    g2d.setColor(im ? color3 : Color.black);
    g2d.drawString(caption, fs, fs);
    return img;
  }

  /**
   * Draws intermediate grid lines without caption.
   * @param g Graphics reference
   * @param drawX draw line for x axis
   * @param d relative position of grid line
   * @param caption caption to draw. if cap = null, no caption is drawn
   */
  private void drawIntermediateGridLine(final Graphics g, final boolean drawX, final double d,
      final String caption) {
    String cap = caption;
    final int pos = calcCoordinate(drawX, d);
    final int h = getHeight();
    final int w = getWidth();
    final int fs = fontSize;
    final int sf = sizeFactor();
    g.setColor(color2A);

    if(cap != null) {
      if(cap.length() > MAXL) cap = cap.substring(0, CUTOFF) + "..";
      final int textH = g.getFontMetrics().getHeight();
      final int imgW = BaseXLayout.width(g, cap) + fs;
      final BufferedImage img = createCaptionImage(g, cap, true, imgW);
      final int y = h - MARGIN[2];
      if(drawX) {
        g.drawImage(img, pos - imgW + textH - fs + 3, y - textH / 3, this);
        g.drawLine(pos, MARGIN[0], pos, h - MARGIN[2]);
      } else {
        g.drawImage(img, MARGIN[1] - imgW - fs / 2, pos - fs, this);
        g.drawLine(MARGIN[1], pos, w - MARGIN[3], pos);
      }
    } else {
      if(drawX) {
        g.drawLine(pos, MARGIN[0], pos, h - MARGIN[2] - sf);
      } else {
        g.drawLine(MARGIN[1] + sf, pos, w - MARGIN[3], pos);
      }
    }
  }

  /**
   * Returns a coordinate for a specific double value of an item.
   * @param d relative coordinate of specific item
   * @param drawX calculated value is x value
   * @return absolute coordinate
   */
  private int calcCoordinate(final boolean drawX, final double d) {
    final int sz = sizeFactor();
    if(drawX) {
      // items with value -1 lack a value for the specific attribute
      if(d == -1) return (int) (MARGIN[1] + sz * .35d);
      final int width = getWidth();
      final int xSpace = width - (MARGIN[1] + MARGIN[3]) - sz;
      return (int) (d * xSpace) + MARGIN[1] + sz;
    }

    final int height = getHeight();
    if(d == -1) return height - MARGIN[2] - sz / 4;
    final int ySpace = height - (MARGIN[0] + MARGIN[2]) - sz;
    return ySpace - (int) (d * ySpace) + MARGIN[0];
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    // all plot data is recalculated, assignments stay the same
    plotData.refreshItems(nextContext != null && more && rightClick ?
        nextContext : gui.context.current(), !more || !rightClick);
    plotData.xAxis.log = gui.gopts.get(GUIOptions.PLOTXLOG);
    plotData.xAxis.refreshAxis();
    plotData.yAxis.log = gui.gopts.get(GUIOptions.PLOTYLOG);
    plotData.yAxis.refreshAxis();

    nextContext = null;
    drawSubNodes = !rightClick;
    rightClick = false;
    plotChanged = true;
    markingChanged = true;
    repaint();
  }

  @Override
  public void refreshFocus() {
    repaint();
  }

  @Override
  public void refreshInit() {
    plotData = null;

    final Data data = gui.context.data();
    if(data == null || !visible()) return;

    plotData = new PlotData(gui.context);

    final String[] items = plotData.getItems().toStringArray();
    itemCombo.setModel(new DefaultComboBoxModel(items));

    // set first item and trigger assignment of axis assignments
    if(items.length > 0) itemCombo.setSelectedIndex(0);

    drawSubNodes = true;
    markingChanged = true;
    plotChanged = true;
    repaint();
  }

  @Override
  public void refreshLayout() {
    itemImg = itemImage(false, false, false);
    itemImgMarked = itemImage(false, true, false);
    itemImgFocused = itemImage(true, false, false);
    itemImgSub = itemImage(false, false, true);
    final int sz = sizeFactor() / 2;
    MARGIN[0] = sz + 7;
    MARGIN[1] = sz * 6;
    MARGIN[2] = 55 + sz * 7;
    MARGIN[3] = sz + 3;
    plotChanged = true;
    markingChanged = true;
    if(plotData == null) return;
    repaint();
  }

  @Override
  public void refreshMark() {
    drawSubNodes = true;
    markingChanged = true;
    repaint();
  }

  @Override
  public void refreshUpdate() {
    refreshContext(false, true);
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWPLOT);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWPLOT, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  /**
   * Locates the nearest item to the mouse pointer.
   * @return item focused
   */
  private boolean focus() {
    final int size = itemImg.getWidth() / 2;
    int focusedPre = gui.context.focused;
    // if mouse pointer is outside of the plot the focused item is set to -1,
    // focus may be refreshed, if necessary
    if(mouseX < MARGIN[1] ||
        mouseX > getWidth() - MARGIN[3] + size ||
        mouseY < MARGIN[0] - size || mouseY > getHeight() - MARGIN[2]) {
      // focused item already -1, no refresh needed
      if(focusedPre == -1) {
        return false;
      }
      gui.notify.focus(-1, this);
      return true;
    }

    // find focused item.
    focusedPre = -1;
    int dist = Integer.MAX_VALUE;
    // all displayed items are tested for focus
    for(int i = 0; i < plotData.pres.length && dist != 0; ++i) {
      // coordinates and distances for current tested item are calculated
      final int x = calcCoordinate(true, plotData.xAxis.co[i]);
      final int y = calcCoordinate(false, plotData.yAxis.co[i]);
      final int distX = Math.abs(mouseX - x);
      final int distY = Math.abs(mouseY - y);
      final int sz = sizeFactor() / 4;
      // if x and y distances are smaller than offset value and the
      // x and y distances combined is smaller than the actual minimal
      // distance of any item tested so far, the current item is considered
      // as a focus candidate
      if(distX < sz && distY < sz) {
        final int currDist = distX * distY;
        if(currDist < dist) {
          dist = currDist;
          focusedPre = plotData.pres[i];
        }
      }
    }

    // if the focus changed, views are refreshed
    if(focusedPre != gui.context.focused) {
      gui.notify.focus(focusedPre, this);
      return true;
    }
    return false;
  }

  /**
   * Determines all nodes lying under the mouse cursor (or the currently
   * focused node).
   * @param pre position of pre value in the sorted array of plotted nodes
   * @return nodes
   */
  private int[] overlappingNodes(final int pre) {
    final IntList il = new IntList();
    // get coordinates for focused item
    final int mx = calcCoordinate(true, plotData.xAxis.co[pre]);
    final int my = calcCoordinate(false, plotData.yAxis.co[pre]);
    for(int i = 0; i < plotData.pres.length; ++i) {
      // get coordinates for current item
      final int x = calcCoordinate(true, plotData.xAxis.co[i]);
      final int y = calcCoordinate(false, plotData.yAxis.co[i]);
      if(mx == x && my == y) {
        il.add(plotData.pres[i]);
      }
    }
    return il.toArray();
  }

  /**
   * Returns a standardized size factor for painting the plot.
   * @return size value
   */
  private int sizeFactor() {
    return Math.max(2, fontSize << 1);
  }

  /**
   * Formats an axis caption.
   * @param drawX x/y axis flag
   * @param focused pre value of the focused node
   * @return formatted string
   */
  private String formatString(final boolean drawX, final int focused) {
    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;
    final byte[] val = axis.getValue(focused);
    if(val.length == 0) return "";
    return axis.type == StatsType.TEXT || axis.type == StatsType.CATEGORY ?
        string(val) : BaseXLayout.value(toDouble(val));
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating || gui.painting) return;
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(gui.updating || e.isShiftDown()) return;
    if(dragging) {
      // to avoid significant offset between coordinates of mouse click and the
      // start coordinates of the bounding box, mouseX and mouseY are determined
      // by mousePressed()
      mouseX = e.getX();
      mouseY = e.getY();
    }
    final int h = getHeight();
    final int w = getWidth();
    final int th = 14;
    final int lb = MARGIN[1] - th;
    final int rb = w - MARGIN[3] + th;
    final int tb = MARGIN[0] - th;
    final int bb = h - MARGIN[2] + th;
    // flag which indicates if mouse pointer is located on the plot inside
    final boolean inBox = mouseY > tb && mouseY < bb &&
      mouseX > lb && mouseX < rb;
    if(!dragging && !inBox) return;
    // first time method is called when mouse dragged
    if(!dragging) {
      dragging = true;
      selectionBox.x = mouseX;
      selectionBox.y = mouseY;
    }

    // keeps selection box on the plot inside. if mouse pointer is outside box
    // the corners of the selection box are set to the predefined values s.a.
    int x = mouseX;
    int y = mouseY;
    if(!inBox) {
      if(mouseX < lb) {
        if(mouseY > bb) {
          x = lb;
          y = bb;
        } else if(mouseY < tb) {
          x = lb;
          y = tb;
        } else {
          x = lb;
        }
      } else if(mouseX > rb) {
        if(mouseY > bb) {
          x = rb;
          y = bb;
        } else if(mouseY < tb) {
          x = rb;
          y = tb;
        } else {
          x = rb;
        }
      } else if(mouseY < tb) {
        y = tb;
      } else {
        y = bb;
      }
    }
    selectionBox.w = x - selectionBox.x;
    selectionBox.h = y - selectionBox.y;

    // searches for items located in the selection box
    final IntList il = new IntList();
    for(int i = 0; i < plotData.pres.length; ++i) {
      x = calcCoordinate(true, plotData.xAxis.co[i]);
      y = calcCoordinate(false, plotData.yAxis.co[i]);
      if(selectionBox.contains(x, y)) il.add(plotData.pres[i]);
    }

    gui.notify.mark(new Nodes(il.toArray(), gui.context.data()), this);
    nextContext = gui.context.marked;
    drawSubNodes = false;
    markingChanged = true;
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(gui.updating || gui.painting) return;
    dragging = false;
    repaint();
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(gui.updating || gui.painting) return;
    markingChanged = true;
    mouseX = e.getX();
    mouseY = e.getY();
    focus();

    // determine if a following context filter operation is possibly triggered
    // by popup menu
    final boolean r = !SwingUtilities.isLeftMouseButton(e);
    if(r) { rightClick = true; return; }
    // no item is focused. no nodes marked after mouse click
    if(gui.context.focused == -1) {
      gui.notify.mark(new Nodes(gui.context.data()), this);
      return;
    }

    // node marking if item focused. if more than one icon is in focus range
    // all of these are marked. focus range means exact same x AND y coordinate.
    final int pre = plotData.findPre(gui.context.focused);
    final int[] il = overlappingNodes(pre);
    // right mouse or shift down
    if(e.isShiftDown()) {
      final Nodes marked = gui.context.marked;
      marked.union(il);
      gui.notify.mark(marked, this);
      // double click
    } else if(e.getClickCount() == 2) {
      // context change also self implied, thus right click set to true
      rightClick = true;
      final Nodes marked = new Nodes(gui.context.data());
      marked.union(il);
      gui.notify.context(marked, false, null);
      // simple mouse click
    } else {
      final Nodes marked = new Nodes(il, gui.context.data());
      gui.notify.mark(marked, this);
    }
    nextContext = gui.context.marked;
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    markingChanged = true;
    plotChanged = true;
    if(!gui.updating) repaint();
  }
}
