package org.basex.gui.view.real;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.view.View;
import org.basex.query.fs.DirIterator;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller, Philipp Ziemer
 */
public final class RealView extends View { 
  /** Var to show the scrollbar. */
  //private BaseXBar scroll = null;
  /** Horizontal Coords of the pointer. */
  private int pointerx;
  /** Vertical Coords of the pointer. */
  private int pointery;
  /** Current font height. */
  private int fontHeight;
  /** Distance from the node to the border. */
  private final int margin = 3;
  /** Arc Measures for the box. */
  //private final int arcWH = 10;
  /** Vertical Distance between node levels. */
  private final int lvdistance = 30;
  /** Distance of the tree from the top border. */
  private final int topdistance = 20;
  /** Color for element node. */
  private final Color elementColor = Color.BLACK;
  /** Color for attribute node. */
  private final Color attributeColor = Color.GREEN;
  /** Color for attribute node. */
  private final Color commentColor = Color.MAGENTA;
  /** Color for process instruction node. */
  private final Color piColor = Color.ORANGE;
  /** Color for text node. */
  private final Color textColor = Color.CYAN;
  /** multiplier for pre/post values. */
  private final int prePostMulti = 17;
  
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
    repaint();
  }

  @Override
  protected void refreshFocus() {
    repaint();
  }

  @Override
  protected void refreshInit() {
    repaint();
  }

  @Override
  protected void refreshLayout() {
    repaint();
  }

  @Override
  protected void refreshMark() {
    repaint();
  }

  @Override
  protected void refreshUpdate() {
    repaint();
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
    pointery = topdistance;
    
//  TODO: choose visualization here
    /** Paint the root */
//    drawNode(g, 0, pointerx, pointery, elementColor);
//    drawTree(g, 0, 0, 0, this.getWidth());
    
    drawPrePost(g, 1, 1);

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
    g.drawRect(xstart - margin, y - fontHeight, textWidth + 2 * margin, 
      fontHeight + margin);
//    g.drawRoundRect(xstart - margin, y - fontHeight, textWidth + 2 * margin, 
//        fontHeight + margin, arcWH, arcWH);
  }
  
  /**
   * Draws the Tree.
   * @param g graphics reference
   * @param root pre value of the current virtual root
   * @param lv current level
   * @param frameleft left border of the parent frame
   * @param space width of the parent frame
   */
  void drawTree(final Graphics g, final int root, final int lv, 
      final int frameleft, final int space) {
    final Data data = GUI.context.data();
    DirIterator iterator = new DirIterator(data, root);
    
    int level = lv + 1;
    int nodetype = Data.ELEM;
    int rootsize = data.size(root, nodetype);
    int pre;
    /** The x-value of the left frameboarder. */
    int border = frameleft;
    /** To engage inaccuracies during the meassure method, this meassures 
     * the size that is left after a child got it's space. */
    int sizeleft = space;    
    
    while(iterator.more()) {
      pre = iterator.next();      
      if(data.kind(pre) == nodetype) {
        /** Meassures the required space by the size value. */
        double percent = (double) (data.size(pre, nodetype) + 1) /
          (double) rootsize;
        int childframewidth = (int) (sizeleft * percent);   
        
        pointerx = border + childframewidth / 2;
        pointery = topdistance + level * lvdistance;
        drawNode(g, pre, pointerx, pointery, elementColor);
        /** System.out.println(Token.string(data.tag(pre))
         *  + "; Percent: " + percent + "; Framewidth: "
         *  + this.getWidth() + "; framex1: " + (space - childframewidth)
         *  + "; space: " + space); */
        drawTree(g, pre, level, border, childframewidth);
        
        border += childframewidth;
        rootsize = rootsize - data.size(pre, nodetype) + 1;
        sizeleft = sizeleft - childframewidth;
      }
    }
  }
  /**
   * calculates the post value of a node.
   * @param pre the pre value of the node
   * @param level the level of the node
   * @return calculated post value
   */
  private int calcPost(final int pre, final int level) {
    final Data data = GUI.context.data();
    return (data.size(pre, data.kind(pre)) - level) + pre;
  }

  /**
   * draws pre and post vlaues next to the node they belong to.
   * @param g graphics reference
   * @param pre the pre value
   * @param post the post value
   * @param textWidth the textWidth
   */
  void drawPreAndPostValues(final Graphics g, final int pre, final int post,
      final int textWidth) {
    int x = pre * prePostMulti;
    int y = post * prePostMulti;
    int preStrLen = BaseXLayout.width(g, Integer.toString(pre) + " ");
    int postStrLen = BaseXLayout.width(g, " ") + textWidth;
    Font temp = this.getFont();
    Font little = new Font(temp.getFontName(), Font.TRUETYPE_FONT,
        (int) (fontHeight / 1.5f));
    g.setFont(little);
    g.setColor(Color.BLUE);
    g.drawString(Integer.toString(pre), x - preStrLen, y + fontHeight / 4);
    g.setColor(Color.RED);
    g.drawString(Integer.toString(post), x + postStrLen, y + fontHeight / 4);
    g.setFont(temp);

  }

  /**
   * draws the nodes for given pre and post value.
   * @param g graphics reference
   * @param pre the pre value for the node to draw
   * @param post the par value for the node to draw
   * @param level the level for the node to draw
   */
  private void drawPrePostNode(final Graphics g, final int pre, final int post,
      final int level) {
    final Data data = GUI.context.data();
    int x = pre * prePostMulti;
    int y = post * prePostMulti;
    int preTextWidth = -1;
    int parTextWidth = -1;
    
    switch(data.kind(pre)) {
      case Data.ELEM:
        String s = Token.string(data.tag(pre));
        g.drawString(s, x, y);
        preTextWidth = BaseXLayout.width(g, s);
        break;
      case Data.ATTR:
        g.setColor(attributeColor);
        g.drawString("A", x, y);
        preTextWidth = BaseXLayout.width(g, "A");
        break;
      case Data.COMM:
        g.setColor(commentColor);
        g.drawString("C", x, y);
        preTextWidth = BaseXLayout.width(g, "C");
        break;
      case Data.PI:
        g.setColor(piColor);
        g.drawString("PI", x, y);
        preTextWidth = BaseXLayout.width(g, "PI");
        break;
      case Data.TEXT:
        g.setColor(textColor);
        g.drawString("T", x, y);
        preTextWidth = BaseXLayout.width(g, "T");
    }
    drawPreAndPostValues(g, pre, post, preTextWidth);
    g.setColor(Color.BLACK);

    int par = data.parent(pre, data.kind(pre));
    
    if(par > 0) {
      int parPost = calcPost(par, level - 1);
      
      switch(data.kind(par)) {
        case Data.ELEM:
          String s = Token.string(data.tag(pre));
          parTextWidth = BaseXLayout.width(g, s);
          break;
        case Data.ATTR:
          parTextWidth = BaseXLayout.width(g, "A");
          break;
        case Data.COMM:
          parTextWidth = BaseXLayout.width(g, "C");
          break;
        case Data.PI:
          parTextWidth = BaseXLayout.width(g, "PI");
          break;
        case Data.TEXT:
          parTextWidth = BaseXLayout.width(g, "T");
      }
      
      int parX = par * prePostMulti;
      int parY = parPost * prePostMulti;
//TODO: improve line connection between nodes.
      g.drawLine(parX, parY, x, y);
    }

  }

  /** 
   * draws pre-post-tree.
   * @param g graphics reference
   * @param pre the pre value of the current node
   * @param level the level of the current node
   */
  private void drawPrePost(final Graphics g, final int pre, final int level) {
    final Data data = GUI.context.data();
    int post = calcPost(pre, level);
        System.out.println("pre: " + pre + " | post: " + post + " | tag: "
            + Token.string(data.tag(pre)) + " | level: " + level);
    int lv = level;

    drawPrePostNode(g, pre, post, lv);

    DirIterator iterator = new DirIterator(data, pre);

    while(iterator.more()) {
      int newPre = iterator.next();

      drawPrePost(g, newPre, lv + 1);
    }

  }
 
}



