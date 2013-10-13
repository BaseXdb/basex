package org.basex.gui.view.folder;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.view.*;

/**
 * This view offers a folder visualization of the database contents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FolderView extends View {
  /** Horizontal offset. */
  private static final int OFFX = 8;

  /** References closed nodes. */
  boolean[] opened;
  /** Line Height. */
  int lineH;
  /** Focused folder position. */
  int focusedPos;

  /** Closed marker. */
  private BufferedImage closedMarker;
  /** Opened marker. */
  private BufferedImage openedMarker;

  /** Scroll Bar. */
  private final BaseXBar scroll;

  /** Vertical mouse position. */
  private int totalW;
  /** Start y value. */
  private int startY;
  /** Total height. */
  private int treeH;
  /** Box Size. */
  private int boxW;
  /** Box Margin. */
  private int boxMargin;

  /**
   * Default constructor.
   * @param man view manager
   */
  public FolderView(final ViewNotifier man) {
    super(FOLDERVIEW, man);
    createBoxes();
    layout(new BorderLayout());
    scroll = new BaseXBar(this);
    add(scroll, BorderLayout.EAST);
    new BaseXPopup(this, POPUP);
  }

  @Override
  public void refreshInit() {
    scroll.pos(0);

    if(gui.context.data() == null) {
      opened = null;
    } else if(visible()) {
      refreshOpenedNodes();
      refreshHeight();
      repaint();
    }
  }

  /**
   * Refreshes opened nodes.
   */
  private void refreshOpenedNodes() {
    final Data data = gui.context.data();
    opened = new boolean[data.meta.size];
    final int is = data.meta.size;
    for(int pre = 0; pre < is; ++pre) {
      opened[pre] = data.parent(pre, data.kind(pre)) <= 0;
    }
  }

  @Override
  public void refreshFocus() {
    repaint();
  }

  @Override
  public void refreshMark() {
    final int pre = gui.context.focused;
    if(pre == -1) return;

    // jump to the currently marked node
    final Data data = gui.context.data();
    final int par = data.parent(pre, data.kind(pre));
    // open node if it's not visible
    jumpTo(pre, par != -1 && !opened[par]);
    repaint();
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    startY = 0;
    scroll.pos(0);

    final Nodes curr = gui.context.current();
    if(more && curr.size() != 0) jumpTo(curr.pres[0], true);
    refreshHeight();
    repaint();
  }

  @Override
  public void refreshLayout() {
    createBoxes();
    if(opened == null) return;
    refreshOpenedNodes();
    refreshHeight();
    repaint();
  }

  @Override
  public void refreshUpdate() {
    if(opened == null) return;

    final Data data = gui.context.data();
    if(opened.length < data.meta.size)
      opened = Arrays.copyOf(opened, data.meta.size);

    startY = 0;
    scroll.pos(0);

    final Nodes marked = gui.context.marked;
    if(marked.size() != 0) jumpTo(marked.pres[0], true);
    refreshHeight();
    repaint();
  }

  @Override
  public boolean visible() {
    return gui.gopts.get(GUIOptions.SHOWFOLDER);
  }

  @Override
  public void visible(final boolean v) {
    gui.gopts.set(GUIOptions.SHOWFOLDER, v);
  }

  @Override
  protected boolean db() {
    return true;
  }

  /**
   * Refreshes tree height.
   */
  private void refreshHeight() {
    if(opened == null) return;

    treeH = new FolderIterator(this).height();
    scroll.height(treeH + 5);
  }

  @Override
  public void paintComponent(final Graphics g) {
    if(opened == null) {
      refreshInit();
      return;
    }
    super.paintComponent(g);
    if(opened == null) return;

    gui.painting = true;
    startY = -scroll.pos();
    totalW = getWidth() - (treeH > getHeight() ? scroll.getWidth() : 0);

    final FolderIterator it = new FolderIterator(this, startY + 5, getHeight());
    final Data data = gui.context.data();
    while(it.more()) {
      final int kind = data.kind(it.pre);
      final boolean elem = kind == Data.ELEM || kind == Data.DOC;
      final int x = OFFX + it.level * (lineH >> 1) + (elem ? lineH : boxW);
      drawString(g, it.pre, x, it.y + boxW);
    }
    gui.painting = false;
  }

  /**
   * Draws a string and checks mouse position.
   * @param g graphics reference
   * @param pre pre value
   * @param x horizontal coordinate
   * @param y vertical coordinate
   */
  private void drawString(final Graphics g, final int pre, final int x, final int y) {
    final Data data = gui.context.data();
    final Nodes marked = gui.context.marked;

    final int kind = data.kind(pre);
    final boolean elem = kind == Data.ELEM || kind == Data.DOC;

    Color col = Color.black;
    Font fnt = font;
    if(marked.find(pre) >= 0) {
      // mark node
      col = colormark3;
      fnt = bfont;
    }
    if(y < -lineH) return;

    g.setColor(color1);
    g.drawLine(2, y + boxMargin - 1, totalW - 5, y + boxMargin - 1);

    final byte[] name = ViewData.content(data, pre, false);

    int p = gui.context.focused;
    while(p > pre) p = ViewData.parent(data, p);
    if(pre == p) {
      g.setColor(color2);
      g.fillRect(0, y - boxW - boxMargin, totalW, lineH + 1);
    }

    final int fsz = fontSize;
    if(elem) {
      final int yy = y - boxW - (fsz > 20 ? 6 : 3);
      final Image marker = opened[pre] ? openedMarker : closedMarker;
      g.drawImage(marker, x - lineH, yy, this);
    }

    g.setFont(fnt);
    g.setColor(col);

    final int tw = totalW + 6;
    BaseXLayout.chopString(g, name, x, y - fsz, tw - x - 10, fsz);

    if(gui.context.focused == pre) {
      g.setColor(color4);
      g.drawRect(1, y - boxW - boxMargin, totalW - 3, lineH + 1);
      g.drawRect(2, y - boxW - boxMargin + 1, totalW - 5, lineH - 1);
    }
  }

  /**
   * Focuses the current pre value.
   * @param x x mouse position
   * @param y y mouse position
   * @return currently focused id
   */
  private boolean focus(final int x, final int y) {
    if(opened == null) return false;

    final int fsz = fontSize;
    final FolderIterator it = new FolderIterator(this, startY + 3, getHeight());
    final Data data = gui.context.data();
    while(it.more()) {
      if(y > it.y && y <= it.y + lineH) {
        Cursor c = CURSORARROW;
        final int kind = data.kind(it.pre);
        if(kind == Data.ELEM || kind == Data.DOC) {
          // set cursor when moving over tree boxes
          final int xx = OFFX + it.level * (lineH >> 1) + lineH - 6;
          if(x > xx - fsz && x < xx) c = CURSORHAND;
        }
        gui.cursor(c);
        gui.notify.focus(it.pre, this);
        repaint();
        return true;
      }
    }
    return false;
  }

  /**
   * Jumps to the specified pre value.
   * @param pre pre value to be found
   * @param open opened folder
   */
  private void jumpTo(final int pre, final boolean open) {
    if(getWidth() == 0 || !visible()) return;

    if(open) {
      int p = pre;
      while(p > 0) {
        opened[p] = true;
        p = ViewData.parent(gui.context.data(), p);
      }
      refreshHeight();
    }

    // find specified pre value
    final FolderIterator it = new FolderIterator(this);
    while(it.more() && pre != it.pre);

    // set new vertical position
    final int y = -it.y;
    final int h = getHeight();
    if(y > startY || y + h < startY + lineH) {
      startY = Math.min(0, Math.max(-treeH + h - 5, y + lineH));
      scroll.pos(-startY);
    }
  }

  /**
   * Creates click boxes.
   */
  private void createBoxes() {
    final int s = fontSize;
    boxMargin = s >> 2;
    lineH = s + boxMargin;
    boxW = s - boxMargin;
    final int sp = Math.max(1, s >> 4);

    /* Empty Box. */
    final BufferedImage emptyBox = new BufferedImage(boxW + 1, boxW + 1,
            BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = emptyBox.createGraphics();
    smooth(g);
    g.setColor(color4);
    g.fillOval((boxW >> 2) - 1, (boxW >> 2) + 1, boxW >> 1, boxW >> 1);
    g.setColor(color3);
    g.fillOval((boxW >> 2) - 2, boxW >> 2, boxW >> 1, boxW >> 1);

    openedMarker = new BufferedImage(boxW + 1, boxW + 1, BufferedImage.TYPE_INT_ARGB);
    g = openedMarker.createGraphics();
    smooth(g);

    Polygon p = new Polygon(new int[] { 0, boxW, boxW >> 1 }, new int[] {
        boxW - sp >> 1, boxW - sp >> 1, boxW }, 3);
    p.translate(0, -1);
    g.setColor(color4);
    g.fillPolygon(p);
    p.translate(-1, -1);
    g.setColor(color3);
    g.fillPolygon(p);

    closedMarker = new BufferedImage(boxW + 1, boxW + 1, BufferedImage.TYPE_INT_ARGB);
    g = closedMarker.createGraphics();
    smooth(g);

    p = new Polygon(new int[] { boxW - sp >> 1, boxW, boxW - sp >> 1 },
        new int[] { 0, boxW >> 1, boxW }, 3);
    p.translate(-1, 1);
    g.setColor(color4);
    g.fillPolygon(p);
    p.translate(-1, -1);
    g.setColor(color3);
    g.fillPolygon(p);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if(gui.updating) return;
    super.mouseMoved(e);
    // set new focus
    focus(e.getX(), e.getY());
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if(gui.updating || opened == null) return;
    super.mousePressed(e);

    if(!focus(e.getX(), e.getY())) return;

    // add or remove marked node
    final Nodes marked = gui.context.marked;
    if(e.getClickCount() == 2) {
      gui.notify.context(marked, false, null);
    } else if(e.isShiftDown()) {
      gui.notify.mark(1, null);
    } else if(sc(e) && SwingUtilities.isLeftMouseButton(e)) {
      gui.notify.mark(2, null);
    } else if(getCursor() != CURSORHAND) {
      if(!marked.contains(gui.context.focused)) gui.notify.mark(0, null);
    } else {
      // open/close entry
      opened[gui.context.focused] ^= true;
      refreshHeight();
      repaint();
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    final boolean left = SwingUtilities.isLeftMouseButton(e);
    if(!left || gui.updating || opened == null) return;
    super.mouseDragged(e);

    // marks currently focused node
    if(focus(e.getX(), e.getY())) gui.notify.mark(1, null);
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    if(gui.updating) return;
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    repaint();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    if(gui.updating || opened == null) return;
    super.keyPressed(e);

    int focus = focusedPos == -1 ? 0 : focusedPos;
    if(gui.context.focused == -1) gui.context.focused = 0;
    final int focusPre = gui.context.focused;
    final Data data = gui.context.data();
    final int kind = data.kind(focusPre);

    final boolean right = NEXT.is(e);
    boolean down = NEXTLINE.is(e);
    boolean up = PREVLINE.is(e);
    if(right || PREV.is(e)) {
      // open/close subtree
      if(e.isShiftDown()) {
        opened[focusPre] = right;
        final int s = data.meta.size;
        for(int pre = focusPre + 1;
          pre != s && data.parent(pre, data.kind(pre)) >= focusPre; pre++) {
          opened[pre] = right;
        }
        refreshHeight();
        repaint();
        return;
      }

      if(right ^ opened[focusPre] && (!ViewData.leaf(gui.gopts, data, focusPre)
          || data.attSize(focusPre, kind) > 1)) {
        opened[focusPre] = right;
        refreshHeight();
        repaint();
      } else if(right) {
        down = true;
      } else {
        up = true;
      }
    }

    if(down) {
      focus = Math.min(data.meta.size - 1, focus + 1);
    } else if(up) {
      focus = Math.max(0, focus - 1);
    } else if(NEXTPAGE.is(e)) {
      focus = Math.min(data.meta.size - 1, focus + getHeight() / lineH);
    } else if(PREVPAGE.is(e)) {
      focus = Math.max(0, focus - getHeight() / lineH);
    } else if(TEXTSTART.is(e)) {
      focus = 0;
    } else if(TEXTEND.is(e)) {
      focus = data.meta.size - 1;
    }
    if(focus == focusedPos) return;

    // calculate new tree position
    gui.context.focused = -1;
    final Nodes curr = gui.context.current();
    int pre = curr.pres[0];
    final FolderIterator it = new FolderIterator(this);
    while(it.more() && focus-- != 0) pre = it.pre;

    if(pre == curr.pres[0] && down) ++pre;
    gui.notify.focus(pre, this);
    jumpTo(pre, false);
    repaint();
  }
}
