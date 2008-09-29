package org.basex.gui.view.scatter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * A scatter plot visualization of the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterView extends View implements Runnable {
  /** Data reference. */
  ScatterData scatterData;
  /** X paint margin. */
  private static final int XMARGIN = 150;
  /** Y paint margin. */ 
  private static final int YMARGIN = 200;
  /** Item size. */
  private static final int ITEMSIZE = 10;
  /** Focused item size. */
  private static final int ITEMSIZEFOCUSED = 18;
  /** Place holder for items which lack value. */
  private static final int NOVALUEBORDER = 60;
  /** Focus offset. */
  private static final int FOCUSOFFSET = 6;
  /** Whitespace between axis captions. */
  static final int CAPTIONWHITESPACE = 40;
  /** Item image. */
  private BufferedImage itemImg;
  /** Marked item image. */
  private BufferedImage markedItemImg;
  /** Focused item image. */
  private BufferedImage itemFocusedImg;
  /** Buffered plot image. */
  private BufferedImage plotImg;
  /** States if a plot value is focused. */ 
  private boolean valueFocused;
  /** Array position of focused item - not pre value!. */
  private int focusedItem;
  /** Currently focused x value. */
  private double focusedValueX;
  /** Currently focused y value. */
  private double focusedValueY;
  /** X coordinate of mouse pointer. */
  private int mouseX;
  /** Y coordinate of mouse pointer. */
  private int mouseY;
  /** Current view dimension. */
  private int viewDimension;
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
  /** Flag for mouse dragging actions. */
  private boolean dragging;
  /** Holds if context marking has changed. */
  boolean markingChanged;
  /** Holds pre values of marked nodes that are displayed in the plot to 
   * solve performance issues.
   */
  private int[] temporaryMarkedPos;
  /** Bounding box which supports selection of multiple items. */
  private ScatterBoundingBox selectionBox;

  /**
   * Default Constructor.
   * @param hlp help text
   */
  public ScatterView(final byte[] hlp) {
    super(hlp);
    setLayout(new BorderLayout());
    setBorder(5, 5, 5, 5);
    final Box box = new Box(BoxLayout.X_AXIS);
    xCombo = new BaseXCombo();
    xCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(scatterData.xAxis.setAxis((String) xCombo.getSelectedItem())) {
          plotChanged = true;
          repaint();
        }
      }
    });
    yCombo = new BaseXCombo();
    yCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(scatterData.yAxis.setAxis((String) yCombo.getSelectedItem())) {
          plotChanged = true;
          repaint();
        }
      }
    });
    itemCombo = new BaseXCombo();
    itemCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if(scatterData.setItem((String) itemCombo.getSelectedItem())) {
          plotChanged = true;
          markingChanged = true;
        }
          repaint();
      }
    });
    box.add(new JLabel("X"));
    box.add(Box.createHorizontalStrut(3));
    box.add(xCombo);
    box.add(Box.createHorizontalStrut(10));
    box.add(new JLabel("Y"));
    box.add(Box.createHorizontalStrut(3));
    box.add(yCombo);
    box.add(Box.createHorizontalStrut(10));
    box.add(new JLabel("Item"));
    box.add(Box.createHorizontalStrut(3));
    box.add(itemCombo);
    box.add(Box.createHorizontalGlue());
    add(box, BorderLayout.NORTH);
    
    temporaryMarkedPos = new int[0];
    selectionBox = new ScatterBoundingBox();
    itemImg = createItemImage(false, false);
    markedItemImg = createItemImage(false, true);
    itemFocusedImg = createItemImage(true, false);
  }
  
  /**
   * Creates a buffered image for items.
   * @param focusedImage create image of focused item if true
   * @param marked create image of marked item
   * @return item image
   */
  private BufferedImage createItemImage(final boolean focusedImage, 
      final boolean marked) {
    final BufferedImage img = focusedImage ? new BufferedImage(
        18, 18, Transparency.TRANSLUCENT) : new BufferedImage(
            10, 10, Transparency.TRANSLUCENT);
    final Graphics g = img.getGraphics();
    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    if(focusedImage) {
      g.setColor(Color.black);
      final int diff = (ITEMSIZEFOCUSED - ITEMSIZE) / 2;
      g.fillOval(diff, diff, ITEMSIZE, ITEMSIZE);
      g.setColor(new Color(180, 80, 80, 200));
      g.fillOval(0, 0, ITEMSIZEFOCUSED, ITEMSIZEFOCUSED);
    } else {
      g.setColor(new Color(50, 60, 130, 150));
      Color c = new Color(GUIConstants.colormark1.getRed(),
          GUIConstants.colormark1.getGreen(), GUIConstants.colormark1.getBlue(),
          150);
      if(marked)
//        g.setColor(GUIConstants.colormark1);
        g.setColor(c);
      g.fillOval(0, 0, ITEMSIZE, ITEMSIZE);
    }
    return img;
  }
  
  /**
   * Precalculates the plot and returns the result as buffered image.
   * @return buffered plot image
   */
  private BufferedImage createPlotImage() {
    final BufferedImage img = new BufferedImage(getWidth(), getHeight(), 
        Transparency.BITMASK);
    final Graphics g = img.getGraphics();
    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    if(scatterData.size == 0)
      return img;
    g.setColor(GUIConstants.color6);
//    final Nodes marked = GUI.context.marked();
    for(int i = 0; i < scatterData.size; i++) {
//      final int pre = scatterData.pres[i];
      drawItem(g, scatterData.xAxis.co[i], 
          scatterData.yAxis.co[i], false, false);
    }
    return img;
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    final Data data = GUI.context.data();
    if(data == null) return;
    
    if(scatterData == null) {
      refreshInit();
      return;
    }
    
    super.paintComponent(g);
    g.setColor(new Color(90, 90, 150, 90));
    
    final int w = getWidth();
    final int h = getHeight();
    if(w + h != viewDimension) {
      viewDimension = w + h;
      plotChanged = true;
    }

    // draw solid background
//    g.fillRect(0, 0, w, h);
    
    plotWidth = w - 2 * XMARGIN;
    plotHeight = h - 2 * YMARGIN;
    
    // overdraw plot background for non value items
    g.setColor(GUIConstants.color2);
    g.fillRect(XMARGIN + NOVALUEBORDER, YMARGIN, plotWidth - NOVALUEBORDER, 
      plotHeight - NOVALUEBORDER);
    
    // draw axis and grid
    drawXaxis(g);
    drawYaxis(g);

    // draw items
    if(scatterData.size == 0) return;
    if(plotImg == null || plotChanged)
      plotImg = createPlotImage();
    g.drawImage(plotImg, 0, 0, this);
    
    // draw marked items
    if(markingChanged) {
      Performance p = new Performance();
      final Nodes marked = GUI.context.marked();
      if(marked.size() > 0) {
        final IntList tmpPre = new IntList();
        for(int i = 0; i < marked.size(); i++) {
          final int prePos = scatterData.getPrePos(marked.pre[i]);
          if(prePos > -1)
            tmpPre.add(prePos);
        }
        temporaryMarkedPos = tmpPre.finish();
        System.out.println(p.getTimer());
      }
      markingChanged = false;
    }
    for(int i = 0; i < temporaryMarkedPos.length; i++) {
      drawItem(g, scatterData.xAxis.co[temporaryMarkedPos[i]], 
          scatterData.yAxis.co[temporaryMarkedPos[i]], false, true);
    }

    // draw focused item
    if(focusedItem > -1) {
      drawItem(g, scatterData.xAxis.co[focusedItem], 
          scatterData.yAxis.co[focusedItem], true, false);
    }
    
    // draw focused x and y value
    if(valueFocused) {
      g.setColor(GUIConstants.color6);
      final String x = mouseX > XMARGIN + NOVALUEBORDER ? 
          scatterData.xAxis.getValue(focusedValueX) : "";
      final String y = mouseY < h - YMARGIN - NOVALUEBORDER ?
          scatterData.yAxis.getValue(focusedValueY) : "";
      g.drawString("x  " + x, XMARGIN, YMARGIN - 50);
      g.drawString("y  " + y, XMARGIN, YMARGIN - 35);
    }
    
    // draw selection box
    if(dragging) {
      g.setColor(ScatterBoundingBox.back);
      final int selW = selectionBox.getWidth();
      final int selH = selectionBox.getHeight();
      final int x1 = selectionBox.x1;
      final int y1 = selectionBox.y1;
      final int x2 = selectionBox.x2;
      final int y2 = selectionBox.y2;
      g.fillRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH, 
          Math.abs(selW), Math.abs(selH));
      g.setColor(ScatterBoundingBox.frame);
      g.drawLine(x1, y1, x2, y1);
      g.drawLine(x1, y1, x1, y2);
      g.drawLine(x1, y2, x2, y2);
      g.drawLine(x2, y1, x2, y2);
    }
    
    plotChanged = false;
  }
  
  /**
   * Draws a plot item at the given position. If the item to be drawn is
   * focused the focused item buffered image is used.
   * @param g graphics reference
   * @param x x coordinate
   * @param y y coordinate
   * @param drawFocused a focused item is drawn
   * @param marked item is marked
   */
  private void drawItem(final Graphics g, final double x, final double y, 
      final boolean drawFocused, final boolean marked) {
    final int x1 = calcCoordinate(true, x);
    final int y1 = calcCoordinate(false, y);
    if(drawFocused) {
      g.drawImage(itemFocusedImg, x1 - ITEMSIZEFOCUSED / 2, 
          y1 - ITEMSIZEFOCUSED / 2, this); 
    } else {
      g.drawImage(!marked ? itemImg : markedItemImg, x1 - ITEMSIZE / 2, 
          y1 - ITEMSIZE / 2, this);
    }
  }
  
  /**
   * Draws the x axis of the plot.
   * @param g graphics reference
   */
  private void drawXaxis(final Graphics g) {
    final int h = getHeight();
    final int w = getWidth();
    g.setColor(GUIConstants.color1);
    g.drawLine(XMARGIN, h - YMARGIN, w - XMARGIN, 
        h - YMARGIN);
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);

    final int textH = g.getFontMetrics().getHeight();
    final int pWidth = plotWidth - NOVALUEBORDER;
    final ScatterAxis axis = scatterData.xAxis;
    axis.calcCaption(pWidth);
    final int nrCaptions = axis.nrCats != 1 ? axis.nrCaptions : 3;
    final double step = axis.captionStep;
    final double range = 1.0d / (nrCaptions - 1);
    final int type = axis.numType;
    final boolean numeric = axis.numeric;
    
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int x = XMARGIN + NOVALUEBORDER + ((int) ((i * range) * pWidth));
      g.drawLine(x, YMARGIN, x, h - YMARGIN + 9);
      String caption = "";
      if(numeric) {
        final double min = axis.min;
        final double captionValue = i == nrCaptions - 1 ? axis.max : 
          min + (i * step);
          
        if(type == ScatterAxis.TYPEINT)
          caption = Integer.toString((int) captionValue);
        else
          caption = "double dummy";
      } else {
        if(axis.nrCats == 1) {
          if(i == 1)
            caption = Token.string(axis.cats[0]);
        } else {
          caption = Token.string(axis.cats[i]);
        }
      }
      
      // draw rotated caption labels
      if(caption.length() > 22) {
        caption = caption.substring(0, 25);
        caption += "...";
      }

      int capW = BaseXLayout.width(g, caption);
      final int imgH = 160;
      int imgW = 160;
      final BufferedImage img = new BufferedImage(imgW, imgH, 
          Transparency.BITMASK);
      Graphics2D g2d = (Graphics2D) img.getGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
          RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setFont(GUIConstants.font);
      g2d.setColor(Color.black);
      g2d.rotate(Math.sin(30), imgW, 0 + textH);
      g2d.drawString(caption, imgW - capW, 0);
      g.drawImage(img, x - imgW + textH + 4, h - YMARGIN + 14, this);
    }
  }
  
  /**
   * Draws the y axis of the plot.
   * @param g graphics reference
   */
  private void drawYaxis(final Graphics g) {
    final int h = getHeight();
    final int w = getWidth();
    g.setColor(GUIConstants.color1);
    g.drawLine(XMARGIN, YMARGIN, XMARGIN, getHeight() - YMARGIN);
    
    scatterData.yAxis.calcCaption(plotHeight - NOVALUEBORDER);

    final boolean numeric = scatterData.yAxis.numeric;
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);
    final int textH = g.getFontMetrics().getHeight();
    final int pHeight = plotHeight - NOVALUEBORDER;
    final int nrCaptions = numeric ? (pHeight / 
        (textH + CAPTIONWHITESPACE)) :
      scatterData.yAxis.nrCaptions;
    final double range = 1.0d / (nrCaptions - 1);
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int y1 = h - YMARGIN - NOVALUEBORDER - 
      ((int) ((i * range) * pHeight));
      g.drawLine(XMARGIN - 9, y1, w - XMARGIN, y1);
      g.setColor(Color.black);
      String caption = null;
      if(numeric) {
        final double value = scatterData.yAxis.min +
          (scatterData.yAxis.max - scatterData.yAxis.min) * range * i;
        caption = Double.toString(value);
      } else {
        caption = Token.string(scatterData.yAxis.cats[i]);
      }
      final int capW = BaseXLayout.width(g, caption);
      g.drawString(caption, XMARGIN - capW - 15, y1 + textH / 2 - 1);
    }
  }
  
  /**
   * Returns a coordinate for a specific double value of an item.
   * @param d relative coordinate of specific item
   * @param xAxis calculated value is x value
   * @return absolute coordinate
   */
  private int calcCoordinate(final boolean xAxis, final double d) {
    if(xAxis) {
      if(d == -1)
        return XMARGIN + NOVALUEBORDER / 2;
      final int width = getWidth();
      final int xSpace = width - 2 * XMARGIN - NOVALUEBORDER;
      final int x = (int) (d * xSpace);
      return x + XMARGIN + NOVALUEBORDER;
    } else {
      final int height = getHeight();
      if(d == -1)
        return height - YMARGIN - NOVALUEBORDER / 2; 
      final int ySpace = height - 2 * YMARGIN - NOVALUEBORDER;
      final int y = ySpace - (int) (d * ySpace);
      return y + YMARGIN;
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  }

  @Override
  protected void refreshFocus() {
    final int s = scatterData.size;
    int i = 0;
    while(i < s) {
      if(scatterData.pres[i] == focused) {
        focusedItem = i;
        repaint();
        break;
      }
      i++;
    }
  }

  @Override
  protected void refreshInit() {
    scatterData = null;

    final Data data = GUI.context.data();
    if(data != null) {
      if(!GUIProp.showplot) return;
      
      viewDimension = Integer.MAX_VALUE;
      scatterData = new ScatterData();
      final String[] keys = scatterData.getStatKeys();
      xCombo.setModel(new DefaultComboBoxModel(keys));
      yCombo.setModel(new DefaultComboBoxModel(keys));
      final byte[][] tmpItems = data.tags.keys();
      final String[] items = new String[tmpItems.length];
      for(int i = 0; i < items.length; i++) {
        items[i] = Token.string(tmpItems[i]);
      }
      itemCombo.setModel(new DefaultComboBoxModel(items));
      if(items.length != 0) {
        itemCombo.setSelectedIndex(0);
      }
      if(keys.length != 0) {
        xCombo.setSelectedIndex(0);
        yCombo.setSelectedIndex(0);
      }
      focusedItem = -1;
      plotChanged = true;
      repaint();
    }
  }

  @Override
  protected void refreshLayout() {
    repaint();
  }

  @Override
  protected void refreshMark() {
    markingChanged = true;
    repaint();
  }

  @Override
  protected void refreshUpdate() {
  }

  /**
   * One day this might start the zoom animation thread.
   */
  public void run() {
  }
  
  /**
   * Calculates the relative value focused by the mouse pointer.
   * @param calcX value to be calculated is x value
   * @return focused x or y value
   */
  private double calcFocusedValue(final boolean calcX) {
    double relative = .0d;
    if(calcX) {
      final int pWidth = plotWidth - NOVALUEBORDER;
      final int xStart = XMARGIN + NOVALUEBORDER;
      relative = 1.0d / pWidth * (mouseX - xStart);
    } else {
      final int pHeight = plotHeight - NOVALUEBORDER;
      final int yStart = YMARGIN;
      relative = 1 - (1.0d / pHeight * (mouseY - yStart));
    }
    if(relative < 0)
      relative = .0d;
    return relative;
  }
  
  /**
   * Locates the nearest item to the mouse pointer. 
   * @return item focused
   */
  private boolean focus() {
    if(mouseX < XMARGIN || mouseX > getWidth() - XMARGIN + ITEMSIZE / 2 ||
        mouseY < YMARGIN - ITEMSIZE / 2 || mouseY > getHeight() - YMARGIN) {
      valueFocused = false;
      focusedItem = -1;
      return false;
    }
    
    // get values focused by mouse pointer
    valueFocused = true;
    focusedValueX = calcFocusedValue(true);
    focusedValueY = calcFocusedValue(false);
    
    // find focused item
    focusedItem = -1;
    int dist = Integer.MAX_VALUE;
    for(int i = 0; i < scatterData.size; i++) {
      final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
      final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
      final int distX = Math.abs(mouseX - x);
      final int distY = Math.abs(mouseY - y);
      if(distX <= FOCUSOFFSET && distY <= FOCUSOFFSET) {
        final int currDist = distX * distY;
        if(currDist < dist) {
          dist = currDist;
          focusedItem = i;
        }
      }
    }
    notifyFocus(focusedItem > -1 ? scatterData.pres[focusedItem] : -1, this);
    return true;
  }
  
  @Override
  public void mouseMoved(final MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
    focus();
    repaint();
  }
  
  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!dragging) {
      dragging = true;
      selectionBox.setStart(mouseX, mouseY);
    }
    mouseX = e.getX();
    mouseY = e.getY();
    selectionBox.setEnd(mouseX, mouseY);
    focus();
    repaint();
  }
  
  @Override
  public void mouseReleased(final MouseEvent e) {
    dragging = false;
    repaint();
  }
  
  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    mouseX = e.getX();
    mouseY = e.getY();

    focus();
    if(focused == -1) {
      Nodes n = new Nodes(GUI.context.data());
      notifyMark(n);
      return;
    }
    
    Nodes marked = GUI.context.marked();
    final int pre = focused;
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    if(!left) {
      // is right click
    } else if(e.isShiftDown()) {
      notifyMark(1);
    } else {
      if(marked.find(pre) < 0)
        notifyMark(0);
    }
  }
}
