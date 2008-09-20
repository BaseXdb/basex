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

import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.util.Token;

/**
 * A scatter plot visualization of the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public class ScatterView extends View implements Runnable {
  /** Data reference. */
  ScatterData scatterData;
  /** X paint margin. */
  private static final int XMARGIN = 150;
  /** Y paint margin. */ 
  private static final int YMARGIN = 150;
  /** Item size. */
  private static final int ITEMSIZE = 10;
  /** Focused item size. */
  private static final int ITEMSIZEFOCUSED = 18;
  /** Place holder for items which lack value. */
  private static final int NOVALUEBORDER = 60;
  /** Focus offset. */
  private static final int FOCUSOFFSET = 6;
  /** Whitespace between axis captions. */
  private static final int CAPTIONWHITESPACE = 40;
  /** Item image. */
  private BufferedImage itemImg;
  /** Focused item image. */
  private BufferedImage itemFocusedImg;
  /** Buffered plot image. */
  private BufferedImage plotImg;
  /** Focused item. */
  private int focusedItem;
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
        if(scatterData.setItem((String) itemCombo.getSelectedItem()))
          plotChanged = true;
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
    
    itemImg = createItemImage(false);
    itemFocusedImg = createItemImage(true);
  }
  
  /**
   * Creates a buffered image for items.
   * @param focusedImage create image of focused item if true
   * @return item image
   */
  private BufferedImage createItemImage(final boolean focusedImage) {
    final BufferedImage img = focusedImage ? new BufferedImage(
        18, 18, Transparency.TRANSLUCENT) : new BufferedImage(
            10, 10, Transparency.TRANSLUCENT);
    final Graphics g = img.getGraphics();
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    if(focusedImage) {
      g.setColor(Color.black);
      final int diff = (ITEMSIZEFOCUSED - ITEMSIZE) / 2;
      g.fillOval(diff, diff, ITEMSIZE, ITEMSIZE);
      g.setColor(new Color(180, 80, 80, 200));
      g.fillOval(0, 0, ITEMSIZEFOCUSED, ITEMSIZEFOCUSED);
    } else {
//      g.setColor(GUIConstants.color6);
      g.setColor(new Color(50, 60, 130, 150));
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
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    if(scatterData.size == 0)
      return img;
    g.setColor(GUIConstants.color6);
    for(int i = 0; i < scatterData.size; i++) {
      drawItem(g, scatterData.xAxis.co[i], scatterData.yAxis.co[i], false);
    }
    return img;
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    g.setColor(new Color(90, 90, 150, 90));
    
    final int w = getWidth();
    final int h = getHeight();
    if(w + h != viewDimension) {
      viewDimension = w + h;
      plotChanged = true;
    }

    g.fillRect(0, 0, w, h);
    plotWidth = w - 2 * XMARGIN;
    plotHeight = h - 2 * YMARGIN;
    
//    overdraw plot background for non value items
    g.setColor(GUIConstants.color2);
    g.fillRect(XMARGIN + NOVALUEBORDER, YMARGIN, plotWidth - NOVALUEBORDER, 
      plotHeight - NOVALUEBORDER);
    
    //draw axis and grid
    drawXaxis(g);
    drawYaxis(g);
    
    // draw items
    if(scatterData.size == 0) return;
    if(plotImg == null || plotChanged)
      plotImg = createPlotImage();
    g.drawImage(plotImg, 0, 0, this);

    if(focusedItem != -1)
    drawItem(g, scatterData.xAxis.co[focusedItem], 
        scatterData.yAxis.co[focusedItem], true);
    plotChanged = false;
  }
  
  /**
   * Draws a plot item at the given position. If the item to be drawn is
   * focused the focused item buffered image is used.
   * @param g graphics reference
   * @param x x coordinate
   * @param y y coordinate
   * @param drawFocused a focused item is drawn
   */
  private void drawItem(final Graphics g, final double x, final double y, 
      final boolean drawFocused) {
    final int x1 = calcCoordinate(true, x);
    final int y1 = calcCoordinate(false, y);
    if(drawFocused) {
      // increased diameter of focused item  --->  -4
      g.drawImage(itemFocusedImg, x1 - ITEMSIZEFOCUSED / 2, 
          y1 - ITEMSIZEFOCUSED / 2, this); 
    } else {
      g.drawImage(itemImg, x1 - ITEMSIZE / 2, y1 - ITEMSIZE / 2, this);
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

    final boolean numeric = scatterData.xAxis.numeric;
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);
    final int textH = g.getFontMetrics().getHeight();
    final int pWidth = plotWidth - NOVALUEBORDER;
    final int nrCaptions = numeric ? 
        (pWidth / (textH * 2 + CAPTIONWHITESPACE)) :
      scatterData.xAxis.nrCaptions;
    final double range = 1.0d / (nrCaptions - 1);
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int x1 = XMARGIN + NOVALUEBORDER + ((int) ((i * range) * pWidth));
      g.drawLine(x1, YMARGIN, x1, h - YMARGIN + 9);
      g.setColor(Color.black);
      String caption = new String();
      if(numeric) {
        final double value = scatterData.xAxis.min +
          (scatterData.xAxis.max - scatterData.xAxis.min) * range * i;
        caption = Double.toString(value);
      } else {
        caption = Token.string(scatterData.xAxis.cats[i]);
      }
      final int capW = BaseXLayout.width(g, caption);
      g.drawString(caption, x1 - capW / 2, h - YMARGIN + 25);
      
      // try to draw rotated string
//      final int imgH = ((int) Math.pow(capW, 2));
//      final BufferedImage img = new BufferedImage(capW + 50, textH + 50, 
//          Transparency.TRANSLUCENT);
//      Graphics2D g2d = (Graphics2D) img.getGraphics();
//      g2d.setFont(GUIConstants.font);
//      g2d.setColor(Color.black);
//      g2d.drawString(caption, 0, 0);
//      AffineTransform at = new AffineTransform();
//      at.rotate(Math.PI / -1.41d);
//      g2d.transform(at);
//      g.drawImage(img, x1, h - YMARGIN + 25, this);
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

    final boolean numeric = scatterData.yAxis.numeric;
    g.setFont(GUIConstants.font);
    g.setColor(GUIConstants.color1);
    final int textH = g.getFontMetrics().getHeight();
    final int pHeight = plotHeight - NOVALUEBORDER;
    final int nrCaptions = numeric ? (pHeight / 
        (textH * 2 + CAPTIONWHITESPACE)) :
      scatterData.yAxis.nrCaptions;
    final double range = 1.0d / (nrCaptions - 1);
    for(int i = 0; i < nrCaptions; i++) {
      g.setColor(GUIConstants.color1);
      final int y1 = h - YMARGIN - NOVALUEBORDER - 
      ((int) ((i * range) * pHeight));
      g.drawLine(XMARGIN - 9, y1, w - XMARGIN, y1);
      g.setColor(Color.black);
      String caption = new String();
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
      itemCombo.setSelectedIndex(0);
      xCombo.setSelectedIndex(0);
      yCombo.setSelectedIndex(0);
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
   * Locates the nearest item to the mouse pointer. 
   * @return item focused
   */
  private boolean focus() {
    int dist = Integer.MAX_VALUE;
    int currFocusedItem = -1;
    if(mouseX < XMARGIN || mouseX > getWidth() - XMARGIN + ITEMSIZE / 2 ||
        mouseY < YMARGIN - ITEMSIZE / 2 || mouseY > getHeight() - YMARGIN) {
      focusedItem = -1;
      return true;
    }
    
    for(int i = 0; i < scatterData.size; i++) {
      final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
      final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
      final int distX = Math.abs(mouseX - x);
      final int distY = Math.abs(mouseY - y);
      if(distX <= FOCUSOFFSET && distY <= FOCUSOFFSET) {
        final int currDist = distX * distY;
        if(currDist < dist) {
          dist = currDist;
          currFocusedItem = i;
        }
      }
    }
    if(focusedItem != currFocusedItem) {
      focusedItem = currFocusedItem;
      if(focusedItem != -1)
        notifyFocus(scatterData.pres[focusedItem], this);
      return true;
    }
    return false;
  }
  
  @Override
  public void mouseMoved(final MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }
}
