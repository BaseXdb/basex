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
import java.util.Arrays;

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
import org.basex.util.StringList;
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
  private static final int MARGINBOTTOM = 150;
  /** Y paint margin. */ 
  private static final int MARGINTOP = 65;
  /** Y paint margin. */ 
  private static final int MARGINLEFT = 170;
  /** Y paint margin. */ 
  private static final int MARGINRIGHT = 40;
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
  private IntList tmpMarkedPos;
//  private int[] temporaryMarkedPos;
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
        final String item = (String) itemCombo.getSelectedItem();
        if(scatterData.setItem(item)) {
          plotChanged = true;
          markingChanged = true;
          
          final StringList input = new StringList();
          input.add(item);
          final String[] keys = scatterData.getStatKeys(input);
          xCombo.setModel(new DefaultComboBoxModel(keys));
          yCombo.setModel(new DefaultComboBoxModel(keys));
          if(keys.length > 0) {
            xCombo.setSelectedIndex(0);
            yCombo.setSelectedIndex(0);
          }
        }
        repaint();
      }
    });
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("Item"));
    box.add(Box.createHorizontalStrut(3));
    box.add(itemCombo);
    box.add(Box.createHorizontalStrut(10));
    box.add(new JLabel("X"));
    box.add(Box.createHorizontalStrut(3));
    box.add(xCombo);
    box.add(Box.createHorizontalStrut(10));
    box.add(new JLabel("Y"));
    box.add(Box.createHorizontalStrut(3));
    box.add(yCombo);
    box.add(Box.createHorizontalStrut(32));
    add(box, BorderLayout.NORTH);
    
    tmpMarkedPos = new IntList();
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

    plotWidth = w - (MARGINLEFT + MARGINRIGHT);
    plotHeight = h - (MARGINTOP + MARGINBOTTOM);
    
    // overdraw plot background
//    g.setColor(GUIConstants.color2);
    g.setColor(new Color(90, 90, 90, 40));
    g.fillRect(MARGINLEFT + NOVALUEBORDER, MARGINTOP, 
        plotWidth - NOVALUEBORDER, 
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
      final Nodes marked = GUI.context.marked();
      if(marked.size() > 0) {
        tmpMarkedPos.reset();
        for(int i = 0; i < marked.size(); i++) {
          final int prePos = scatterData.getPrePos(marked.nodes[i]);
          if(prePos > -1)
            tmpMarkedPos.add(prePos);
        }
      }
      markingChanged = false;
    }
    final int[] ti = tmpMarkedPos.finish();
    for(int i = 0; i < ti.length; i++) {
      drawItem(g, scatterData.xAxis.co[ti[i]], 
          scatterData.yAxis.co[ti[i]], false, true);
    }

    // draw focused item
    if(focusedItem > -1) {
      drawItem(g, scatterData.xAxis.co[focusedItem], 
          scatterData.yAxis.co[focusedItem], true, false);
    }
    
    // draw focused x and y value
    if(valueFocused) {
      g.setColor(GUIConstants.color6);
      final String x = mouseX > MARGINLEFT + NOVALUEBORDER ? 
          scatterData.xAxis.getValue(focusedValueX) : "";
      final String y = mouseY < h - MARGINBOTTOM - NOVALUEBORDER ?
          scatterData.yAxis.getValue(focusedValueY) : "";
      g.drawString("x  " + x, MARGINLEFT, MARGINTOP - 20);
      g.drawString("y  " + y, MARGINLEFT, MARGINTOP - 6);
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
    g.drawLine(MARGINLEFT, h - MARGINBOTTOM, w - MARGINRIGHT, 
        h - MARGINBOTTOM);
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);

    final int textH = g.getFontMetrics().getHeight();
    final int pWidth = plotWidth - NOVALUEBORDER;
    final ScatterAxis axis = scatterData.xAxis;
    if(plotChanged)
      axis.calcCaption(pWidth);
    final int nrCaptions = axis.nrCats != 1 ? axis.nrCaptions : 3;
    final double step = axis.captionStep;
    final double range = 1.0d / (nrCaptions - 1);
    final int type = axis.numType;
    final boolean numeric = axis.numeric;
    
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int x = MARGINLEFT + NOVALUEBORDER + ((int) ((i * range) * pWidth));
      g.drawLine(x, MARGINTOP, x, h - MARGINBOTTOM + 9);
      String caption = "";
      if(numeric) {
        final double min = axis.min;
        final double captionValue = i == nrCaptions - 1 ? axis.max : 
          min + (i * step);
          
        if(type == ScatterAxis.TYPEINT)
          caption = Integer.toString((int) captionValue);
        else
          caption = Double.toString(captionValue);
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
        caption = caption.substring(0, 20);
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
      g.drawImage(img, x - imgW + textH + 4, h - MARGINBOTTOM + 14, this);
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
    g.drawLine(MARGINLEFT, MARGINTOP, MARGINLEFT, getHeight() - MARGINBOTTOM);
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);

    final int textH = g.getFontMetrics().getHeight();
    final int pHeight = plotHeight - NOVALUEBORDER;
    final ScatterAxis axis = scatterData.yAxis;
    if(plotChanged) {
      axis.calcCaption(pHeight);
    }
    final int nrCaptions = axis.nrCats != 1 ? axis.nrCaptions : 3;
    final double step = axis.captionStep;
    final double range = 1.0d / (nrCaptions - 1);
    final int type = axis.numType;
    final boolean numeric = axis.numeric;
    
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int y1 = h - MARGINBOTTOM - NOVALUEBORDER - 
      ((int) ((i * range) * pHeight));
      g.drawLine(MARGINLEFT - 9, y1, w - MARGINRIGHT, y1);
      g.setColor(Color.black);
      String caption = "";
      if(numeric) {
        final double min = axis.min;
        final double captionValue = i == nrCaptions - 1 ? axis.max : 
          min + (i * step);
        if(type == ScatterAxis.TYPEINT)
          caption = Integer.toString((int) captionValue);
        else
          caption = Double.toString(captionValue);
      } else {
        if(axis.nrCats == 1) {
          if(i == 1)
            caption = Token.string(axis.cats[0]);
        } else {
          caption = Token.string(axis.cats[i]);
        }
      }
      if(caption.length() > 22) {
        caption = caption.substring(0, 20);
        caption += "...";
      }
      final int capW = BaseXLayout.width(g, caption);
      g.drawString(caption, MARGINLEFT - capW - 15, y1 + textH / 2 - 1);
    }
    
//    final int h = getHeight();
//    final int w = getWidth();
//    g.setColor(GUIConstants.color1);
//    g.drawLine(MARGINLEFT, MARGINTOP, MARGINLEFT, getHeight() - MARGINBOTTOM);
//    
//    if(plotChanged)
//      scatterData.yAxis.calcCaption(plotHeight - NOVALUEBORDER);
//
//    final boolean numeric = scatterData.yAxis.numeric;
//    g.setFont(GUIConstants.font);
//    g.setColor(GUIConstants.color1);
//    final int textH = g.getFontMetrics().getHeight();
//    final int pHeight = plotHeight - NOVALUEBORDER;
//    final int nrCaptions = numeric ? (pHeight / 
//        (textH + CAPTIONWHITESPACE)) :
//      scatterData.yAxis.nrCaptions;
//    final double range = 1.0d / (nrCaptions - 1);
//    for(int i = 0; i < nrCaptions; i++) {
//      g.setColor(GUIConstants.color1);
//      final int y1 = h - MARGINBOTTOM - NOVALUEBORDER - 
//      ((int) ((i * range) * pHeight));
//      g.drawLine(MARGINLEFT - 9, y1, w - MARGINRIGHT, y1);
//      g.setColor(Color.black);
//      String caption = null;
//      if(numeric) {
//        final double value = scatterData.yAxis.min +
//          (scatterData.yAxis.max - scatterData.yAxis.min) * range * i;
//        caption = Double.toString(value);
//      } else {
//        caption = Token.string(scatterData.yAxis.cats[i]);
//      }
//      final int capW = BaseXLayout.width(g, caption);
//      g.drawString(caption, MARGINLEFT - capW - 15, y1 + textH / 2 - 1);
//    }
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
        return MARGINLEFT + NOVALUEBORDER / 2;
      final int width = getWidth();
      final int xSpace = width - (MARGINLEFT + MARGINRIGHT) - NOVALUEBORDER;
      final int x = (int) (d * xSpace);
      return x + MARGINLEFT + NOVALUEBORDER;
    } else {
      final int height = getHeight();
      if(d == -1)
        return height - MARGINBOTTOM - NOVALUEBORDER / 2; 
      final int ySpace = height - (MARGINTOP + MARGINBOTTOM) - NOVALUEBORDER;
      final int y = ySpace - (int) (d * ySpace);
      return y + MARGINTOP;
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  }

  @Override
  protected void refreshFocus() {
    final int s = scatterData.size;
    int i = 0;
    focusedItem = -1;
    while(i < s) {
      if(scatterData.pres[i] == focused) {
        focusedItem = i;
        break;
      }
      i++;
    }
    repaint();
  }

  @Override
  protected void refreshInit() {
    scatterData = null;

    final Data data = GUI.context.data();
    if(data != null) {
      if(!GUIProp.showplot) return;
      
      viewDimension = Integer.MAX_VALUE;
      scatterData = new ScatterData();
      
      final String[] keys = scatterData.getStatKeys(new StringList());
      xCombo.setModel(new DefaultComboBoxModel(keys));
      yCombo.setModel(new DefaultComboBoxModel(keys));

      final byte[][] tmpItems = data.tags.keys();
      final String[] items = new String[tmpItems.length];
      for(int i = 0; i < items.length; i++) {
        items[i] = Token.string(tmpItems[i]);
      }
      Arrays.sort(items);
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
      final int xStart = MARGINLEFT + NOVALUEBORDER;
      relative = 1.0d / pWidth * (mouseX - xStart);
    } else {
      final int pHeight = plotHeight - NOVALUEBORDER;
      final int yStart = MARGINTOP;
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
    if(mouseX < MARGINLEFT || 
        mouseX > getWidth() - MARGINRIGHT + ITEMSIZE / 2 ||
        mouseY < MARGINTOP - ITEMSIZE / 2 || mouseY > 
        getHeight() - MARGINBOTTOM) {
      valueFocused = false;
      focusedItem = -1;
      return false;
    }
    
    // get values focused by mouse pointer
    valueFocused = true;
    focusedValueX = calcFocusedValue(true);
    focusedValueY = calcFocusedValue(false);
    
    // find focused item
    if(!dragging) {
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
    } else {
      tmpMarkedPos.reset();
      for(int i = 0; i < scatterData.size; i++) {
        final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
        final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
        if(((x >= selectionBox.x1 && x <= selectionBox.x2) || 
            (x <= selectionBox.x1 && x >= selectionBox.x2)) && 
            ((y >= selectionBox.y1 && y <= selectionBox.y2) || 
                (y <= selectionBox.y1 && y >= selectionBox.y2))) {
          tmpMarkedPos.add(i);
        }
      }
      markingChanged = true;
    }
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
    final int[] tmp = new int[tmpMarkedPos.size];
    final int[] tmpPos = tmpMarkedPos.finish();
    for(int i = 0; i < tmp.length; i++) {
      tmp[i] = scatterData.pres[tmpPos[i]];
    }
    notifyMark(new Nodes(tmp, GUI.context.data()));
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
      tmpMarkedPos.reset();
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
        tmpMarkedPos.reset();
        notifyMark(0);
    }
  }
}
