package org.basex.gui.view.scatter;

import static org.basex.Text.*;
import static org.basex.util.Token.*;

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
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.util.IntList;

/**
 * A scatter plot visualization of the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterView extends View implements Runnable {
  /** Rotate factor. */
  private static final double ROTATE = Math.sin(30);
  /** Plot margin: top, left, bottom, right margin. */
  private static final int[] MARGIN = new int[4];
  /** Data reference. */
  ScatterData scatterData;
  /** Item image. */
  private BufferedImage itemImg;
  /** Marked item image. */
  private BufferedImage markedItemImg;
  /** Focused item image. */
  private BufferedImage itemFocusedImg;
  /** Buffered plot image. */
  private BufferedImage plotImg;
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
          
          final String[] keys =
            scatterData.getCategories(token(item)).finishString();
          xCombo.setModel(new DefaultComboBoxModel(keys));
          yCombo.setModel(new DefaultComboBoxModel(keys));
          if(keys.length > 0) {
            xCombo.setSelectedIndex(0);
            yCombo.setSelectedIndex(keys.length > 1 ? 1 : 0);
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
    
    popup = new BaseXPopup(this, GUIConstants.POPUP);
    selectionBox = new ScatterBoundingBox();
    refreshLayout();
  }
  
  /**
   * Creates a buffered image for items.
   * @param focus create image of focused item if true
   * @param marked create image of marked item
   * @return item image
   */
  private BufferedImage createItemImage(final boolean focus, 
      final boolean marked) {

    final int size =  itemSize(focus);
    final BufferedImage img = new BufferedImage(size, size,
        Transparency.TRANSLUCENT);
    
    final Graphics g = img.getGraphics();
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
       RenderingHints.VALUE_ANTIALIAS_ON);
    
    Color c = GUIConstants.color;
    if(marked) c = GUIConstants.colormark;
    if(focus) c = GUIConstants.color6;

    g.setColor(c);
    g.fillOval(0, 0, size, size);
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
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    g.setColor(GUIConstants.color6);
    for(int i = 0; i < scatterData.size; i++) {
      drawItem(g, scatterData.xAxis.co[i], 
          scatterData.yAxis.co[i], false, false);
    }
    return img;
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    final Data data = GUI.context.data();
    if(data == null) return;

    super.paintComponent(g);
    BaseXLayout.antiAlias(g);
    
    if(scatterData == null) {
      refreshInit();
      return;
    }
    
    final int w = getWidth();
    final int h = getHeight();
    if(w + h != viewDimension) {
      viewDimension = w + h;
      plotChanged = true;
    }

    plotWidth = w - (MARGIN[1] + MARGIN[3]);
    plotHeight = h - (MARGIN[0] + MARGIN[2]);

    final int novalue = noValueSize();
    if(plotWidth - novalue < 0 || plotHeight - novalue < 0) {
      g.setFont(GUIConstants.font);
      g.setColor(Color.black);
      BaseXLayout.drawCenter(g, NOSPACE, getWidth(), (h + MARGIN[0]) / 2);
      return;
    }
    
    // overdraw plot background
    g.setColor(GUIConstants.back);
    g.fillRect(MARGIN[1] + novalue, MARGIN[0],
        plotWidth - novalue, plotHeight - novalue);
    
    // draw axis and grid
    drawAxis(g, true);
    drawAxis(g, false);

    // draw items
    if(scatterData.size == 0) return;
    if(plotImg == null || plotChanged) plotImg = createPlotImage();
    g.drawImage(plotImg, 0, 0, this);
    
    // draw marked items
    final Nodes marked = GUI.context.marked();
    if(marked.size() > 0) {
      for(int i = 0; i < marked.size(); i++) {
        final int prePos = scatterData.findPre(marked.nodes[i]);
        if(prePos > -1)
          drawItem(g, scatterData.xAxis.co[prePos], 
              scatterData.yAxis.co[prePos], false, true);
      }
    }

    // draw focused item
    final int f = scatterData.findPre(focused);
    if(f > -1) {
      if(!dragging) {
        final double x1 = scatterData.xAxis.co[f];
        final double y1 = scatterData.yAxis.co[f];
        drawItem(g, x1, y1, true, false);
        // draw focused x and y value
        g.setFont(GUIConstants.font);
        final String x = formatString(true);
        final String y = formatString(false);
        final String label = (x.length() > 16 ? x.substring(0, 16) : x) + " / " 
            + (y.length() > 15 ? y.substring(0, 15) : y);
        final int tw = BaseXLayout.width(g, label);
        final int th = g.getFontMetrics().getHeight();
        final int xx = Math.min(getWidth() - tw - 8, calcCoordinate(true, x1));
        g.setColor(GUIConstants.COLORS[10]);
        g.fillRect(xx - 1, calcCoordinate(false, y1) - th, tw + 4, th);
        g.setColor(GUIConstants.color1);
        g.drawString(label, xx, calcCoordinate(false, y1) - 4);
      }
    }
    
    // draw selection box
    if(dragging) {
      g.setColor(GUIConstants.back);
      final int selW = selectionBox.getWidth();
      final int selH = selectionBox.getHeight();
      final int x1 = selectionBox.x1;
      final int y1 = selectionBox.y1;
      g.fillRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH, 
          Math.abs(selW), Math.abs(selH));
      g.setColor(GUIConstants.frame);
      g.drawRect(selW > 0 ? x1 : x1 + selW, selH > 0 ? y1 : y1 + selH, 
          Math.abs(selW), Math.abs(selH));
    }
    plotChanged = false;
  }
  
  /**
   * Draws a plot item at the given position. If the item to be drawn is
   * focused the focused item buffered image is used.
   * @param g graphics reference
   * @param x x coordinate
   * @param y y coordinate
   * @param focus a focused item is drawn
   * @param marked item is marked
   */
  private void drawItem(final Graphics g, final double x, final double y, 
      final boolean focus, final boolean marked) {
    final int x1 = calcCoordinate(true, x);
    final int y1 = calcCoordinate(false, y);
    
    final BufferedImage img = focus ? itemFocusedImg : marked ?
        markedItemImg : itemImg;
    final int size =  img.getWidth() / 2;
    g.drawImage(img, x1 - size, y1 - size, this); 
  }
  
  /**
   * Draws the x axis of the plot.
   * @param g graphics reference
   * @param drawX drawn axis is x axis
   */
  private void drawAxis(final Graphics g, final boolean drawX) {
    final int h = getHeight();
    final int w = getWidth();
    g.setColor(GUIConstants.color1);
    
    final int novalue = noValueSize();
    final int textH = g.getFontMetrics().getHeight();
    // the painting space provided for items which lack no value
    final int pWidth = plotWidth - novalue;
    final int pHeight = plotHeight - novalue;
    
    final ScatterAxis axis = drawX ? scatterData.xAxis : scatterData.yAxis;
    // drawing horizontal axis line
    if(drawX) {
      g.drawLine(MARGIN[1], h - MARGIN[2], w - MARGIN[3], h - MARGIN[2]);
      if(plotChanged) axis.calcCaption(pWidth);
    } else {
      // drawing vertical axis line
      g.drawLine(MARGIN[1], MARGIN[0], MARGIN[1], getHeight() - MARGIN[2]);
      if(plotChanged) axis.calcCaption(pHeight);
    }
    
    final Kind type = axis.type;
    if(type == Kind.TEXT) return;
    final int nrCaptions = axis.nrCaptions;
    final double step = axis.captionStep;
    final double range = 1.0d / (nrCaptions - 1);
    final int fs = GUIProp.fontsize;
    
    g.setFont(GUIConstants.font);
    for(int i = 0; i < nrCaptions; i++) {
      String caption = "";
      if(type != Kind.CAT) {
        final double min = axis.min;
        final double captionValue = i == nrCaptions - 1 ? axis.max : 
          min + (i * step);

        caption = formatString(captionValue, drawX);

        // category data, each category is displayed as a caption 
      } else {
        caption = string(axis.cats[i]);
      }
      
      // if assignment consists of more than 10 characters the caption string
      // is chopped to the length 10 to avoid layout issues
      if(caption.length() > 10) caption = caption.substring(0, 10) + "...";

      // caption labels are rotated, for both x and y axis. first a buffered
      // image is created which displays the rotated label ...
      final int imgW = BaseXLayout.width(g, caption) + fs;
      final int imgH = 160;
      final BufferedImage img = new BufferedImage(imgW, imgH, 
          Transparency.BITMASK);
      Graphics2D g2d = img.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
          RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.rotate(ROTATE, imgW, 0 + textH);
      g2d.setFont(GUIConstants.font);
      g2d.setColor(Color.black);
      g2d.drawString(caption, fs, fs);

      // ... after that
      // the image is drawn beside x / y axis
      g.setColor(GUIConstants.color1);
      if(drawX) {
        final int x = MARGIN[1] + novalue + (int) (i * range * pWidth);
        final int y = h - MARGIN[2];
        g.drawImage(img, x - imgW + textH - fs, y + fs / 4, this);
        g.drawLine(x, MARGIN[0], x, y + fs / 2);
      } else {
        final int y = h - MARGIN[2] - novalue - (int) (i * range * pHeight);
        g.drawImage(img, MARGIN[1] - imgW - fs, y - fs, this);
        g.drawLine(MARGIN[1] - fs / 2, y, w - MARGIN[3], y);
      }
      
      //
//      }
      //
      
    }
  }

  /**
   * Returns a coordinate for a specific double value of an item.
   * @param d relative coordinate of specific item
   * @param xAxis calculated value is x value
   * @return absolute coordinate
   */
  private int calcCoordinate(final boolean xAxis, final double d) {
    final int novalue = noValueSize();
    if(xAxis) {
      // items with value -1 lack a value for the specific attribute
      if(d == -1) return MARGIN[1] + novalue / 2;
      final int width = getWidth();
      final int xSpace = width - (MARGIN[1] + MARGIN[3]) - novalue;
      final int x = (int) (d * xSpace);
      return x + MARGIN[1] + novalue;
    } else {
      final int height = getHeight();
      if(d == -1) return height - MARGIN[2] - novalue / 2; 
      final int ySpace = height - (MARGIN[0] + MARGIN[2]) - novalue;
      final int y = ySpace - (int) (d * ySpace);
      return y + MARGIN[0];
    }
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    if(!GUIProp.showplot) return;
    
    // all plot data is recalculated, assignments stay the same
    scatterData.refreshItems();
    scatterData.xAxis.refreshAxis();
    scatterData.yAxis.refreshAxis();

    plotChanged = true;
    repaint();
  }

  @Override
  protected void refreshFocus() {
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

      final String[] items = scatterData.getItems().finishString();
      itemCombo.setModel(new DefaultComboBoxModel(items));

      // set first item and trigger assignment of axis assignments
      if(items.length != 0) itemCombo.setSelectedIndex(0);

      plotChanged = true;
      repaint();
    }
  }

  @Override
  protected void refreshLayout() {
    itemImg = createItemImage(false, false);
    markedItemImg = createItemImage(false, true);
    itemFocusedImg = createItemImage(true, false);
    final int size = itemSize(false);
    MARGIN[0] = 32 + size;
    MARGIN[1] = size * 5;
    MARGIN[2] = size * 6;
    MARGIN[3] = size;
    plotChanged = true;
    repaint();
  }

  @Override
  protected void refreshMark() {
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
    int focusedPre = focused;
    // if mouse pointer is outside of the plot the focused item is set to -1,
    // focus may be refreshed, if necessary
    if(mouseX < MARGIN[1] || 
        mouseX > getWidth() - MARGIN[3] + size ||
        mouseY < MARGIN[0] - size || mouseY > getHeight() - MARGIN[2]) {
      // focused item already -1, no refresh needed
      if(focusedPre == -1) {
        return false;
      }
      notifyFocus(-1, this);
      return true;
    }
    
    // find focused item. 
    focusedPre = -1;
    int dist = Integer.MAX_VALUE;
    // all displayed items are tested for focus
    for(int i = 0; i < scatterData.size && dist != 0; i++) {
      // coordinates and distances for current tested item are calculated
      final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
      final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
      final int distX = Math.abs(mouseX - x);
      final int distY = Math.abs(mouseY - y);
      final int off = itemSize(false) / 2;
      // if x and y distances are smaller than offset value and the
      // x and y distances combined is smaller than the actual minimal
      // distance of any item tested so far, the current item is considered 
      // as a focus candidate
      if(distX < off && distY < off) {
        final int currDist = distX * distY;
        if(currDist < dist) {
          dist = currDist;
          focusedPre = scatterData.pres[i];
        }
      }
    }
    
    // if the focus changed, views are refreshed
    if(focusedPre != focused) {
      notifyFocus(focusedPre, this);
      return true;
    }
    return false;
  }
  
  /**
   * Returns the size of the place holder for items lacking values.
   * @return size value
   */
  static int noValueSize() {
    return itemSize(false) * 2;
  }
  
  /**
   * Returns the item size.
   * @param focus focus flag 
   * @return size value
   */
  static int itemSize(final boolean focus) {
    return GUIProp.fontsize + (focus ? 4 : 2);
  }
  
  /**
   * Formats an axis caption.
   * @param drawX x/y axis flag
   * @return formatted string
   */
  private String formatString(final boolean drawX) {
    final ScatterAxis axis = drawX ? scatterData.xAxis : scatterData.yAxis;
    final byte[] val = axis.getValue(focused);
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
    return BaseXLayout.value(value, attr.equals("@size"),
        attr.equals("@mtime"));
  }
  
  @Override
  public void mouseMoved(final MouseEvent e) {
    if(working) return;
    mouseX = e.getX();
    mouseY = e.getY();
    if(focus()) repaint();
  }
  
  @Override
  public void mouseDragged(final MouseEvent e) {
    if(working) return;
    if(dragging) {
      // to avoid significant offset between coordinates of mouse click and the
      // start coordinates of the bounding box, mouseX and mouseY are determined
      // by mousePressed()
      mouseX = e.getX();
      mouseY = e.getY();
    }
    final int h = getHeight();
    final int w = getWidth();
    final int th = 17;
    final int lb = MARGIN[1] - th;
    final int rb = w - MARGIN[3] + th;
    final int tb = MARGIN[0] - th;
    final int bb = h - MARGIN[2] + th;
    // flag which indicates if mouse pointer is located on the plot inside.
    boolean inBox = mouseY > tb && mouseY < bb && mouseX > lb && mouseX < rb;
    if(!dragging && !inBox)
      return;
    // first time method is called when mouse dragged
    if(!dragging) {
      dragging = true;
      selectionBox.setStart(mouseX, mouseY);
    }
    
    // keeps selection box on the plot inside. if mouse pointer is outside box
    // the corners of the selection box are set to the predefined values s.a. 
    if(!inBox) {
      if(mouseX < lb) {
        if(mouseY > bb) {
          selectionBox.setEnd(lb, bb);
        } else if(mouseY < tb) {
          selectionBox.setEnd(lb, tb);
        } else {
          selectionBox.setEnd(lb, mouseY);
        }
      } else if(mouseX > rb) {
        if(mouseY > bb) {
          selectionBox.setEnd(rb, bb);
        } else if(mouseY < tb) {
          selectionBox.setEnd(rb, tb);
        } else {
          selectionBox.setEnd(rb, mouseY);
        }
      } else if(mouseY < tb) {
        selectionBox.setEnd(mouseX, tb);
      } else
        selectionBox.setEnd(mouseX, bb);
      
      // mouse pointer position is in the plot
    } else {
      selectionBox.setEnd(mouseX, mouseY);
    }
    
    // searches for items located in the selection box
    final IntList il = new IntList();
    for(int i = 0; i < scatterData.size; i++) {
      final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
      final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
      if(((x >= selectionBox.x1 && x <= selectionBox.x2) || 
          (x <= selectionBox.x1 && x >= selectionBox.x2)) && 
          ((y >= selectionBox.y1 && y <= selectionBox.y2) || 
             (y <= selectionBox.y1 && y >= selectionBox.y2))) {
        il.add(scatterData.pres[i]);
      }
    }
    notifyMark(new Nodes(il.finish(), GUI.context.data()));
    
    repaint();
  }
  
  @Override
  public void mouseReleased(final MouseEvent e) {
    if(working) return;
    dragging = false;
    notifyFocus(-1, this);
    repaint();
  }
  
  @Override
  public void mousePressed(final MouseEvent e) {
    if(working) return;
    super.mousePressed(e);
    mouseX = e.getX();
    mouseY = e.getY();
    focus();
    // no item is focused. no nodes marked after mouse click
    if(focused == -1) {
      // a marking update is triggered with an empty node set as argument
      Nodes n = new Nodes(GUI.context.data());
      notifyMark(n);
      return;
    }
    
    // node marking if item focused. if more than one icon is in focus range
    // all of these are marked. focus range means exact same x AND y coordinate.
    final int pre = scatterData.findPre(focused);
    if(pre < 0) return;
    final IntList il = new IntList();
    // get coordinates for focused item
    final int mx = calcCoordinate(true, scatterData.xAxis.co[pre]);
    final int my = calcCoordinate(false, scatterData.yAxis.co[pre]);
    for(int i = 0; i < scatterData.size; i++) {
      // get coordinates for current item 
      final int x = calcCoordinate(true, scatterData.xAxis.co[i]);
      final int y = calcCoordinate(false, scatterData.yAxis.co[i]);
      if(mx == x && my == y) {
        il.add(scatterData.pres[i]);
      }
    }
    
    // marking operation is executed depending on mouse action
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    // right mouse or shift down
    if(e.isShiftDown() || !left) {
      final Nodes marked = GUI.context.marked();
      marked.union(il.finish());
      notifyMark(marked);
      // double click
    } else if(e.getClickCount() == 2) {
      final Nodes marked = new Nodes(GUI.context.data());
      marked.union(il.finish());
      notifyContext(marked, false);
      // simple mouse click
    } else {
      final Nodes marked = new Nodes(GUI.context.data());
      marked.union(il.finish());
      notifyMark(marked);
    }
  }
  
  
  //
  // testing
  //
//  public static void main(String[] args) {
//    final byte[][] t = { token("ab"), token("aa"), token(""), token("cab") }; 
////        token(112), token(86),  token(64),  token(325) };
//    IntList il = new IntList();
//    il = IntList.createOrder(t, false, true);
//    final int[] res = il.finish();
//    for(int i = 0; i < res.length; i++) {
//      System.out.println(res[i]);
//    }
//    System.out.println("\nog: ");
//    for(int i = 0; i < t.length; i++) {
//      System.out.println(string(t[i]));
//    }
//  }
}
