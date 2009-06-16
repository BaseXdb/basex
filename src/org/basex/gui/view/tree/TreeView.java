package org.basex.gui.view.tree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;
import org.basex.query.ChildIterator;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller
 */
public final class TreeView extends View {
  /** Current font height. */
  private int fontHeight;
  /** the sum of all size values of a node line. */
  private int sumNodeSizeInLine = 0;
  /** the list of all parent nodes of a node line. */
  private IntList parentList = null;
  /** array with position of the parent node. */
  private HashMap<Integer, Double> parentPos = null;
  /** array of current rectangles. */
  private ArrayList<TreeRect> rects = null;
  /** nodes in current line. */
  private int nodeCount = 0;
  /** current mouse position x. */
  private int mousePosX = -1;
  /** current mouse position y. */
  private int mousePosY = -1;
  /** window width. */
  private int wwidth = -1;
  /** window height. */
  private int wheight = -1;
  /** current focused rect. */
  private TreeRect focusedRect = null;
  /** current Image of visualization. */
  private BufferedImage realImage = null;
  /** color for nodes. **/
  private int colorNode = 0xEDEFF7;
  /** color for rects. **/
  private int colorRect = 0xC9CFE7;
  /** color distance per level. **/
  private int colorDiff = 0x121008;
  /** color marked nodes. **/
  private final Color markColor = new Color(0x035FC7);
  /** color text-nodes. **/
  private final Color textColor = new Color(0x000F87);
  /** color highlighted nodes. **/
  private final Color highlightColor = new Color(0x5D6FB7);
  /** minimum space in rects needed for tags. **/
  private final int minSpace = 35;
  /** draw only element nodes. **/
  private boolean onlyElementNodes = false;
//  /** depth of the document. **/
//  private int documentDepth = -1;
  /** notified focus flag. **/ 
  boolean refreshedFocus = false;

  /**
   * Default Constructor.
   * @param man view manager
   */
  public TreeView(final ViewNotifier man) {
    super(man, null);
    new BaseXPopup(this, GUIConstants.POPUP);
  }

  @Override
  protected void refreshContext(final boolean more, final boolean quick) {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshFocus() {
    refreshedFocus = true;
    repaint();
  }

  @Override
  protected void refreshInit() {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshLayout() {
    repaint();
  }

  @Override
  protected void refreshMark() {
    wwidth = -1;
    wheight = -1;
    repaint();
  }

  @Override
  protected void refreshUpdate() {
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {

    final Data data = gui.context.data();
    /** Set the paint Component */
    super.paintComponent(g);
    BaseXLayout.antiAlias(g);
    g.setColor(Color.BLACK);
    g.setFont(GUIConstants.font);
//    documentDepth = data.meta.height;

    /** Timer */
    final Performance perf = new Performance();
    perf.initTimer();
    
    /** Initialize sizes */
    fontHeight = g.getFontMetrics().getHeight();

    if(windowSizeChanged()) {

      realImage = createImage();
      Graphics rg = realImage.getGraphics();
      rects = new ArrayList<TreeRect>();
      Nodes curr = gui.context.current();
      for(int i = 0; i < curr.size(); i++) {
        treeView(curr.nodes[i], rg, i);

      }
    }
    g.drawImage(realImage, 0, 0, getWidth(), getHeight(), this);

    // highlights the focused node
    if(focus()) {
      TreeRect r = focusedRect;
      g.setColor(markColor);
      g.drawRect(r.x, r.y, r.w, r.h);
      String s = "";
      int l = r.multiPres.length;

      for(int i = 0; i < l; i++) {

        if(s.length() > 0) s += " | ";

        int pre = r.multiPres[i];

        if(data.kind(pre) == Data.ELEM) {
          s += Token.string(data.tag(pre));
        } else {
          s += Token.string(data.text(pre));
        }

        for(int y = 0; y < data.attSize(pre, data.kind(pre)) - 1; y++) {
          s += " " + Token.string(data.attName(pre + y + 1)) + "=" + "\""
              + Token.string(data.attValue(pre + y + 1)) + "\" ";
        }

      }
      int w = BaseXLayout.width(g, s);
      g.setColor(highlightColor);
      g.fillRect(r.x - 1, r.y - fontHeight, w + 1, fontHeight);
      g.setColor(Color.WHITE);
      g.drawString(s, r.x + 1, (int) (r.y - fontHeight / 4f));
    }

    // highlights marked nodes
    if(!rects.isEmpty() && gui.context.marked().size() > 0) {

      Iterator<TreeRect> it = rects.iterator();

      while(it.hasNext()) {
        final TreeRect r = it.next();
        int size = gui.context.marked().size();
        final int[] markedNodes = new int[size];
        System.arraycopy(gui.context.marked().nodes, 0, markedNodes, 0, size);

        for(int j = 0; j < size; j++) {
          for(int z = 0; z < r.multiPres.length; z++) {
            if(r.multiPres[z] == markedNodes[j]) {

              g.setColor(Color.RED);

              g.fillRect(r.x + 1, r.y + 1, r.w - 1, r.h - 1);
              drawTextIntoRectangle(g, data.kind(r.pre), r.pre, r.x
                  + (int) (r.w / 2f), r.w, r.y);

              if(size < 2) {
                return;
              } else if(j < size - 1) {
                markedNodes[j] = markedNodes[size - 1];
              }
              size--;
              break;
            }
          }

        }
      }

    }

  }

  /**
   * creates a new translucent BufferedImage.
   * @return new translucent BufferedImage
   */
  private BufferedImage createImage() {
    return new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
        Transparency.TRANSLUCENT);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;
    super.mouseMoved(e);
    // refresh mouse focus
    mousePosX = e.getX();
    mousePosY = e.getY();
    focus();
    repaint();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {

    final boolean left = SwingUtilities.isLeftMouseButton(e);
    final boolean right = SwingUtilities.isRightMouseButton(e);
    if(!right && !left || focusedRect == null) return;

    if(left) {
      gui.notify.mark(0, null);
      if(e.getClickCount() > 1 && focusedRect.multiPres.length > 0) {
        gui.notify.context(gui.context.marked(), false, this);
        refreshContext(false, false);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(gui.updating || gui.focused == -1) return;
    if(e.getWheelRotation() > 0) gui.notify.context(new Nodes(gui.focused,
        gui.context.data()), false, null);
    else gui.notify.hist(false);
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  public boolean focus() {
    // int level = mousePosY / (fontHeight * 2);

    if(rects == null) return false;

    // final Data data = gui.context.data();
    final Iterator<TreeRect> it = rects.iterator();
    while(it.hasNext()) {
      final TreeRect r = it.next();

      if(refreshedFocus ? r.pre == gui.focused : 
        r.contains(mousePosX, mousePosY)) {
        focusedRect = r;

        for(int i = 0; i < r.multiPres.length; i++) {
          gui.notify.focus(r.multiPres[i], this);
        }
        refreshedFocus = false;
        return true;
      }
    }

    focusedRect = null;
    gui.notify.focus(-1, this);
    return false;
  }

  /**
   * controls the node temperature drawing.
   * @param root the root node
   * @param g the graphics reference
   * @param rootNum number of current root
   */
  private void treeView(final int root, final Graphics g, final int rootNum) {
    final Data data = gui.context.data();
    int level = 0;
    sumNodeSizeInLine = data.meta.size;
    parentList = new IntList();
    parentList.add(root);
    nodeCount = 1;
    parentPos = null;
    while(parentList.size > 0) {
      drawNodes(g, level, rootNum);
      getNextNodeLine();
      level++;
    }
  }

  /**
   * Saves node line in parentList.
   */
  private void getNextNodeLine() {
    final Data data = gui.context.data();
    final int l = parentList.size;
    final IntList temp = new IntList();
    int sumNodeSize = 0;
    int nCount = 0; // counts nodes

    for(int i = 0; i < l; i++) {
      final int p = parentList.list[i];

      if(p == -1) {
        continue;
      }

      final ChildIterator iterator = new ChildIterator(data, p);

      if(i > 0) {
        temp.add(-1);
      }

      while(iterator.more()) {
        final int pre = iterator.next();

        if(!onlyElementNodes || data.kind(pre) == Data.ELEM) {
          temp.add(pre);
          ++nCount;
        }
        sumNodeSize += data.size(pre, data.kind(pre));
      }
    }
    nodeCount = nCount;
    parentList = temp;
    sumNodeSizeInLine = sumNodeSize;
  }

  /**
   * Draws node temperature per line.
   * @param g graphics reference
   * @param level the current level
   * @param rootNum number of current root
   */
  private void drawNodes(final Graphics g, final int level, final int rootNum) {

    final int h = fontHeight;
    final int numberOfRoots = gui.context.current().nodes.length;
    final Data data = gui.context.data();
    final int size = parentList.size;
    final HashMap<Integer, Double> temp = new HashMap<Integer, Double>();
    final int y = 1 * level * fontHeight * 2;
    int screenWidth = getSize().width - 1;
    final double width = screenWidth / numberOfRoots;
    double x = (int) (rootNum * width);
    double ratio = width / size;
    IntList preList = new IntList();
    int factor = 1;
    double currRatio = ratio;
    while(currRatio < 2) {
      currRatio *= ++factor;
    }

    for(int i = 0; i < size; i++) {

      double boxMiddle = x + ratio / 2f;
      double w = ratio;

      int pre = parentList.list[i];

      if(pre == -1) {
        x += ratio;
        continue;
      }

      int nodeKind = data.kind(pre);
      int nodeSize = data.size(pre, nodeKind);

      // int parent = data.parent(pre, nodeKind);
      if(nodeSize > 0) temp.put(pre, boxMiddle);

      preList.add(pre);
      //
      // while(ratio < 2) {
      //
      // int p = parentList.list[++i];
      //
      // if(p == -1) {
      // continue;
      // }
      //
      // int nK = data.kind(p);
      // int par = data.parent(p, nK);
      //
      // if(par != parent) {
      // --i;
      // break;
      // }
      //
      // int nS = data.size(p, nK);
      // if(nS > 0) temp.put(p, boxMiddle);
      // preList.add(p);
      // w += ratio;
      //
      // }

      w = Math.max(2, ratio);

      TreeRect rect = new TreeRect();
      rect.x = (int) x;
      rect.y = y;
      rect.w = (int) w;
      rect.h = h;
      rect.pre = pre;
      rect.multiPres = preList.finish();
      preList.reset();
      rects.add(rect);

      g.setColor(new Color(colorRect - ((level < 11 ? level : 11) * colorDiff)));
      g.drawRect((int) x + 2, y, (int) w - 2, h);

      if(parentPos != null) {
        final double parentMiddle = parentPos.get(data.parent(pre, nodeKind));

        g.setColor(new Color(colorRect));
        g.drawLine((int) boxMiddle, y - 1, (int) parentMiddle, y - fontHeight
            + 1);

        // final int line = Math.round(fontHeight / 4f);
        // g.drawLine((int) boxMiddle, y, (int) boxMiddle, y - line);
        // g.drawLine((int) boxMiddle, y - line,
        // (int) parentMiddle, y - line);
        // g.drawLine((int) parentMiddle, y - line, (int) parentMiddle, y
        // - fontHeight);
      }

      g.setColor(new Color(colorNode - ((level < 11 ? level : 11) * colorDiff)));
      g.fillRect((int) x + 1, y, (int) w - 1, h);

      drawTextIntoRectangle(g, nodeKind, pre, (int) boxMiddle, (int) w, y);

      x += Math.max(ratio, 1);
    }
    parentPos = temp;
  }

  /**
   * draws text into the rectangles.
   * @param g graphics reference
   * @param nodeKind the node kind.
   * @param pre the pre value.
   * @param boxMiddle the middle of the rectangle.
   * @param w the width of the.
   * @param y the y value;
   */
  private void drawTextIntoRectangle(final Graphics g, final int nodeKind,
      final int pre, final int boxMiddle, final int w, final int y) {

    if(w < minSpace) return;

    final Data data = gui.context.data();
    String s = "";

    switch(nodeKind) {
      case Data.ELEM:
        s = Token.string(data.tag(pre));
        g.setColor(Color.BLACK);
        break;
      case Data.COMM:
        s = Token.string(data.text(pre));
        g.setColor(Color.GREEN);
        break;
      case Data.PI:
        s = Token.string(data.text(pre));
        g.setColor(Color.PINK);
        break;
      case Data.DOC:
        s = Token.string(data.text(pre));
        g.setColor(Color.BLACK);
        break;
      default:
        s = Token.string(data.text(pre));
        g.setColor(textColor);
    }

    Token.string(data.text(pre));
    int textWidth = 0;

    while((textWidth = BaseXLayout.width(g, s)) + 4 > w) {
      s = s.substring(0, s.length() / 2).concat("*");
    }
    g.drawString(s, boxMiddle - textWidth / 2, y + fontHeight - 2);
  }

  /**
   * Returns true if window-size has changed.
   * @return window-size has changed
   */
  boolean windowSizeChanged() {
    if((wwidth > -1 && wheight > -1)
        && (getHeight() == wheight && getWidth() == wwidth)) return false;

    wheight = getHeight();
    wwidth = getWidth();
    return true;
  }

}
