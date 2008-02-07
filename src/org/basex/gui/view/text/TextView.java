package org.basex.gui.view.text;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import org.basex.BaseX;
import org.basex.data.Nodes;
import org.basex.data.PrintSerializer;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;
import org.basex.gui.layout.BaseXBar;
import org.basex.gui.view.View;
import org.basex.io.CachedOutput;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class offers a fast text view, using the {@link TextRenderer} class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TextView extends View implements Runnable {
  /** Reference the text of the panel. */
  private byte[] text = Token.EMPTY;
  /** Renderer reference. */
  private final TextRenderer rend;
  /** Scrollbar reference. */
  private final BaseXBar scroll;
  /** Current mouse position. */
  private Point mousePos;
  /** Current search term. */
  private byte[] find = Token.EMPTY;

  /**
   * Default constructor.
   * @param mode panel design
   * @param header text header
   * @param help help text
   */
  public TextView(final FILL mode, final String header,
      final byte[] help) {
    
    super(help);
    setMode(mode);
    setLayout(new BorderLayout());
    rend = new TextRenderer(header);
    scroll = new BaseXBar(this);
    scroll.setMode(mode);
    add(scroll, BorderLayout.EAST);
  }

  @Override
  public void refreshInit() {
    scroll.pos(0);
    refreshDoc(GUI.context.current());
  }

  @Override
  public void refreshFocus() {
    repaint();
  }

  @Override
  public void refreshMark() {
    final Nodes nodes = GUI.context.marked();
    refreshDoc(nodes);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) {
    refreshDoc(GUI.context.current());
  }

  /**
   * Refreshes the doc display.
   * @param nodes nodes to display
   */
  private void refreshDoc(final Nodes nodes) {
    find = Token.EMPTY;
    rend.find(find);
    if(!GUIProp.showtext || !rend.header.equals(GUIConstants.TEXTVIEW)) return;
    if(!GUI.context.db() || nodes.size == 0) {
      setText(Token.EMPTY);
      return;
    }
    
    try {
      final CachedOutput out = GUI.get().textcache;
      out.reset();
      final boolean chop = GUI.context.data().meta.chop;
      nodes.serialize(new PrintSerializer(out, false, chop));
      setText(out.finish(), false);
    } catch(final Exception ex) {
      BaseX.debug(ex);
    }
  }

  @Override
  public void refreshLayout() {
    rend.init(GUIConstants.mfont);
    find = Token.EMPTY;
    setText(text);
  }

  @Override
  public void refreshUpdate() {
    refreshContext(false, true);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);

    rend.write(g, scroll.pos());

    String t = null;
    if(mousePos != null) {
      t = RESULTCLIP;
    } else if(find.length != 0) {
      t = RESULTFIND + Token.string(find);
    }
    if(t != null) print(g, t, new Point(10, getHeight() - 10));
  }

  /**
   * Prints the specified text on the graphics panel.
   * @param g graphics reference
   * @param t text to be printed
   * @param p printing position
   */
  private void print(final Graphics g, final String t, final Point p) {
    g.setFont(new Font(GUIProp.font, 0, GUIProp.fontsize));
    final FontMetrics fm = g.getFontMetrics();
    final int w = fm.stringWidth(t);
    final int h = fm.getHeight();
    final int x = p.x;
    final int y = p.y;

    g.setColor(GUIConstants.color6);
    g.fillRect(x - 3, y - h + 1, w + 9, h + 3);
    g.setColor(Color.white);
    g.fillRect(x - 4, y - h, w + 8, h + 2);
    g.setColor(GUIConstants.color6);
    g.drawRect(x - 4, y - h, w + 8, h + 2);
    g.drawString(t, x, y - 2);
  }

  /**
   * Sets the output text.
   * @param t specified output text
   */
  public void setText(final byte[] t) {
    text = t;
    find = Token.EMPTY;
    rend.init(text);
    scroll.pos(0);
    SwingUtilities.invokeLater(this);
  }

  /**
   * Sets the output text.
   * @param t specified output text
   * @param inf info flag
   */
  public void setText(final byte[] t, final boolean inf) {
    rend.info(inf);
    setText(t);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    if(!SwingUtilities.isLeftMouseButton(e)) return;
    
    // selection mode
    rend.select(scroll.pos(), e.getPoint(), false);
    repaint();
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    copy(e.getPoint());
  }

  /**
   * Starts the text calculation or displays the clipboard copy information.
   */
  public void run() {
    if(mousePos != null) {
      repaint();
      Performance.sleep(1000);
      mousePos = null;
      // mark text
      rend.startW = -1;
      rend.endW = -1;
      repaint();
    } else {
      final Graphics g = getGraphics();
      if(g == null) return;
      scroll.height(rend.calc(g, getWidth() - scroll.getWidth(),
          getHeight()));
      repaint();
    }
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    rend.select(scroll.pos(), e.getPoint(), true);

    if(e.getY() > getHeight() - 20) {
      scroll.pos(scroll.pos() + 20 - (getHeight() - e.getY()));
    } else if(e.getY() < 20) {
      scroll.pos(scroll.pos() + e.getY() - 20);
    }
    scroll.repaint();
    repaint();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int oldpos = scroll.pos();
    byte[] fnd = find;
    int pos = oldpos;

    final int c = e.getKeyCode();
    final char ch = e.getKeyChar();
    if(e.isAltDown()) {
      super.keyPressed(e);
      return;
    }

    if(e.isControlDown()) {
      if(c == 'A') {
        // mark text
        rend.startW = 0;
        rend.endW = Integer.MAX_VALUE;
        repaint();
      } else if(c == 'C') {
        // copy text to clipboard
        if(rend.startW != rend.endW)
        copy(new Point(10, getHeight() - 10));
      }
    } else if(c == KeyEvent.VK_ENTER) {
      // remove search info
      fnd = Token.EMPTY;
    } else if(c == KeyEvent.VK_ESCAPE) {
      // reset incremental search
      super.keyPressed(e);
      fnd = Token.EMPTY;
    } else if(Token.letterOrDigit(ch)) {
      // extend search string
      fnd = Token.append(fnd, (byte) ch);
    } else if(c == KeyEvent.VK_BACK_SPACE && fnd.length != 0) {
      // delete last character from search string
      fnd = Token.substring(find, 0, find.length - 1);
    } else {
      super.keyPressed(e);
    }

    // move window
    if(c == KeyEvent.VK_DOWN)           pos += GUIProp.fontsize;
    else if(c == KeyEvent.VK_UP)        pos -= GUIProp.fontsize;
    else if(c == KeyEvent.VK_PAGE_DOWN) pos += getHeight();
    else if(c == KeyEvent.VK_PAGE_UP)   pos -= getHeight();
    else if(c == KeyEvent.VK_HOME)      pos = 0;
    else if(c == KeyEvent.VK_END)       pos = scroll.height();

    // refresh find
    if(fnd != find) {
      find = fnd;
      final int p = rend.find(find);
      final int sp = scroll.pos();
      if(p != -1 && p < sp || p > sp + getHeight()) scroll.pos(p - 40);
      repaint();
    }

    // refresh scroll position
    if(oldpos != pos) {
      scroll.pos(pos);
      repaint();
    }
  }

  /**
   * Copy the selected text to the clipboard.
   * @param p info string position
   */
  private void copy(final Point p) {
    // copy selection to clipboard
    final String txt = rend.copy(scroll.pos()).trim();
    if(txt.length() == 0) return;
    final Clipboard clip = getToolkit().getSystemClipboard();
    clip.setContents(new StringSelection(txt), null);
    mousePos = p;
    new Thread(this).start();
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    repaint();
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    rend.init(text);
    scroll.pos(0);
    SwingUtilities.invokeLater(this);
  }
}
