package org.basex.gui.view.real;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

import org.basex.core.proc.List;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.gui.view.View;
import org.basex.gui.view.table.TableData;
import org.basex.index.Names;
import org.basex.query.fs.DirIterator;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Map;
import org.basex.util.Performance;
import org.basex.util.Set;
import org.basex.util.Token;
import org.basex.util.TokenIterator;
import org.basex.util.TokenList;

/**
 * This class offers a real tree view. *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller, Philipp Ziemer
 */
public class RealView extends View { 
  /** Var to show the scrollbar. */
  private BaseXBar scroll = null;
  /** Horizontal Coords of the pointer. */
  private int pointerx;
  /** Vertical Coords of the pointer. */
  private int pointery;
  /** Current font hight. */
  private int fontHeight;
  /** Distance from the node to the border. */
  private final int margin = 3;
  /** Arc Meassures for the box. */
  private final int arcWH = 10;
  /** Vertical Distance between node levels. */
  private final int lvdistance = 30;
  /** Color for element node */
  private final Color elementColor = Color.BLACK;
  /** Color for attribute node */
  private final Color attributeColor = Color.RED;
  /** Color for text node */
  private final Color textColor = Color.BLUE;
  /** Color for comment node */
  private final Color commentColor = Color.GREEN;
  
  //
  //  /**
  //   * key par, value all pre values to given par.
  //   */
  //  HashMap<Integer, IntList> map = null;

  /**
   * Default Constructor.
   */
  public RealView() {
    super(null);
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshFocus() {
    repaint();
    // TODO Auto-generated method stub

  }

  @Override
  protected void refreshInit() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshLayout() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshMark() {
  // TODO Auto-generated method stub

  }

  @Override
  protected void refreshUpdate() {
  // TODO Auto-generated method stub

  }  

  @Override
  public void paintComponent(final Graphics g) {  
    /** Set the paint Component */
    super.paintComponent(g);    
    BaseXLayout.antiAlias(g);
    g.setColor(Color.BLACK);
    g.setFont(GUIConstants.font);

    /** Timer */
    Performance perf = new Performance();
    perf.initTimer();

    /** Initialise sizes */    
    fontHeight = g.getFontMetrics().getHeight();
    
    /** Initialise the pointer */
    pointerx = this.getWidth() / 2;
    pointery = 20;
    
    /** Paint the root */
    drawNode(g, 0, pointerx, pointery, elementColor);
    drawTree(g, 0, 0, 0, this.getWidth());

//    maxTextWidth = 0;
//    topBorder = 20;
//
//    int textWidth = BaseXLayout.width(g, Token.string(root));
//    
//    middleY = this.getHeight() / 2;
//    rightBorder = this.getWidth();


    System.out.println("Perf: " + perf.getTime());    
  }
  
  /**
   * Draws the node at the given Coordinates.
   * @param g graphics reference
   * @param pre pre value
   * @param x horizontal coordinate
   * @param y vertical coordinate
   * @param color node-color for different types
   */
  void drawNode(final Graphics g, final int pre, final int x, final int y, 
      final Color color) {    
    final Data data = GUI.context.data();
    String node = Token.string(data.tag(pre));
    int textWidth = BaseXLayout.width(g, node);
    g.setColor(color);
    /** Defines the start of the painting */
    int xstart = x - textWidth / 2;
    
    /** Draw String and Box */
    g.drawString(node, xstart, y);
    g.drawRoundRect(xstart - margin, y - fontHeight, textWidth + 2 * margin, 
        fontHeight + margin, arcWH, arcWH);
  }
  
  /**
   * Draws the Tree.
   * @param g graphics reference
   * @param root pre value of the current virtual root
   * @param lv current level
   * @param framex1 virtual width from parent
   * @param framex2 virtual width from parent
   */
  void drawTree(final Graphics g, final int root, final int lv, 
      final int framex1, final int framex2) {
    final Data data = GUI.context.data();
    DirIterator iterator = new DirIterator(data, root);
    
    int level = lv + 1;
    int nodetype = Data.ELEM;
    int rootsize = data.size(root, nodetype);
    int pre;
    int space = framex1;
    
    while(iterator.more()) {
      pre = iterator.next();
      if(data.kind(pre) == nodetype) {      
        /** Benötigten Platz berechnen */
        double percent = (double) (data.size(pre, nodetype) + 1) /
          (double) rootsize;
        int childframewidth = (int) ((framex2 - framex1) * percent);
        space += childframewidth;
        
        pointerx = space - childframewidth / 2;
        pointery = 20 + level * lvdistance;
        drawNode(g, pre, pointerx, pointery, elementColor);
        drawTree(g, pre, level, 0, space);
      }
    }
  }
  
  /**
   * Rounds the double.
   * @param value the double
   * @param decimalPlace how many places after ,
   * @return the rounded double
   */
  public static double round(final double value, final int decimalPlace) {
    double poweroften = 1;
    int decimal = decimalPlace;
    while (decimal-- > 0)
       poweroften *= 10.0;
    return Math.round(value * poweroften) / poweroften;
  }
}



