package org.basex.gui.view.plot;

import static org.basex.Text.*;
import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.Nodes;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.gui.view.ViewRect;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * A scatter plot visualization of the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class PlotView extends View implements Runnable {
  /** Rotate factor. */
  private static final double ROTATE = Math.sin(30);
  /** Plot margin: top, left, bottom, right margin. */
  private static final int[] MARGIN = new int[4];
  /** Whitespace between captions. */
  static final int CAPTIONWHITESPACE = 10;
  /** Data reference. */
  PlotData plotData;
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
  /** Keeps track of changes in the plot. */
  boolean plotChanged;
  /** Current plot height. */
  private int plotHeight;
  /** Current plot width. */
  private int plotWidth;
  /** X axis selector. */
  BaseXCombo xCombo;
  /** Y axis selector. */
  BaseXCombo yCombo;
  /** Item selector combo. */
  BaseXCombo itemCombo;
  /** Logarithmic display. */
  BaseXCheckBox xLog;
  /** Logarithmic display. */
  BaseXCheckBox yLog;
  /** Dot size in plot view. */
  BaseXSlider dots;

  /** Flag for mouse dragging actions. */
  private boolean dragging;
  /** Indicates if global marked nodes should be drawn. */
  boolean drawSubNodes;
  /** Indicates if the buffered image for marked nodes has to be redrawn. */
  boolean markingChanged;
  /** Indicates if a filter operation is self implied or was trigger by
   * another view. */
  boolean rightClick;
  /** Context which is displayed in the plot after a context change which was
   * triggered by the plot itself. */
  private Nodes nextContext;
  /** Bounding box which supports selection of multiple items. */
  private final ViewRect selectionBox;

  /**
   * Default Constructor.
   * @param man view manager
   * @param hlp help text
   */
  public PlotView(final ViewNotifier man, final byte[] hlp) {
    super(man, hlp);
    setLayout(new BorderLayout());
    setBorder(5, 5, 5, 5);

    final BaseXBack panel = new BaseXBack(Fill.NONE);
    panel.setLayout(new BorderLayout());

    Box box = new Box(BoxLayout.X_AXIS);
    xLog = new BaseXCheckBox(PLOTLOG, null, false, null);
    xLog.setSelected(GUIProp.plotxlog);
    xLog.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        GUIProp.plotxlog ^= true;
        refreshUpdate();
      }
    });
    dots = new BaseXSlider(gui, new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        GUIProp.plotdots = dots.value();
        refreshLayout();
      }
    }, -10, 10, GUIProp.plotdots);
    BaseXLayout.setWidth(dots, 40);
    yLog = new BaseXCheckBox(PLOTLOG, null, false, null);
    yLog.setSelected(GUIProp.plotylog);
    yLog.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        GUIProp.plotylog ^= true;
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
    xCombo = new BaseXCombo();
    xCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(plotData.xAxis.setAxis((String) xCombo.getSelectedItem())) {
          plotChanged = true;
          markingChanged = true;
          repaint();
        }
      }
    });
    yCombo = new BaseXCombo();
    yCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(plotData.yAxis.setAxis((String) yCombo.getSelectedItem())) {
          plotChanged = true;
          markingChanged = true;
          repaint();
        }
      }
    });
    itemCombo = new BaseXCombo();
    itemCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String item = (String) itemCombo.getSelectedItem();
        if(plotData.setItem(item)) {
          plotChanged = true;
          markingChanged = true;

          final String[] keys =
            plotData.getCategories(token(item)).finishString();
          xCombo.setModel(new DefaultComboBoxModel(keys));
          yCombo.setModel(new DefaultComboBoxModel(keys));
          if(keys.length > 0) {
            // choose size category as default for vertical axis
            int y = 0;
            for(int k = 0; k < keys.length; k++) {
              if(keys[k].endsWith(DataText.S_SIZE)) {
                y = k;
                break;
              }
            }
            // choose name category as default for horizontal axis
            int x = y == 0 ? Math.min(1, keys.length) : 0;
            for(int k = 0; k < keys.length; k++) {
              if(keys[k].endsWith(DataText.S_NAME)) {
                x = k;
                break;
              }
            }
            xCombo.setSelectedIndex(x);
            yCombo.setSelectedIndex(y);
          }
        }
        drawSubNodes = true;
        markingChanged = true;
        repaint();
      }
    });
    box.add(yCombo);
    box.add(Box.createHorizontalStrut(3));
    box.add(new JLabel("Y"));
    box.add(Box.createHorizontalGlue());
    box.add(itemCombo);
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("X"));
    box.add(Box.createHorizontalStrut(3));
    box.add(xCombo);
    panel.add(box, BorderLayout.SOUTH);
    add(panel, BorderLayout.SOUTH);

    new BaseXPopup(this, POPUP);
    selectionBox = new ViewRect();
    refreshLayout();
  }

  /**
   * Creates a buffered image for items.
   * @param focus create image of focused item if true
   * @param marked create image of marked item
   * @param markedSub child node of marked node
   * @return item image
   */
  private BufferedImage itemImage(final boolean focus,
      final boolean marked, final boolean markedSub) {

    final int size = Math.max(1,
        GUIProp.fontsize + GUIProp.plotdots + (focus ? 2 :
          marked || markedSub ? 0 : -2));
    final BufferedImage img = new BufferedImage(size, size,
        Transparency.TRANSLUCENT);

    final Graphics g = img.getGraphics();
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
       RenderingHints.VALUE_ANTIALIAS_ON);

    Color c = color;
    if(marked) c = colormarkA;
    if(markedSub) c = colormark2A;
    if(focus) c = color6;

    g.setColor(c);
    g.fillOval(0, 0, size, size);
    return img;
  }

  /**
   * Precalculates the plot and returns the result as buffered image.
   */
  private void createPlotImage() {
    plotImg = new BufferedImage(getWidth(), getHeight(),
        Transparency.BITMASK);
    final Graphics g = plotImg.getGraphics();
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    // overdraw plot background
    g.setColor(color1);
    final int sz = sizeFactor();
    g.fillRect(MARGIN[1] + sz, MARGIN[0], plotWidth - sz,
        plotHeight - sz);

    // draw axis and grid
      drawAxis(g, true);
      drawAxis(g, false);

    // draw dark plot bounder
    g.setColor(color6);
    final int w = getWidth();
    final int h = getHeight();
    g.drawLine(MARGIN[1] + sz, MARGIN[0], w - MARGIN[3], MARGIN[0]);
    g.drawLine(MARGIN[1] + sz, h - MARGIN[2] - sz, w - MARGIN[3],
        h - MARGIN[2] - sz);
    g.drawLine(MARGIN[1] + sz, MARGIN[0], MARGIN[1] + sz,
        h - MARGIN[2] - sz);
    g.drawLine(w - MARGIN[3], MARGIN[0], w - MARGIN[3], h - MARGIN[2] - sz);

    // draw items
    g.setColor(color6);
    for(int i = 0; i < plotData.size; i++) {
      drawItem(g, plotData.xAxis.co[i],
          plotData.yAxis.co[i], false, false, false);
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Data data = gui.context.data();
    if(data == null) return;

    super.paintComponent(g);
    BaseXLayout.antiAlias(g);

    if(plotData == null) {
      refreshInit();
      return;
    }

    final int w = getWidth();
    final int h = getHeight();
    plotWidth = w - (MARGIN[1] + MARGIN[3]);
    plotHeight = h - (MARGIN[0] + MARGIN[2]);

    final int sz = sizeFactor();
    if(plotWidth - sz < 0 || plotHeight - sz < 0) {
      g.setFont(font);
      g.setColor(Color.black);
      BaseXLayout.drawCenter(g, NOSPACE, w, h / 2 - MARGIN[0]);
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
    // draw focused item
    final int f = plotData.findPre(gui.focused);
    if(f > -1) {
      // determine number of overlapping nodes (plotting second)
      final int ol = getOverlappingNodes(f).length;
      if(!dragging) {
        final double x1 = plotData.xAxis.co[f];
        final double y1 = plotData.yAxis.co[f];
        drawItem(g, x1, y1, true, false, false);
        // draw focused x and y value
        g.setFont(font);
        final int textH = g.getFontMetrics().getHeight();

        final String x = formatString(true);
        final String y = formatString(false);
        String label = x.length() > 16 ? x.substring(0, 14) + ".." : x;
        if(x.length() != 0 && y.length() != 0) label += " | ";
        label += y.length() > 16 ? y.substring(0, 14) + ".." : y;
        final int xa = calcCoordinate(true, x1) + 15;
        int ya = calcCoordinate(false, y1) + GUIProp.plotdots;
        final int ww = getWidth();

        final byte[] nm = data.attValue(data.nameID, gui.focused);
        String name = nm != null ? string(nm) : "";
        if(name.length() > 0 && plotData.xAxis.attrID != data.nameID &&
            plotData.yAxis.attrID != data.nameID) {

          if(ol > 1) name = ol + "x: " + name + ", ...";
          final int lw = BaseXLayout.width(g, label);
          if(ya < MARGIN[0] + textH && xa < w - lw) {
            ya += 2 * textH - GUIProp.plotdots;
          }
          if(xa > w - lw)
            BaseXLayout.drawTooltip(g, name + ": " + label, xa, ya, ww, 10);
          else {
            BaseXLayout.drawTooltip(g, name, xa, ya - textH, ww, 10);
            BaseXLayout.drawTooltip(g, label, xa, ya, ww, 10);
          }
        } else
          if(ol > 1) label = label.length() == 0 ? ol + "x" :
            ol + "x: " + label + ", ...";
          BaseXLayout.drawTooltip(g, label, xa, ya, ww, 10);
      }
    }

    // draw selection box
    if(dragging) {
      g.setColor(back);
      final int selW = selectionBox.w;
      final int selH = selectionBox.h;
      final int x1 = selectionBox.x;
      final int y1 = selectionBox.y;
      g.fillRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH,
          Math.abs(selW), Math.abs(selH));
      g.setColor(frame);
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
    markedImg = new BufferedImage(getWidth(), getHeight(),
        Transparency.BITMASK);
    final Graphics gi = markedImg.getGraphics();
    ((Graphics2D) gi).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    final Nodes marked = gui.context.marked();
    if(marked.size() <= 0) return;
    final int[] m = Array.finish(marked.nodes, marked.nodes.length);
    int i = 0;

    // no child nodes of the marked context nodes are marked
    if(!drawSubNodes) {
      while(i < m.length) {
        final int pi = plotData.findPre(m[i]);
        if(pi > -1) drawItem(gi, plotData.xAxis.co[pi],
            plotData.yAxis.co[pi], false, true, false);
        i++;
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
      drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k], false,
          true, false);
      k++;
    } else {
      k *= -1;
      k--;
    }

    // context change (triggered by another view).
    // descendants of marked node set are also checked for intersection
    // with currently plotted nodes
    while(i < m.length && k < p.length) {
      final int a = m[i];
      final int b = p[k];
      final int ns = data.size(a, data.kind(a)) - 1;
      if(a == b) {
        drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k], false,
            true, false);
        k++;
      } else if(a + ns >= b) {
        if(a < b) drawItem(gi, plotData.xAxis.co[k], plotData.yAxis.co[k],
            false, false, true);
        k++;
      } else {
        i++;
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
  private void drawItem(final Graphics g, final double x, final double y,
      final boolean focus, final boolean marked, final boolean sub) {
    final int x1 = calcCoordinate(true, x);
    final int y1 = calcCoordinate(false, y);

    final BufferedImage img = focus ? itemImgFocused : marked ?
        itemImgMarked : sub ? itemImgSub : itemImg;
    final int size =  img.getWidth() / 2;
    g.drawImage(img, x1 - size, y1 - size, this);
  }

  /**
   * Draws the x axis of the plot.
   * @param g graphics reference
   * @param drawX drawn axis is x axis
   */
  private void drawAxis(final Graphics g, final boolean drawX) {
//    Performance perf = new Performance();
    final int h = getHeight();
    final int w = getWidth();
    g.setColor(back);

    final int sz = sizeFactor();
    // the painting space provided for items which lack no value
    final int pWidth = plotWidth - sz;
    final int pHeight = plotHeight - sz;

    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;
    // drawing horizontal axis line
    if(drawX) {
      g.drawLine(MARGIN[1], h - MARGIN[2], w - MARGIN[3], h - MARGIN[2]);
      if(plotChanged) {
        axis.calcCaption(pWidth);
        final Kind kind = plotData.xAxis.type;
        xLog.setEnabled(kind == Kind.DBL || kind == Kind.INT);
      }
    } else {
      // drawing vertical axis line
      g.drawLine(MARGIN[1], MARGIN[0], MARGIN[1], getHeight() - MARGIN[2]);
      if(plotChanged) {
        axis.calcCaption(pHeight);
        final Kind kind = plotData.yAxis.type;
        yLog.setEnabled(kind == Kind.DBL || kind == Kind.INT);
      }
    }
    if(plotData.pres.length < 1) {
      drawCaptionAndGrid(g, drawX, "", 0);
      drawCaptionAndGrid(g, drawX, "", 1);
      return;
    }

    // getting some axis specific data
    final Kind type = axis.type;
    final int nrCaptions = axis.nrCaptions;
    final double step = axis.actlCaptionStep;
    final double capRange = 1.0d / (nrCaptions - 1);

    g.setFont(font);


    // draw axis and assignment for TEXT data
    if(type == Kind.TEXT) {
      final int nrCats = axis.nrCats;
      final double[] coSorted = Array.finish(axis.co, axis.co.length);
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
      while(i < cl && coSorted[i] == 0) i++;
      // find nearest position for next axis caption
      while(i < cl && op < 1.0d - 0.4d * capRange) {
        if(coSorted[i] > op) {
          final double distL = Math.abs(coSorted[i - 1] - op);
          final double distG = Math.abs(coSorted[i] - op);
          op = distL < distG ? coSorted[i - 1] : coSorted[i];

          int j = 0;
          // find value for given plot position
          while(j < axis.co.length && axis.co[j] != op) j++;
          drawCaptionAndGrid(g, drawX,
              string(axis.getValue(plotData.pres[j])), op);
          // increase to next optimum caption position
          op += capRange;
        }
        i++;
      }
      if(nrCats == 1) {
        op = .5d;
        int j = 0;
        // find value for given plot position
        while(j < axis.co.length && axis.co[j] != op) j++;
        drawCaptionAndGrid(g, drawX,
            string(axis.getValue(plotData.pres[j])), op);
      }


      // axis is drawn for numerical data, type INT/DBL
    } else {
      final boolean noRange = axis.max - axis.min == 0;
      // draw min and max grid line
      drawCaptionAndGrid(g, drawX, "", 0);
      drawCaptionAndGrid(g, drawX, "", 1);

      // return if insufficient plot space
      if(nrCaptions == 0) return;


      // if min equal max, draw min in plot middle
      if(noRange) {
        drawCaptionAndGrid(g, drawX, formatString(axis.min, drawX), .5d);
        return;
      }

      int c = 0;

      // draw logarithmic scale
      if(axis.log) {
        int l = (int) (Math.log10(Math.abs(axis.min)));
        l = l > 0 ? l : 1;
        double a = axis.min;
        double b = 0;
        final boolean neg = axis.min < 0;
        // draw labels <= 0
        if(neg) {
          b = (int) (-1 * Math.pow(10, l));
          while(adequateDistance(drawX, b, axis.max) && b > axis.min) {
            if(adequateDistance(drawX, a, b) && b < axis.max)
              // ------------------ insteps
              drawCaptionAndGrid(g, drawX,
                  formatString(b, drawX), axis.calcPosition(b));
            a = b;
            l--;
            if(l == 0) {
              b = 0;
              // ------------------ insteps ?????
              if(axis.max > 0 && adequateDistance(drawX, a, b))
                drawCaptionAndGrid(g, drawX,
                    formatString(b, drawX), axis.calcPosition(b));
              break;
            }
            b = (int) (-1 * Math.pow(10, l));
          }
        }

        // draw labels >= 0, logarithmic crap
        if(neg) {
          a = 0;
          l++;
        } else {
          a = axis.min;
          l = (int) (Math.log10(Math.abs(axis.min)));
          l = l > 0 ? l : 0;
        }
        b = (int) (Math.pow(10, l));
        b = b > 0 ? b : 0;
        while(adequateDistance(drawX, b, axis.max) && b < axis.max) {
          if(adequateDistance(drawX, a, b) && b > axis.min)
            // ------------------ insteps
            drawCaptionAndGrid(g, drawX, formatString(b, drawX),
                axis.calcPosition(b));
          a = b;
          l++;
          b = (int) (Math.pow(10, l));
        }

      // draw linear scale
      } else {
        // draw captions between min and max
        double d = axis.calcPosition(axis.startvalue);
        double f = axis.startvalue;
        while(d < 1.0d - .25d / nrCaptions) {
          c++;
          drawCaptionAndGrid(g, drawX,
              formatString(f, drawX), d);
          f = f + step;
          d = axis.calcPosition(f);
        }
        // draw min/max labels if little space available
        if(c < 2) {
          drawCaptionAndGrid(g, drawX, formatString(axis.min, drawX), 0.0);
          drawCaptionAndGrid(g, drawX, formatString(axis.max, drawX), 1.0);
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
  private boolean adequateDistance(final boolean drawX, final double a,
      final double b) {
    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;
    return Math.abs(calcCoordinate(drawX, axis.calcPosition(a)) -
    calcCoordinate(drawX, axis.calcPosition(b))) > sizeFactor() * 1.5;
  }

  /**
   * Draws an axis caption to the specified position.
   * @param g graphics reference
   * @param drawX draw caption on x axis
   * @param caption given caption string
   * @param d relative position in plot view depending on axis
   */
  private void drawCaptionAndGrid(final Graphics g, final boolean drawX,
      final String caption, final double d) {
    final int pos = calcCoordinate(drawX, d);
    final int h = getHeight();
    final int w = getWidth();
    final int textH = g.getFontMetrics().getHeight();
    final int fs = GUIProp.fontsize;
    String cap = caption;

    // if label is too long, it is is chopped to the first characters
    if(cap.length() > 10) cap = cap.substring(0, 9) + "..";

    // caption labels are rotated, for both x and y axis. first a buffered
    // image is created which displays the rotated label ...
    final int imgW = BaseXLayout.width(g, cap) + fs;
    final int imgH = 160;
    final BufferedImage img = new BufferedImage(imgW, imgH,
        Transparency.BITMASK);
    final Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.rotate(ROTATE, imgW, 0 + textH);
    g2d.setFont(font);
    g2d.setColor(Color.black);
    g2d.drawString(cap, fs, fs);

    // ... after that
    // the image and the grid line are drawn beside x / y axis
    g.setColor(back);
    if(drawX) {
      final int y = h - MARGIN[2];
      g.drawImage(img, pos - imgW + textH - fs, y, this);
      g.drawLine(pos, MARGIN[0], pos, y + fs / 2);
    } else {
      g.drawImage(img, MARGIN[1] - imgW - fs / 2, pos - fs, this);
      g.drawLine(MARGIN[1] - fs / 2, pos, w - MARGIN[3], pos);
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
    } else {
      final int height = getHeight();
      if(d == -1) return height - MARGIN[2] - sz / 4;
      final int ySpace = height - (MARGIN[0] + MARGIN[2]) - sz;
      return ySpace - (int) (d * ySpace) + MARGIN[0];
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    if(!GUIProp.showplot) return;

    // all plot data is recalculated, assignments stay the same
    plotData.refreshItems(nextContext != null && more && rightClick ?
        nextContext : gui.context.current(), !more || !rightClick);
    plotData.xAxis.log = GUIProp.plotxlog;
    plotData.xAxis.refreshAxis();
    plotData.yAxis.log = GUIProp.plotylog;
    plotData.yAxis.refreshAxis();

    nextContext = null;
    drawSubNodes = !rightClick;
    rightClick = false;
    plotChanged = true;
    markingChanged = true;
    repaint();
  }

  @Override
  protected void refreshFocus() {
    repaint();
  }

  @Override
  protected void refreshInit() {
    plotData = null;

    final Data data = gui.context.data();
    if(data != null) {
      if(!GUIProp.showplot) return;

      plotData = new PlotData(gui.context);

      final String[] items = plotData.getItems().finishString();
      itemCombo.setModel(new DefaultComboBoxModel(items));

      // set first item and trigger assignment of axis assignments
      if(items.length > 0) itemCombo.setSelectedIndex(0);

      drawSubNodes = true;
      markingChanged = true;
      plotChanged = true;
      repaint();
    }
  }

  @Override
  protected void refreshLayout() {
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
    repaint();
  }

  @Override
  protected void refreshMark() {
    drawSubNodes = true;
    markingChanged = true;
    repaint();
  }

  @Override
  protected void refreshUpdate() {
    refreshContext(false, true);
  }

  /**
   * One day this might start the zoom animation thread.
   */
  public void run() {
  }

  /**
   * Locates the nearest item to the mouse pointer.
   * @return item focused
   */
  private boolean focus() {
    final int size =  itemImg.getWidth() / 2;
    int focusedPre = gui.focused;
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
    for(int i = 0; i < plotData.size && dist != 0; i++) {
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
    if(focusedPre != gui.focused) {
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
  private int[] getOverlappingNodes(final int pre) {
    final IntList il = new IntList();
    // get coordinates for focused item
    final int mx = calcCoordinate(true, plotData.xAxis.co[pre]);
    final int my = calcCoordinate(false, plotData.yAxis.co[pre]);
    for(int i = 0; i < plotData.size; i++) {
      // get coordinates for current item
      final int x = calcCoordinate(true, plotData.xAxis.co[i]);
      final int y = calcCoordinate(false, plotData.yAxis.co[i]);
      if(mx == x && my == y) {
        il.add(plotData.pres[i]);
      }
    }
    return il.finish();
  }

  /**
   * Returns a standardized size factor for painting the plot.
   * @return size value
   */
  private static int sizeFactor() {
    return Math.max(2, GUIProp.fontsize * 2);
  }

  /**
   * Formats an axis caption.
   * @param drawX x/y axis flag
   * @return formatted string
   */
  private String formatString(final boolean drawX) {
    final PlotAxis axis = drawX ? plotData.xAxis : plotData.yAxis;
    final byte[] val = axis.getValue(gui.focused);
    if(val.length == 0) return "";
    return axis.type == Kind.TEXT || axis.type == Kind.CAT ? string(val) :
      formatString(toDouble(val), drawX);
  }

  /**
   * Formats a string.
   * @param value double value
   * @param drawX formatted string is x axis value
   * @return formatted string
   */
  private String formatString(final double value, final boolean drawX) {
    final String attr = (String) (drawX ? xCombo : yCombo).getSelectedItem();
    return BaseXLayout.value(value, attr.equals("@" + DataText.S_SIZE),
        attr.equals("@" + DataText.S_MTIME));
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
    // flag which indicates if mouse pointer is located on the plot inside.
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
    for(int i = 0; i < plotData.size; i++) {
      x = calcCoordinate(true, plotData.xAxis.co[i]);
      y = calcCoordinate(false, plotData.yAxis.co[i]);
      if(selectionBox.contains(x, y)) il.add(plotData.pres[i]);
    }

    gui.notify.mark(new Nodes(il.finish(), gui.context.data()), this);
    nextContext = gui.context.marked();
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
    final boolean r = SwingUtilities.isRightMouseButton(e);
    if(r) { rightClick = true; return; }
    // no item is focused. no nodes marked after mouse click
    if(gui.focused == -1) {
      gui.notify.mark(new Nodes(gui.context.data()), this);
      return;
    }

    // node marking if item focused. if more than one icon is in focus range
    // all of these are marked. focus range means exact same x AND y coordinate.
    final int pre = plotData.findPre(gui.focused);
    final int[] il = getOverlappingNodes(pre);
    // right mouse or shift down
    if(e.isShiftDown()) {
      final Nodes marked = gui.context.marked();
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
      final Nodes marked = new Nodes(gui.context.data());
      marked.union(il);
      gui.notify.mark(marked, this);
    }
    nextContext = gui.context.marked();
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    markingChanged = true;
    plotChanged = true;
    if(!gui.updating) repaint();
  }
}
