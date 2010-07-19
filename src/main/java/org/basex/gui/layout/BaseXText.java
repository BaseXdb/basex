package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
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
import java.util.Arrays;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.MatteBorder;
import org.basex.core.Main;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Fill;
import org.basex.util.Undo;
import static org.basex.util.Token.*;

/**
 * This class offers a fast text input, using the {@link BaseXTextRenderer}
 * class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class BaseXText extends BaseXPanel {
  /** Text array to be written. */
  protected BaseXTextTokens text = new BaseXTextTokens(EMPTY);
  /** Renderer reference. */
  protected final BaseXTextRenderer rend;
  /** Scrollbar reference. */
  private final BaseXBar scroll;
  /** Undo history; if set to {@code null}, text will be read-only. */
  protected final Undo undo;

  /** Search field. */
  private BaseXTextField find;

  /**
   * Default constructor.
   * @param edit editable flag
   * @param win parent window
   */
  public BaseXText(final boolean edit, final Window win) {
    super(win);
    setFocusable(true);
    setFocusTraversalKeysEnabled(!edit);

    BaseXLayout.addInteraction(this, null, win);
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

    Undo un = null;
    if(edit) {
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 1, 1, GUIConstants.COLORS[6]));
      un = new Undo();
    } else {
      setMode(Fill.NONE);
    }
    undo = un;

    new BaseXPopup(this, edit ?
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
    if(undo != null) undo.reset(t);
  }

  /**
   * Adds a search dialog.
   * @param f search field
   */
  public final void addSearch(final BaseXTextField f) {
    f.addSearch(this);
    find = f;
  }

  /**
   * Finds the specified term.
   * @param t output text
   * @param b backward browsing
   */
  final void find(final String t, final boolean b) {
    find(rend.find(t, b));
  }

  /**
   * Displays the search term.
   * @param y vertical position
   */
  final void find(final int y) {
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
  public final void setText(final byte[] t, final int s) {
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
    if(undo != null) undo.store(t.length != ns ? Arrays.copyOf(t, ns) : t, 0);
    SwingUtilities.invokeLater(calc);
  }

  /**
   * Sets a syntax highlighter.
   * @param s syntax reference
   */
  public final void setSyntax(final BaseXSyntax s) {
    rend.setSyntax(s);
  }

  /**
   * Sets a new cursor position.
   * @param p cursor position
   */
  public final void setCaret(final int p) {
    text.setCaret(p);
    showCursor(1);
    cursor(true);
  }

  /**
   * Jumps to the end of the text.
   */
  public final void scrollToEnd() {
    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
        text.pos(text.size());
        text.setCaret();
        showCursor(2);
      }
    });
  }

  /**
   * Returns the output text.
   * @return output text
   */
  public final byte[] getText() {
    return text.toArray();
  }

  @Override
  public final void setFont(final Font f) {
    if(rend != null) {
      rend.setFont(f);
      rend.repaint();
    }
  }

  /**
   * Refreshes the syntax highlighting.
   * @param s start of optional error mark
   */
  public final void error(final int s) {
    text.error(s);
    rend.repaint();
  }

  @Override
  public final void setEnabled(final boolean e) {
    super.setEnabled(e);
    rend.setEnabled(e);
    scroll.setEnabled(e);
    cursor(e);
  }

  /**
   * Selects the whole text.
   */
  final void selectAll() {
    text.pos(0);
    text.startMark();
    text.pos(text.size());
    text.endMark();
    rend.repaint();
  }

  // MOUSE INTERACTIONS =======================================================

  @Override
  public final void mouseEntered(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORTEXT);
  }

  @Override
  public final void mouseExited(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public final void mouseReleased(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;
    rend.stopSelect();
  }

  @Override
  public final void mouseDragged(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    rend.select(scroll.pos(), e.getPoint(), true);

    final int y = Math.max(20, Math.min(e.getY(), getHeight() - 20));
    if(y != e.getY()) scroll.pos(scroll.pos() + e.getY() - y);
  }

  @Override
  public final void mousePressed(final MouseEvent e) {
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
  private void selectLine() {
    text.pos(text.cursor());
    text.bol(true);
    text.startMark();
    text.forward(Integer.MAX_VALUE, true);
    text.endMark();
  }

  // KEY INTERACTIONS =======================================================

  @Override
  public void keyPressed(final KeyEvent e) {
    if(modifier(e)) return;

    // operations that change the focus are put first..
    if(pressed(PREVTAB, e)) {
      transferFocusBackward();
      return;
    }
    if(pressed(NEXTTAB, e)) {
      transferFocus();
      return;
    }
    if(pressed(FIND, e)) {
      if(find != null) find.requestFocusInWindow();
      return;
    }

    // re-animate cursor
    cursor(true);

    // operations without cursor movement...
    final int fh = rend.fontH();
    if(pressed(SCROLLDOWN, e)) {
      scroll.pos(scroll.pos() + fh);
      return;
    }
    if(pressed(SCROLLUP, e)) {
      scroll.pos(scroll.pos() - fh);
      return;
    }
    if(pressed(COPY, e)) {
      copy();
      return;
    }

    // set cursor position and reset last column
    text.pos(text.cursor());
    if(!pressed(PREVLINE, e) && !pressed(NEXTLINE, e)) lastCol = -1;

    if(pressed(FINDNEXT, e) || pressed(FINDPREV, e)) {
      find(rend.find(pressed(FINDPREV, e), true));
      return;
    }
    if(pressed(SELECTALL, e)) {
      selectAll();
      text.setCaret();
      return;
    }

    final boolean marking = e.isShiftDown();
    final boolean nomark = text.start() == -1;
    if(marking && nomark) text.startMark();
    boolean down = true;
    boolean consumed = true;

    // operations that consider the last text mark..
    if(pressed(NEXTWORD, e)) {
      text.nextToken(marking);
    } else if(pressed(PREVWORD, e)) {
      text.prevToken(marking);
      down = false;
    } else if(pressed(TEXTSTART, e)) {
      if(!marking) text.noMark();
      text.pos(0);
      down = false;
    } else if(pressed(TEXTEND, e)) {
      if(!marking) text.noMark();
      text.pos(text.size());
    } else if(pressed(LINESTART, e)) {
      text.bol(marking);
      down = false;
    } else if(pressed(LINEEND, e)) {
      text.forward(Integer.MAX_VALUE, marking);
    } else if(pressed(NEXTPAGE, e)) {
      down(getHeight() / fh, marking);
    } else if(pressed(PREVPAGE, e)) {
      up(getHeight() / fh, marking);
      down = false;
    } else if(pressed(NEXT, e)) {
      text.next(marking);
    } else if(pressed(PREV, e)) {
      text.prev(marking);
      down = false;
    } else if(pressed(PREVLINE, e)) {
      up(1, marking);
      down = false;
    } else if(pressed(NEXTLINE, e)) {
      down(1, marking);
    } else {
      consumed = false;
    }

    final byte[] txt = text.text;
    if(marking && !pressed(DELNEXT, e) && !pressed(DELPREV, e)) {
      // refresh scroll position
      text.endMark();
      text.checkMark();
    } else if(undo != null) {
      // edit operations...
      if(pressed(CUT, e)) {
        cut();
      } else if(pressed(PASTE, e)) {
        paste();
      } else if(pressed(UNDO, e)) {
        undo();
      } else if(pressed(REDO, e)) {
        redo();
      } else if(pressed(DELLINEEND, e) || pressed(DELNEXTWORD, e) ||
          pressed(DELNEXT, e)) {
        if(nomark) {
          if(text.pos() == text.size()) return;
          text.startMark();
          if(pressed(DELNEXTWORD, e)) {
            text.nextToken(true);
          } else if(pressed(DELLINEEND, e)) {
            text.forward(Integer.MAX_VALUE, true);
          } else {
            text.next(true);
          }
          text.endMark();
        }
        text.delete();
      } else if(pressed(DELLINESTART, e) || pressed(DELPREVWORD, e) ||
          pressed(DELPREV, e)) {
        if(nomark) {
          if(text.pos() == 0) return;
          text.startMark();
          if(pressed(DELPREVWORD, e)) {
            text.prevToken(true);
          } else if(pressed(DELLINESTART, e)) {
            text.bol(true);
          } else {
            text.prev();
          }
          text.endMark();
        }
        text.delete();
        down = false;
      } else {
        consumed = false;
      }
    }
    if(consumed) e.consume();

    text.setCaret();
    if(txt != text.text) rend.calc();
    showCursor(down ? 2 : 0);
  }

  /**
   * Displays the currently edited text area.
   * @param align vertical alignment
   */
  final void showCursor(final int align) {
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
   * Moves the cursor down. The current column position is remembered in
   * {@link #lastCol} and, if possible, restored.
   * @param l number of lines to move cursor
   * @param mark mark flag
   */
  private void down(final int l, final boolean mark) {
    if(!mark) text.noMark();
    final int x = text.bol(mark);
    if(lastCol == -1) lastCol = x;
    for(int i = 0; i < l; i++) {
      text.forward(Integer.MAX_VALUE, mark);
      text.next(mark);
    }
    text.forward(lastCol, mark);
    if(text.pos() == text.size()) lastCol = -1;
  }

  /**
   * Moves the cursor up.
   * @param l number of lines to move cursor
   * @param mark mark flag
   */
  private void up(final int l, final boolean mark) {
    if(!mark) text.noMark();
    final int x = text.bol(mark);
    if(lastCol == -1) lastCol = x;
    if(text.pos() == 0) {
      lastCol = -1;
      return;
    }
    for(int i = 0; i < l; i++) {
      text.prev(mark);
      text.bol(mark);
    }
    text.forward(lastCol, mark);
  }

  /** Last horizontal position. */
  private int lastCol = -1;

  @Override
  public final void keyTyped(final KeyEvent e) {
    if(undo == null || ignoreTyped(e) || pressed(ESCAPE, e) ||
        pressed(DELNEXT, e) || pressed(DELPREV, e)) return;

    text.pos(text.cursor());
    if(text.start() != -1) text.delete();
    text.add(String.valueOf(e.getKeyChar()));
    text.setCaret();
    rend.calc();
    showCursor(2);
    e.consume();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if(undo != null) undo.store(text.toArray(), text.cursor());
  }

  /**
   * Releases a key or mouse. Can be overwritten to react on events.
   * @param force force querying
   */
  @SuppressWarnings("unused")
  protected void release(final boolean force) { }

  // EDITOR COMMANDS ==========================================================

  /**
   * Undoes the text.
   */
  protected final void undo() {
    if(undo == null) return;
    text = new BaseXTextTokens(undo.prev());
    rend.setText(text);
    text.pos(undo.cursor());
    text.setCaret();
  }

  /**
   * Redoes the text.
   */
  protected final void redo() {
    if(undo == null) return;
    text = new BaseXTextTokens(undo.next());
    rend.setText(text);
    text.pos(undo.cursor());
    text.setCaret();
  }

  /**
   * Cuts the selected text to the clipboard.
   */
  protected final void cut() {
    text.pos(text.cursor());
    if(copy()) delete();
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  protected final boolean copy() {
    final String txt = text.copy();
    if(txt.isEmpty()) {
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
  protected final void paste() {
    // copy selection to clipboard
    final String txt = clip();
    if(txt == null) return;
    text.pos(text.cursor());
    if(text.start() != -1) text.delete();
    text.add(txt);
    if(undo != null) undo.store(text.toArray(), text.cursor());
  }

  /**
   * Deletes the selected text.
   */
  protected final void delete() {
    text.pos(text.cursor());
    text.delete();
    if(undo != null) undo.store(text.toArray(), text.cursor());
    text.setCaret();
  }

  /**
   * Returns the clipboard text.
   * @return text
   */
  final String clip() {
    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable t = clip.getContents(null);
    if(t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      try {
        return (String) t.getTransferData(DataFlavor.stringFlavor);
      } catch(final Exception ex) {
        Main.debug(ex);
      }
    }
    return null;
  }

  /** Cursor. */
  final Timer cursor = new Timer(500, new ActionListener() {
    @Override
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
  protected final void cursor(final boolean start) {
    cursor.stop();
    if(start) cursor.start();
    rend.cursor(start);
    rend.repaint();
  }

  @Override
  public final void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    rend.repaint();
  }

  @Override
  public final void componentResized(final ComponentEvent e) {
    scroll.pos(0);
    SwingUtilities.invokeLater(calc);
  }

  /** Calculation counter. */
  private final Thread calc = new Thread() {
    @Override
    public void run() {
      rend.calc();
      rend.repaint();
    }
  };

  /** Text command. */
  abstract class TextCmd implements GUICommand {
    @Override
    public boolean checked() {
      return false;
    }
    @Override
    public String help() {
      return null;
    }
    @Override
    public String key() {
      return null;
    }
    /** Finishes a command. */
    public void finish() {
      text.setCaret();
      rend.calc();
      showCursor(2);
      release(true);
    }
  }

  /** Undo command. */
  class UndoCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      undo();
      finish();
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

  /** Redo command. */
  class RedoCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      redo();
      finish();
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

  /** Cut command. */
  class CutCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      cut();
      finish();
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

  /** Copy command. */
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

  /** Paste command. */
  class PasteCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      paste();
      finish();
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

  /** Delete command. */
  class DelCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      delete();
      finish();
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

  /** Select all command. */
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
