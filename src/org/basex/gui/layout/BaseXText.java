package org.basex.gui.layout;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.MatteBorder;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Fill;
import org.basex.util.Array;
import org.basex.util.Undo;
import static org.basex.util.Token.*;

/**
 * This class offers a fast text input, using the {@link BaseXTextRenderer}
 * class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class BaseXText extends BaseXPanel {
  /** Text array to be written. */
  protected BaseXTextTokens text = new BaseXTextTokens(EMPTY);
  /** Renderer reference. */
  protected final BaseXTextRenderer rend;
  /** Search field. */
  protected BaseXTextField find;
  /** Undo history. */
  protected Undo undo;

  /** Scrollbar reference. */
  protected final BaseXBar scroll;
  /** Popup Menu. */
  protected final BaseXPopup popup;

  /**
   * Default constructor.
   * @param help help text
   * @param edit editable flag
   * @param win parent window
   */
  public BaseXText(final byte[] help, final boolean edit, final Window win) {
    super(help, win);
    setFocusable(true);

    BaseXLayout.addInteraction(this, help, win);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addComponentListener(this);
    addMouseListener(this);
    addKeyListener(this);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        if(isEnabled()) cursor(true);
      }
      @Override
      public void focusLost(final FocusEvent e) {
        cursor(false);
        rend.cursor(false);
        rend.repaint();
      }
    });

    setLayout(new BorderLayout(4, 0));
    scroll = new BaseXBar(this);
    rend = new BaseXTextRenderer(text, scroll);

    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);

    if(edit) {
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 1, 1, GUIConstants.COLORS[6]));
      setMode(Fill.PLAIN);
      undo = new Undo();
    } else {
      setMode(Fill.NONE);
    }

    popup = new BaseXPopup(this, edit ?
      new GUICommand[] { new UndoCmd(), new RedoCmd(), null, new CutCmd(),
        new CopyCmd(), new PasteCmd(), new DelCmd(), null, new AllCmd() } :
      new GUICommand[] { new CopyCmd(), null, new AllCmd() });
  }

  /**
   * Sets the output text.
   * @param t output text
   */
  public void setText(final byte[] t) {
    setText(t, t.length);
  }

  /**
   * Adds a search dialog.
   * @param f search field
   */
  public void addSearch(final BaseXTextField f) {
    f.addSearch(this);
    find = f;
  }
  
  /**
   * Finds the specified term.
   * @param t output text
   * @param b backward browsing
   */
  public void find(final String t, final boolean b) {
    find(rend.find(t, b));
  }

  /**
   * Displays the search term.
   * @param y vertical position
   */
  public void find(final int y) {
    // updates the visible area
    final int p = scroll.pos();
    final int m = y + rend.fontH() * 3 - getHeight();
    if(y != 0 && (p < m || p > y)) scroll.pos(y - getHeight() / 2);
    repaint();
  }

  /**
   * Sets the output text.
   * @param t output text
   * @param s text size
   */
  public void setText(final byte[] t, final int s) {
    // remove 0x0Ds (carriage return) and compare old with new string
    int ns = 0;
    final int ts = text.size();
    final byte[] tt = text.text;
    boolean eq = true;
    for(int r = 0; r < s; r++) {
      if(t[r] != 0x0D) t[ns++] = t[r];
      eq &= ns < ts && ns < s && t[ns] == tt[ns];
    }
    eq &= ns == ts;

    // new text is different...
    if(!eq) {
      text = new BaseXTextTokens(t, ns);
      rend.setText(text);
      scroll.pos(0);
    }

    if(undo != null) undo.store(t.length != ns ? Array.finish(t, ns) : t, 0);
    SwingUtilities.invokeLater(calc);
  }

  /**
   * Sets a syntax highlighter.
   * @param s syntax reference
   */
  public void setSyntax(final BaseXSyntax s) {
    rend.setSyntax(s);
  }

  /**
   * Sets a new cursor position.
   * @param p cursor position
   */
  public void setCaret(final int p) {
    text.setCaret(p);
    showCursor(1);
    cursor(true);
  }

  /**
   * Returns the output text.
   * @return output text
   */
  public byte[] getText() {
    return text.finish();
  }

  @Override
  public void setFont(final Font f) {
    if(rend != null) {
      rend.setFont(f);
      rend.repaint();
    }
  }

  /**
   * Refreshes the syntax highlighting.
   * @param s start of optional error mark
   */
  public void error(final int s) {
    text.error(s);
    rend.repaint();
  }

  @Override
  public void setEnabled(final boolean e) {
    super.setEnabled(e);
    rend.setEnabled(e);
    scroll.setEnabled(e);
    cursor(e);
  }

  /**
   * Selects the whole text.
   */
  public void selectAll() {
    text.pos(0);
    text.startMark();
    text.pos(text.size());
    text.endMark();
    rend.repaint();
  }

  // MOUSE INTERACTIONS =======================================================

  @Override
  public void mouseEntered(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORTEXT);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;
    rend.stopSelect();
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    rend.select(scroll.pos(), e.getPoint(), true);

    final int y = Math.max(20, Math.min(e.getY(), getHeight() - 20));
    if(y != e.getY()) scroll.pos(scroll.pos() + e.getY() - y);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    super.mousePressed(e);
    if(!isEnabled()) return;

    requestFocusInWindow();
    cursor(true);

    if(!SwingUtilities.isLeftMouseButton(e)) return;

    final int c = e.getClickCount();
    if(c == 1) {
      // selection mode
      rend.select(scroll.pos(), e.getPoint(), false);
    } else if(c == 2) {
      selectWord();
    } else {
      selectLine();
    }
  }

  /**
   * Selects the word at the cursor position.
   */
  private void selectWord() {
    text.pos(text.cursor());
    final boolean ch = ftChar(text.prev(true));
    while(text.pos() > 0) {
      final int c = text.prev(true);
      if(c == '\n' || ch != ftChar(c)) break;
    }
    if(text.pos() != 0) text.next(true);
    text.startMark();
    while(text.pos() < text.size()) {
      final int c = text.curr();
      if(c == '\n' || ch != ftChar(c)) break;
      text.next(true);
    }
    text.endMark();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    text.pos(text.cursor());
    text.home(true);
    text.startMark();
    text.end(Integer.MAX_VALUE, true);
    text.endMark();
  }

  // KEY INTERACTIONS =======================================================

  /** Last horizontal position. */
  private int col = -1;

  @Override
  public void keyPressed(final KeyEvent e) {
    final int c = e.getKeyCode();
    final boolean shf = e.isShiftDown();
    final boolean ctrl = (Toolkit.getDefaultToolkit().
        getMenuShortcutKeyMask() & e.getModifiers()) != 0;

    if(ctrl && c == KeyEvent.VK_F) {
      if(find != null) find.requestFocusInWindow();
      return;
    }
    if(c == KeyEvent.VK_F3) {
      find(rend.find(shf, true));
      return;
    }

    if(e.isAltDown() || c == KeyEvent.VK_SHIFT || c == KeyEvent.VK_META ||
        c == KeyEvent.VK_CONTROL || c == KeyEvent.VK_ESCAPE) return;

    text.pos(text.cursor());
    cursor(true);

    final byte[] txt = text.text;

    boolean down = true;
    if(!ctrl && !e.isActionKey()) return;

    if(c != KeyEvent.VK_DOWN && c != KeyEvent.VK_UP) col = -1;

    if(ctrl && !shf) {
      if(c == 'A') {
        selectAll();
        text.setCaret();
        return;
      }
      if(c == 'C') {
        copy();
        return;
      }

      if(undo != null) {
        if(c == 'X') {
          cut();
        } else if(c == 'V') {
          paste();
        } else if(c == 'Z') {
          undo();
        } else if(c == 'Y') {
          redo();
        }
      }
    }

    if(shf && text.start() == -1) text.startMark();

    final int fh = rend.fontH();
    final int h = getHeight();

    if(c == KeyEvent.VK_RIGHT) {
      if(ctrl) {
        final boolean ch = ftChar(text.next(shf));
        while(text.pos() < text.size() && ch == ftChar(text.curr()))
          text.next(shf);
      } else {
        text.next(shf);
      }
    } else if(c == KeyEvent.VK_LEFT) {
      if(ctrl) {
        final boolean ch = ftChar(text.prev(shf));
        while(text.pos() > 0 && ch == ftChar(text.prev(shf)));
        if(text.pos() != 0) text.next(shf);
      } else {
        text.prev(shf);
      }
      down = false;
    } else if(c == KeyEvent.VK_DOWN) {
      if(ctrl) {
        scroll.pos(scroll.pos() + fh);
        return;
      }
      down(1, shf);
    } else if(c == KeyEvent.VK_UP) {
      if(ctrl) {
        scroll.pos(scroll.pos() - fh);
        return;
      }
      up(1, shf);
      down = false;
    } else {
      if(!shf) text.noMark();

      if(c == KeyEvent.VK_HOME) {
        if(ctrl) text.pos(0);
        else text.home(shf);
        down = false;
      } else if(c == KeyEvent.VK_END) {
        if(ctrl) text.pos(text.size());
        else text.end(Integer.MAX_VALUE, shf);
      } else if(c == KeyEvent.VK_PAGE_DOWN) {
        down(h / fh, shf);
      } else if(c == KeyEvent.VK_PAGE_UP) {
        up(h / fh, shf);
        down = false;
      }
    }

    // refresh scroll position
    if(shf) text.endMark();

    text.setCaret();
    if(txt != text.text) rend.calc(); // comparison by reference
    showCursor(down ? 2 : 0);
  }

  /**
   * Displays the currently edited text area.
   * @param align vertical alignment
   */
  public void showCursor(final int align) {
    // updates the visible area
    final int p = scroll.pos();
    final int y = rend.cursorY();
    final int m = y + rend.fontH() * 3 - getHeight();
    if(p < m || p > y) {
      scroll.pos(align == 0 ? y : align == 1 ? y - getHeight() / 2 : m);
      rend.repaint();
    }
  }

  /**
   * Moves the cursor down.
   * @param l number of lines to move cursor
   * @param shf shift flag
   */
  private void down(final int l, final boolean shf) {
    if(!shf) text.noMark();
    final int x = text.home(shf);
    if(col == -1) col = x;
    for(int i = 0; i < l; i++) {
      text.end(Integer.MAX_VALUE, shf);
      text.next(shf);
    }
    text.end(col, shf);
    if(text.pos() == text.size()) col = -1;
  }

  /**
   * Moves the cursor up.
   * @param l number of lines to move cursor
   * @param shf shift flag
   */
  private void up(final int l, final boolean shf) {
    if(!shf) text.noMark();
    final int x = text.home(shf);
    if(col == -1) col = x;
    if(text.pos() == 0) {
      col = -1;
      return;
    }
    for(int i = 0; i < l; i++) {
      text.prev(shf);
      text.home(shf);
    }
    text.end(col, shf);
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if(undo == null) return;

    // not nice here.. no alternative, though
    final char ch = e.getKeyChar();
    if(!Prop.MAC && e.isAltDown() || e.isActionKey() ||
      ch == KeyEvent.VK_ESCAPE) return;

    boolean down = true;
    final byte[] txt = text.text;
    text.pos(text.cursor());

    final boolean ctrl = (Toolkit.getDefaultToolkit().
        getMenuShortcutKeyMask() & e.getModifiers()) != 0;

    if(ch == KeyEvent.VK_BACK_SPACE) {
      if(text.start() == -1) {
        if(text.pos() == 0) return;
        text.prev();
      }
      final boolean ld = ftChar(text.curr());
      text.delete();
      if(ctrl) {
        while(text.pos() > 0 && ld == ftChar(text.prev())) text.delete();
        if(text.pos() != 0) text.next();
      }
      down = false;
    } else if(ch == KeyEvent.VK_DELETE) {
      if(text.start() == -1 && text.pos() == text.size()) return;
      final boolean ld = ftChar(text.curr());
      text.delete();
      while(ctrl && text.pos() < text.size() && ld == ftChar(text.curr()))
        text.delete();
    } else {
      if(ctrl) return;
      if(text.start() != -1) text.delete();
      text.add(new char[] { ch });
    }
    e.consume();
    text.setCaret();
    if(txt != text.text) rend.calc(); // comparison by reference
    showCursor(down ? 2 : 0);
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final int c = e.getKeyCode();
    if(e.isControlDown() && (c == KeyEvent.VK_Z || c == KeyEvent.VK_Y) ||
        undo == null) return;

    undo.store(text.finish(), text.cursor());
  }

  // EDITOR COMMANDS ==========================================================

  /**
   * Undoes the text.
   */
  protected void undo() {
    text = new BaseXTextTokens(undo.prev());
    rend.setText(text);
    text.pos(undo.cursor());
  }

  /**
   * Redoes the text.
   */
  protected void redo() {
    text = new BaseXTextTokens(undo.next());
    rend.setText(text);
    text.pos(undo.cursor());
  }

  /**
   * Cuts the selected text to the clipboard.
   */
  protected void cut() {
    text.pos(text.cursor());
    if(copy()) delete();
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  protected boolean copy() {
    final String txt = text.copy();
    if(txt.length() == 0) {
      text.noMark();
      return false;
    }

    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    clip.setContents(new StringSelection(txt), null);
    return true;
  }

  /**
   * Pastes the clipboard text.
   */
  protected void paste() {
    // copy selection to clipboard
    final String txt = clip();
    if(txt == null) return;
    text.pos(text.cursor());
    if(text.start() != -1) text.delete();
    text.add(txt.toCharArray());
    if(undo != null) undo.store(text.finish(), text.cursor());
    text.setCaret();
  }

  /**
   * Deletes the selected text.
   */
  protected void delete() {
    text.pos(text.cursor());
    text.delete();
    if(undo != null) undo.store(text.finish(), text.cursor());
    text.setCaret();
  }

  /**
   * Returns the clipboard text.
   * @return text
   */
  public String clip() {
    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable t = clip.getContents(null);
    if(t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      try {
        return (String) t.getTransferData(DataFlavor.stringFlavor);
      } catch(final Exception ex) {
        BaseX.debug(ex);
      }
    }
    return null;
  }

  /** Cursor. */
  final Timer cursor = new Timer(500, new ActionListener() {
    public void actionPerformed(final ActionEvent e) {
      rend.cursor(!rend.cursor());
      rend.repaint();
    }
  });

  /**
   * Handles the cursor thread; interrupts the old thread as soon as
   * new one has been started.
   * @param start start/stop flag
   */
  protected void cursor(final boolean start) {
    cursor.stop();
    if(start) cursor.start();
    rend.cursor(start);
    rend.repaint();
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    rend.repaint();
  }

  @Override
  public void componentResized(final ComponentEvent e) {
    scroll.pos(0);
    SwingUtilities.invokeLater(calc);
  }

  /** Calculation counter. */
  final Thread calc = new Thread() {
    @Override
    public void run() {
      rend.calc();
      rend.repaint();
    }
  };

  /** Text Command. */
  abstract class TextCmd implements GUICommand {
    public abstract void execute(final GUI main);
    public abstract void refresh(final GUI main, final AbstractButton button);
    public boolean checked() { return false; }
    public abstract String desc();
    public String help() { return null; }
    public String key() { return null; }
  }

  /** Undo Command. */
  class UndoCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      undo();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(!undo.first());
    }
    @Override
    public String desc() {
      return GUIUNDO;
    }
  }

  /** Redo Command. */
  class RedoCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      redo();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(!undo.last());
    }
    @Override
    public String desc() {
      return GUIREDO;
    }
  }

  /** Cut Command. */
  class CutCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      cut();
      rend.calc();
      showCursor(2);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.start() != -1);
    }
    @Override
    public String desc() {
      return GUICUT;
    }
  }

  /** Copy Command. */
  class CopyCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      copy();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.start() != -1);
    }
    @Override
    public String desc() {
      return GUICOPY;
    }
  }

  /** Paste Command. */
  class PasteCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      paste();
      rend.calc();
      showCursor(2);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(clip() != null);
    }
    @Override
    public String desc() {
      return GUIPASTE;
    }
  }

  /** Delete Command. */
  class DelCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      delete();
      rend.calc();
      showCursor(2);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.start() != -1);
    }
    @Override
    public String desc() {
      return GUIDEL;
    }
  }

  /** Select all Command. */
  class AllCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      selectAll();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
    }
    @Override
    public String desc() {
      return GUIALL;
    }
  }
}
