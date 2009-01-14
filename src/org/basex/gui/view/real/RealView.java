package org.basex.gui.view.real;

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
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPopup;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewRect;
import org.basex.query.ChildIterator;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a real tree view.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Wolfgang Miller, Philipp Ziemer
 */
public final class RealView extends View {
  /** Current font height. */
  private int fontHeight;
  /** the sum of all size values of a node line. */
  private int sumNodeSizeInLine = 0;
  /** the list of all parent nodes of a node line. */
  private IntList parentList = null;
  /** array with position of the parent node. */
  private HashMap<Integer, Double> parentPos = null;
  /** array of current rectangles. */
  private ArrayList<ViewRect[]> rects = null;
  /** nodes in current line. */
  private int rectCount = 0;
  /** current mouse position x. */
  private int mousePosX = -1;
  /** current mouse position y. */
  private int mousePosY = -1;
  /** window width. */
  private int wwidth = -1;
  /** window height. */
  private int wheight = -1;
  /** current focused rect. */
  private ViewRect focusedRect = null;
  /** current Image of visualization. */
  private BufferedImage realImage = null;

  /**
   * Default Constructor.
   */
  public RealView() {
    super(null);
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

    final Data data = GUI.context.data();
    /** Set the paint Component */
    super.paintComponent(g);
    BaseXLayout.antiAlias(g);
    g.setColor(Color.BLACK);
    g.setFont(GUIConstants.font);

    /** Timer */
    final Performance perf = new Performance();
    perf.initTimer();

    /** Initialize sizes */
    fontHeight = g.getFontMetrics().getHeight();

    if(windowSizeChanged()) {

      realImage = createImage();
      Graphics rg = realImage.getGraphics();
      rects = new ArrayList<ViewRect[]>();
      Nodes curr = GUI.context.current();
      for(int i = 0; i < curr.size; i++) {
        temperature(curr.nodes[i], rg, i);

      }
      focus();
    }
    g.drawImage(realImage, 0, 0, getWidth(), getHeight(), this);

    // highlights focused node
    if(focusedRect != null) {
      ViewRect r = focusedRect;
      g.drawRect(r.x, r.y, r.w, r.h);

      final int pre = r.pre;
      String s = "";

      if(data.kind(pre) == Data.ELEM) {
        s = Token.string(data.tag(pre));
      } else {
        s = Token.string(data.text(pre));
      }

      for(int y = 0; y < data.attSize(pre, data.kind(pre)) - 1; y++) {
        s += " " + Token.string(data.attName(pre + y + 1)) + "=" + "\""
            + Token.string(data.attValue(pre + y + 1)) + "\" ";
      }

      int w = BaseXLayout.width(g, s);

      g.setColor(Color.WHITE);
      g.fillRect(r.x, r.y - fontHeight, w, fontHeight);
      g.setColor(Color.BLACK);
      g.drawString(s, r.x, (int) (r.y - fontHeight / 4f));

    }

    // highlights marked nodes
    if(!rects.isEmpty() && GUI.context.marked().size > 0) {

      g.setColor(Color.GREEN);
      Iterator<ViewRect[]> it = rects.iterator();

      while(it.hasNext()) {
        final ViewRect[] r = it.next();
        int size = GUI.context.marked().size;
        final int[] markedNodes = new int[size];
        System.arraycopy(GUI.context.marked().nodes, 0, markedNodes, 0, size);

        for(int i = 0; i < r.length; i++) {

          for(int j = 0; j < size; j++) {
            if(r[i].pre == markedNodes[j]) {

              g.drawRect(r[i].x, r[i].y, r[i].w, r[i].h);

              if(size < 2) {
                return;
              } else if(j < size - 1) {
                markedNodes[j] = markedNodes[size - 1];
              }
              size--;
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
    if(updating) return;
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
      View.notifyMark(0, this);
      int pre = focusedRect.pre;
      if(e.getClickCount() > 1 && pre > -1) {
        View.notifyContext(GUI.context.marked(), false, this);
        refreshContext(false, false);
      }
    }
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(updating || focused == -1) return;
    if(e.getWheelRotation() > 0) notifyContext(new Nodes(focused, 
        GUI.context.data()), false, null);
    else notifyHist(false);
  }

  /**
   * Finds rectangle at cursor position.
   * @return focused rectangle
   */
  public boolean focus() {
    // int level = mousePosY / (fontHeight * 2);

    if(rects == null) return false;

    // final Data data = GUI.context.data();
    final Iterator<ViewRect[]> it = rects.iterator();

    while(it.hasNext()) {
      final ViewRect[] r = it.next();

      for(int i = 0; i < r.length; i++) {
        if(r[i].contains(mousePosX, mousePosY)) {
          focusedRect = r[i];
          View.notifyFocus(r[i].pre, this);
          return true;
        }
      }

    }
    focusedRect = null;
    View.notifyFocus(-1, this);
    return false;
  }

  /**
   * controls the node temperature drawing.
   * @param root the root node
   * @param g the graphics reference
   * @param rootNum number of current root
   */
  private void temperature(final int root, final Graphics g, 
      final int rootNum) {
    final Data data = GUI.context.data();
    int level = 0;
    sumNodeSizeInLine = data.meta.size;
    parentList = new IntList();
    parentList.add(root);
    rectCount = 1;
    parentPos = null;
    while(parentList.size > 0) {
      drawTemperature(g, level, rootNum);
      getNextNodeLine();
      level++;
    }
  }

  /**
   * Saves node line in parentList.
   */
  private void getNextNodeLine() {
    final Data data = GUI.context.data();
    final int l = parentList.size;
    final IntList temp = new IntList();
    int sumNodeSize = 0;
    int rCount = 0; // counts nodes

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
        // if(data.kind(pre) == Data.ELEM) {
        temp.add(pre);
        ++rCount;
        // }
        sumNodeSize += data.size(pre, data.kind(pre));
      }
    }
    rectCount = rCount;
    parentList = temp;
    sumNodeSizeInLine = sumNodeSize;
  }

  /**
   * Draws node temperature per line.
   * @param g graphics reference
   * @param level the current level
   * @param rootNum number of current root
   */
  private void drawTemperature(final Graphics g, final int level,
      final int rootNum) {

    final int numberOfRoots = GUI.context.current().nodes.length;
    final ViewRect[] tRect = new ViewRect[rectCount];
    final Data data = GUI.context.data();
    final int size = parentList.size;
    final HashMap<Integer, Double> temp = new HashMap<Integer, Double>();
    final int y = 1 * level * fontHeight * 2;
    final double width = (getSize().width - 1) / numberOfRoots;
    double x = (int) (rootNum * width);
    final double ratio = width / size;
    final int minSpace = 35; // minimum Space for Tags
    final boolean space = ratio > minSpace ? true : false;

    int r = 0;

    for(int i = 0; i < size; i++) {
      final int pre = parentList.list[i];

      if(pre == -1) {
        x += ratio;
        continue;
      }
      int nodeKind = data.kind(pre);

      final int nodeSize = data.size(pre, nodeKind);

      final double nodePercent = nodeSize / (double) sumNodeSizeInLine;
      g.setColor(Color.black);

      final int l = Math.max(2, (int) ratio); // rectangle length
      final int h = fontHeight; // rectangle height

      // g.drawRect((int) x, y, l, h);
      tRect[r++] = new ViewRect((int) x, y, l, h, pre, 0);

      int c = (int) Math.rint(255 * nodePercent * 40);
      c = c > 255 ? 255 : c;
      g.setColor(new Color(c, 0, 255 - c));
      g.fillRect((int) x + 1, y + 1, l - 1, h - 1);

      final double boxMiddle = x + ratio / 2f;

      if(space) {
        String s = "";

        switch(nodeKind) {
          case Data.ELEM:
            s = Token.string(data.tag(pre));
            g.setColor(Color.WHITE);
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
            g.setColor(Color.BLUE);
            break;
          default:
            s = Token.string(data.text(pre));
            g.setColor(Color.YELLOW);
        }

        Token.string(data.text(pre));
        int textWidth = 0;

        while((textWidth = BaseXLayout.width(g, s)) + 4 > ratio) {
          s = s.substring(0, s.length() / 2).concat("?");
        }

        g.drawString(s, (int) boxMiddle - textWidth / 2, y + fontHeight - 2);
      }

      if(parentPos != null) {

        final double parentMiddle = parentPos.get(data.parent(pre, nodeKind));

        g.setColor(new Color(192 + c / 4, 192, 255 - c / 4));
        g.drawLine((int) boxMiddle, y - 1, (int) parentMiddle, y - fontHeight
            + 1);

        // final int line = Math.round(fontHeight / 4f);
        // g.drawLine((int) boxMiddle, y, (int) boxMiddle, y - line);
        // g.drawLine((int) boxMiddle, y - line,
        // (int) parentMiddle, y - line);
        // g.drawLine((int) parentMiddle, y - line, (int) parentMiddle, y
        // - fontHeight);

      }

      if(nodeSize > 0) temp.put(pre, boxMiddle);

      x += ratio;
    }
    rects.add(tRect);
    parentPos = temp;
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
