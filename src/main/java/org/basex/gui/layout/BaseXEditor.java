package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides a text viewer and editor, using the
 * {@link BaseXTextRenderer} class to render the text.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXEditor extends BaseXPanel {
  /** Text array to be written. */
  protected transient BaseXTextTokens text = new BaseXTextTokens(EMPTY);
  /** Renderer reference. */
  final BaseXTextRenderer rend;
  /** Undo history; if set to {@code null}, text will be read-only. */
  final transient Undo undo;

  /** Scrollbar reference. */
  private final BaseXBar scroll;
  /** Search field. */
  private BaseXTextField find;

  /**
   * Default constructor.
   * @param edit editable flag
   * @param win parent window
   */
  public BaseXEditor(final boolean edit, final Window win) {
    super(win);
    setFocusable(true);
    setFocusTraversalKeysEnabled(!edit);

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

    layout(new BorderLayout(4, 0));
    scroll = new BaseXBar(this);
    rend = new BaseXTextRenderer(text, scroll);

    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);

    Undo un = null;
    if(edit) {
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 0, 0, GUIConstants.color(6)));
      un = new Undo();
    } else {
      mode(Fill.NONE);
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
  public final void setSearch(final BaseXTextField f) {
    f.setSearch(this);
    find = f;
  }

  /**
   * Returns the cursor coordinates.
   * @return line/column
   */
  public final String pos() {
    final int[] pos = rend.pos();
    return pos[0] + " : " + pos[1];
  }

  /**
   * Finds the next/previous occurrence of the current keyword.
   * @param forward forward search
   */
  final void find(final boolean forward) {
    scroll(rend.find(forward, true));
  }

  /**
   * Sets a new keyword.
   * @param key new keyword
   * @return old keyword
   */
  String keyword(final String key) {
    return rend.keyword(key);
  }

  /**
   * Finds the current keyword.
   * @return {@code true} if the keyword was found
   */
  final boolean find() {
    final int p = rend.find(true, false);
    scroll(p);
    return p != 0;
  }

  /**
   * Displays the search term.
   * @param y vertical position
   */
  final void scroll(final int y) {
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
    // remove invalid characters and compare old with new string
    int ns = 0;
    final int ts = text.size();
    final byte[] tt = text.text();
    boolean eq = true;
    for(int r = 0; r < s; ++r) {
      final byte b = t[r];
      // support characters, highlighting codes, tabs and newlines
      if(b >= ' ' || b <= TokenBuilder.MARK || b == 0x09 || b == 0x0A) {
        t[ns++] = t[r];
      }
      eq &= ns < ts && ns < s && t[ns] == tt[ns];
    }
    eq &= ns == ts;

    // new text is different...
    if(!eq) {
      text = new BaseXTextTokens(Arrays.copyOf(t, ns));
      rend.setText(text);
      scroll.pos(0);
    }
    if(undo != null) undo.store(t.length != ns ? Arrays.copyOf(t, ns) : t, 0);
    SwingUtilities.invokeLater(calc);
  }

  /**
   * Sets a syntax highlighter, based on the file format.
   * @param file file reference
   * @param opened indicates if file was opened from disk
   */
  protected final void setSyntax(final IO file, final boolean opened) {
    setSyntax(
      !opened || file.hasSuffix(IO.XQSUFFIXES) ? new XQuerySyntax() :
      file.hasSuffix(IO.JSONSUFFIX) ? new JSONSyntax() :
      file.hasSuffix(IO.XMLSUFFIXES) || file.hasSuffix(IO.HTMLSUFFIXES) ||
      file.hasSuffix(IO.BXSSUFFIX) ? new XMLSyntax() : BaseXSyntax.SIMPLE);
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
    SwingUtilities.invokeLater(new Runnable() {
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
    return text.text();
  }

  @Override
  public final void setFont(final Font f) {
    super.setFont(f);
    if(rend != null) {
      rend.setFont(f);
      rend.repaint();
    }
  }

  /**
   * Moves the error marker. {@code -1} removes the marker.
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
    text.selectAll();
    text.setCaret();
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
  public void mouseReleased(final MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e)) rend.stopSelect();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if(!SwingUtilities.isMiddleMouseButton(e)) return;
    if(!paste()) return;
    finish();
    repaint();
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
    if(!isEnabled() || !isFocusable()) return;

    requestFocusInWindow();
    cursor(true);

    if(SwingUtilities.isMiddleMouseButton(e)) copy();

    final boolean marking = e.isShiftDown();
    final boolean nomark = !text.marked();
    if(SwingUtilities.isLeftMouseButton(e)) {
      final int c = e.getClickCount();
      if(c == 1) {
        // selection mode
        if(marking && nomark) text.startMark();
        rend.select(scroll.pos(), e.getPoint(), marking);
      } else if(c == 2) {
        text.selectWord();
      } else {
        text.selectLine();
      }
    } else if(nomark) {
      rend.select(scroll.pos(), e.getPoint(), false);
    }
  }

  // KEY INTERACTIONS =======================================================

  @Override
  public void keyPressed(final KeyEvent e) {
    if(modifier(e)) return;

    // operations that change the focus are put first..
    if(PREVTAB.is(e)) {
      transferFocusBackward();
      return;
    }
    if(NEXTTAB.is(e)) {
      transferFocus();
      return;
    }
    if(FIND.is(e)) {
      if(find != null) find.requestFocusInWindow();
      return;
    }

    // re-animate cursor
    cursor(true);

    // operations without cursor movement...
    final int fh = rend.fontH();
    if(SCROLLDOWN.is(e)) {
      scroll.pos(scroll.pos() + fh);
      return;
    }
    if(SCROLLUP.is(e)) {
      scroll.pos(scroll.pos() - fh);
      return;
    }
    if(COPY1.is(e) || COPY2.is(e)) {
      copy();
      return;
    }

    // set cursor position and reset last column
    text.pos(text.cursor());
    if(!PREVLINE.is(e) && !NEXTLINE.is(e)) lastCol = -1;

    if(FINDNEXT.is(e) || FINDNEXT2.is(e)) {
      scroll(rend.find(true, true));
      return;
    }
    if(FINDPREV.is(e) || FINDPREV2.is(e)) {
      scroll(rend.find(false, true));
      return;
    }
    if(SELECTALL.is(e)) {
      selectAll();
      return;
    }

    // necessary on Macs as the shift button is pressed for REDO
    final boolean marking = e.isShiftDown() &&
      !DELNEXT.is(e) && !DELPREV.is(e) && !PASTE2.is(e) && !COMMENT.is(e) &&
      !DELLINE.is(e) && !REDOSTEP.is(e);
    final boolean nomark = !text.marked();
    if(marking && nomark) text.startMark();
    boolean down = true;
    boolean consumed = true;

    // operations that consider the last text mark..
    final byte[] txt = text.text();
    if(NEXTWORD.is(e)) {
      text.nextToken(marking);
    } else if(PREVWORD.is(e)) {
      text.prevToken(marking);
      down = false;
    } else if(TEXTSTART.is(e)) {
      if(!marking) text.noMark();
      text.pos(0);
      down = false;
    } else if(TEXTEND.is(e)) {
      if(!marking) text.noMark();
      text.pos(text.size());
    } else if(LINESTART.is(e)) {
      text.bol(marking);
      down = false;
    } else if(LINEEND.is(e)) {
      text.eol(marking);
    } else if(NEXTPAGE.is(e)) {
      down(getHeight() / fh, marking);
    } else if(PREVPAGE.is(e)) {
      up(getHeight() / fh, marking);
      down = false;
    } else if(NEXT.is(e)) {
      text.next(marking);
    } else if(PREV.is(e)) {
      text.prev(marking);
      down = false;
    } else if(PREVLINE.is(e)) {
      up(1, marking);
      down = false;
    } else if(NEXTLINE.is(e)) {
      down(1, marking);
    } else if(FINDERROR.is(e)) {
      final int p = text.error();
      if(p != -1) setCaret(p);
    } else {
      consumed = false;
    }

    if(marking) {
      // refresh scroll position
      text.endMark();
      text.checkMark();
    } else if(undo != null) {
      // edit operations...
      if(CUT1.is(e) || CUT2.is(e)) {
        cut();
      } else if(PASTE1.is(e) || PASTE2.is(e)) {
        paste();
      } else if(UNDOSTEP.is(e)) {
        undo();
      } else if(REDOSTEP.is(e)) {
        redo();
      } else if(COMMENT.is(e)) {
        text.comment(rend.getSyntax());
      } else if(DELLINE.is(e)) {
        text.deleteLine();
      } else if(DELLINEEND.is(e) || DELNEXTWORD.is(e) || DELNEXT.is(e)) {
        if(nomark) {
          if(text.pos() == text.size()) return;
          text.startMark();
          if(DELNEXTWORD.is(e)) {
            text.nextToken(true);
          } else if(DELLINEEND.is(e)) {
            text.eol(true);
          } else {
            text.next(true);
          }
          text.endMark();
        }
        undo.cursor(text.cursor());
        text.delete();
      } else if(DELLINESTART.is(e) || DELPREVWORD.is(e) || DELPREV.is(e)) {
        if(nomark) {
          if(text.pos() == 0) return;
          text.startMark();
          if(DELPREVWORD.is(e)) {
            text.prevToken(true);
          } else if(DELLINESTART.is(e)) {
            text.bol(true);
          } else {
            text.prev();
          }
          text.endMark();
        }
        undo.cursor(text.cursor());
        text.delete();
        down = false;
      } else {
        consumed = false;
      }
    }
    if(consumed) e.consume();

    text.setCaret();
    if(txt != text.text()) rend.calc();
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
    for(int i = 0; i < l; ++i) {
      text.eol(mark);
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
    for(int i = 0; i < l; ++i) {
      text.prev(mark);
      text.bol(mark);
    }
    text.forward(lastCol, mark);
  }

  /** Last horizontal position. */
  private int lastCol = -1;

  @Override
  public void keyTyped(final KeyEvent e) {
    if(undo == null || control(e) || DELNEXT.is(e) || DELPREV.is(e) || ESCAPE.is(e))
      return;

    text.pos(text.cursor());
    // string to be added
    String ch = String.valueOf(e.getKeyChar());

    // remember if marked text is to be deleted
    boolean del = true;
    final byte[] txt = text.text();
    if(TAB.is(e)) {
      if(text.marked()) {
        // check if lines are to be indented
        final int s = Math.min(text.pos(), text.start());
        final int l = Math.max(text.pos(), text.start()) - 1;
        for(int p = s; p <= l && p < txt.length; p++) del &= txt[p] != '\n';
        if(!del) {
          text.indent(s, l, e.isShiftDown());
          ch = null;
        }
      } else {
        boolean c = true;
        for(int p = text.pos() - 1; p >= 0 && c; p--) {
          final byte b = txt[p];
          c = ws(b);
          if(b == '\n') break;
        }
        if(c) ch = "  ";
      }
    }

    // delete marked text
    if(text.marked() && del) text.delete();

    if(ENTER.is(e)) {
      // adopt indentation from previous line
      final StringBuilder sb = new StringBuilder(1).append(e.getKeyChar());
      int s = 0;
      for(int p = text.pos() - 1; p >= 0; p--) {
        final byte b = txt[p];
        if(b == '\n') break;
        if(b == '\t') {
          s += 2;
        } else if(b == ' ') {
          s++;
        } else {
          s = 0;
        }
      }
      for(int p = 0; p < s; p++) sb.append(' ');
      ch = sb.toString();
    }

    if(ch != null) text.add(ch);
    text.setCaret();
    rend.calc();
    showCursor(2);
    e.consume();
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    if(undo != null) undo.store(text.text(), text.cursor());
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
  final void undo() {
    if(undo == null) return;
    text = new BaseXTextTokens(undo.prev());
    rend.setText(text);
    text.pos(undo.cursor());
    text.setCaret();
  }

  /**
   * Redoes the text.
   */
  final void redo() {
    if(undo == null) return;
    text = new BaseXTextTokens(undo.next());
    rend.setText(text);
    text.pos(undo.cursor());
    text.setCaret();
  }

  /**
   * Cuts the selected text to the clipboard.
   */
  final void cut() {
    text.pos(text.cursor());
    if(copy()) delete();
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  final boolean copy() {
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
   * @return success flag
   */
  final boolean paste() {
    return paste(clip());
  }

  /**
   * Pastes the specified string.
   * @param txt string to be pasted
   * @return success flag
   */
  final boolean paste(final String txt) {
    if(txt == null || undo == null) return false;
    text.pos(text.cursor());
    undo.cursor(text.cursor());
    if(text.marked()) text.delete();
    text.add(txt);
    undo.store(text.text(), text.cursor());
    return true;
  }

  /**
   * Deletes the selected text.
   */
  final void delete() {
    if(undo == null) return;
    text.pos(text.cursor());
    undo.cursor(text.cursor());
    text.delete();
    undo.store(text.text(), text.cursor());
    text.setCaret();
  }

  /**
   * Returns the clipboard text.
   * @return text
   */
  static final String clip() {
    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable tr = clip.getContents(null);
    if(tr != null) {
      for(final Object o : BaseXLayout.contents(tr)) return o.toString();
    }
    return "";
  }

  /**
   * Finishes a command.
   */
  void finish() {
    text.setCaret();
    rend.calc();
    showCursor(2);
    release(false);
  }

  /** Cursor. */
  private final Timer cursor = new Timer(500, new ActionListener() {
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
  final void cursor(final boolean start) {
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
  private final transient Runnable calc = new Runnable() {
    @Override
    public void run() {
      rend.calc();
      rend.repaint();
    }
  };

  /** Text command. */
  abstract static class TextCmd implements GUICommand {
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
    public String label() {
      return UNDO;
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
    public String label() {
      return REDO;
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
      button.setEnabled(text.marked());
    }
    @Override
    public String label() {
      return CUT;
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
      button.setEnabled(text.marked());
    }
    @Override
    public String label() {
      return COPY;
    }
  }

  /** Paste command. */
  class PasteCmd extends TextCmd {
    @Override
    public void execute(final GUI main) {
      if(paste()) finish();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(clip() != null);
    }
    @Override
    public String label() {
      return PASTE;
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
      button.setEnabled(text.marked());
    }
    @Override
    public String label() {
      return DELETE;
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
    public String label() {
      return SELECT_ALL;
    }
  }
}
