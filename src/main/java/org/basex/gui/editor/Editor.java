package org.basex.gui.editor;

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

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class provides a text viewer and editor, using the
 * {@link Renderer} class to render the text.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class Editor extends BaseXPanel {
  /** Delay for highlighting an error. */
  private static final int ERROR_DELAY = 500;

  /** Search direction. */
  public enum SearchDir {
    /** Current hit. */
    CURRENT,
    /** Next hit. */
    FORWARD,
    /** Previous hit. */
    BACKWARD,
  }

  /** Editor action. */
  public enum Action {
    /** Check for changes; do nothing if input has not changed. */
    CHECK,
    /** Enforce parsing of input. */
    PARSE,
    /** Enforce execution of input. */
    EXECUTE
  }

  /** Text array to be written. */
  protected final transient EditorText text = new EditorText(EMPTY);
  /** Undo history. */
  public transient History hist;
  /** Renderer reference. */
  final Renderer rend;
  /** Scrollbar reference. */
  final BaseXBar scroll;
  /** Editable flag. */
  final boolean editable;
  /** Search field. */
  private SearchPanel search;
  /** Link listener. */
  private LinkListener linkListener;

  /**
   * Default constructor.
   * @param edit editable flag
   * @param win parent window
   */
  public Editor(final boolean edit, final Window win) {
    this(edit, win, EMPTY);
  }

  /**
   * Default constructor.
   * @param edit editable flag
   * @param win parent window
   * @param txt initial text
   */
  public Editor(final boolean edit, final Window win, final byte[] txt) {
    super(win);
    setFocusable(true);
    setFocusTraversalKeysEnabled(!edit);
    editable = edit;

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
      }
    });

    layout(new BorderLayout(4, 0));
    scroll = new BaseXBar(this);
    rend = new Renderer(text, scroll);
    setFont(GUIConstants.dmfont);

    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);

    setText(txt);
    hist = new History(edit ? text.text() : null);

    if(edit) {
      setBackground(Color.white);
      setBorder(new MatteBorder(1, 1, 0, 0, GUIConstants.color(6)));
    } else {
      mode(Fill.NONE);
    }

    new BaseXPopup(this, edit ?
      new GUICmd[] { new UndoCmd(), new RedoCmd(), null, new CutCmd(),
        new CopyCmd(), new PasteCmd(), new DelCmd(), null, new AllCmd() } :
      new GUICmd[] { new CopyCmd(), null, new AllCmd() });
  }

  /**
   * Sets the output text.
   * @param t output text
   */
  public void setText(final byte[] t) {
    setText(t, t.length);
    resetError();
  }

  /**
   * Returns the cursor coordinates.
   * @return line/column
   */
  public final int[] pos() {
    return rend.pos();
  }

  /**
   * Sets the output text.
   * @param t output text
   * @param s text size
   */
  public final void setText(final byte[] t, final int s) {
    // remove invalid characters and compare old with new string
    int ns = 0;
    final int pc = text.getCaret();
    final int ts = text.size();
    final byte[] old = text.text();
    boolean eq = true;
    for(int r = 0; r < s; ++r) {
      final byte b = t[r];
      // ignore carriage return
      if(b != 0x0D) t[ns++] = t[r];
      // support characters, highlighting codes, tabs and newlines
      //if(b >= ' ' || b <= TokenBuilder.ULINE || b == 0x09 || b == 0x0A) t[ns++] = t[r];
      eq &= ns < ts && ns < s && t[ns] == old[ns];
    }
    eq &= ns == ts;

    // new text is different...
    if(!eq) text.text(Arrays.copyOf(t, ns));
    if(hist != null) hist.store(t.length != ns ? Arrays.copyOf(t, ns) : t, pc, 0);
    componentResized(null);
  }

  /**
   * Sets a syntax highlighter, based on the file format.
   * @param file file reference
   * @param opened indicates if file was opened from disk
   */
  protected final void setSyntax(final IO file, final boolean opened) {
    setSyntax(
      !opened || file.hasSuffix(IO.XQSUFFIXES) ? new SyntaxXQuery() :
      file.hasSuffix(IO.JSONSUFFIX) ? new SyntaxJSON() :
      file.hasSuffix(IO.XMLSUFFIXES) || file.hasSuffix(IO.HTMLSUFFIXES) ||
      file.hasSuffix(IO.XSLSUFFIXES) || file.hasSuffix(IO.BXSSUFFIX) ?
        new SyntaxXML() : Syntax.SIMPLE);
  }

  /**
   * Returns the editable flag.
   * @return boolean result
   */
  public final boolean isEditable() {
    return editable;
  }

  /**
   * Sets a syntax highlighter.
   * @param s syntax reference
   */
  public final void setSyntax(final Syntax s) {
    rend.setSyntax(s);
  }

  /**
   * Sets a new text cursor position.
   * @param p cursor position
   */
  public final void setCaret(final int p) {
    text.setCaret(p);
    showCursor(1);
    cursor(true);
  }

  /**
   * Returns the current text cursor.
   * @return cursor position
   */
  public final int getCaret() {
    return text.getCaret();
  }

  /**
   * Jumps to the end of the text.
   */
  public final void scrollToEnd() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        text.setCaret(text.size());
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

  /** Thread counter. */
  int errorID;

  /**
   * Removes the error marker.
   */
  public final void resetError() {
    ++errorID;
    text.error(-1);
    rend.repaint();
  }

  /**
   * Sets the error marker.
   * @param pos start of optional error mark
   */
  public final void error(final int pos) {
    final int eid = ++errorID;
    text.error(pos);
    new Thread() {
      @Override
      public void run() {
        Performance.sleep(ERROR_DELAY);
        if(eid == errorID) rend.repaint();
      }
    }.start();
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
    final int s = text.size();
    text.select(0, s);
    text.setCaret(s);
    rend.repaint();
  }

  // SEARCH OPERATIONS ==================================================================

  /**
   * Installs a link listener.
   * @param ll link listener
   */
  public final void setLinkListener(final LinkListener ll) {
    linkListener = ll;
  }

  /**
   * Installs a search panel.
   * @param s search panel
   */
  final void setSearch(final SearchPanel s) {
    search = s;
  }

  /**
   * Returns the search panel.
   * @return search panel
   */
  public final SearchPanel getSearch() {
    return search;
  }

  /**
   * Performs a search.
   * @param sc search context
   * @param jump jump to next hit
   */
  final void search(final SearchContext sc, final boolean jump) {
    try {
      rend.search(sc);
      gui.status.setText(sc.search.isEmpty() ? OK : Util.info(STRINGS_FOUND_X,  sc.nr()));
      if(jump) jump(SearchDir.CURRENT, false);
    } catch(final Exception ex) {
      final String msg = Util.message(ex).replaceAll(Prop.NL + ".*", "");
      gui.status.setError(REGULAR_EXPR + COLS + msg);
    }
  }

  /**
   * Replaces the text.
   * @param rc replace context
   */
  final void replace(final ReplaceContext rc) {
    try {
      final int[] select = rend.replace(rc);
      if(rc.text != null) {
        final boolean sel = text.selected();
        setText(rc.text);
        if(sel) text.select(select[0], select[1]);
        text.setCaret(select[0]);
        release(Action.CHECK);
      }
      gui.status.setText(Util.info(STRINGS_REPLACED));
    } catch(final Exception ex) {
      final String msg = Util.message(ex).replaceAll(Prop.NL + ".*", "");
      gui.status.setError(REGULAR_EXPR + COLS + msg);
    }
  }

  /**
   * Jumps to a search string.
   * @param dir search direction
   * @param select select hit
   */
  final void jump(final SearchDir dir, final boolean select) {
    // updates the visible area
    final int y = rend.jump(dir, select);
    final int h = getHeight();
    final int p = scroll.pos();
    final int m = y + rend.fontH() * 3 - h;
    if(y != -1 && (p < m || p > y)) scroll.pos(y - h / 2);
    rend.repaint();
  }

  // MOUSE INTERACTIONS =================================================================

  @Override
  public final void mouseEntered(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORTEXT);
  }

  @Override
  public final void mouseExited(final MouseEvent e) {
    gui.cursor(GUIConstants.CURSORARROW);
  }

  @Override
  public final void mouseMoved(final MouseEvent e) {
    if(linkListener == null) return;
    final boolean link = rend.link(scroll.pos(), e.getPoint());
    gui.cursor(link ? GUIConstants.CURSORHAND : GUIConstants.CURSORARROW);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e)) {
      text.checkSelect();
      // evaluate link
      if(!text.selected() && rend.link(scroll.pos(), e.getPoint()))
        linkListener.linkClicked(rend.link());
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if(!SwingUtilities.isMiddleMouseButton(e)) return;
    new PasteCmd().execute(gui);
  }

  @Override
  public final void mouseDragged(final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return;

    // selection mode
    rend.select(scroll.pos(), e.getPoint(), false);
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
    final boolean nomark = !text.selecting();
    if(SwingUtilities.isLeftMouseButton(e)) {
      final int c = e.getClickCount();
      if(c == 1) {
        // selection mode
        if(marking && nomark) text.startSelect();
        rend.select(scroll.pos(), e.getPoint(), !marking);
      } else if(c == 2) {
        text.selectWord();
      } else {
        text.selectLine();
      }
    } else if(nomark) {
      rend.select(scroll.pos(), e.getPoint(), true);
    }
  }

  // KEY INTERACTIONS =======================================================

  @Override
  public void keyPressed(final KeyEvent e) {
    // handle search operations
    if(search != null) {
      if(ESCAPE.is(e)) {
        search.deactivate(true);
        return;
      }
      if(FIND.is(e)) {
        search.activate(text.copy());
        return;
      }
      if(FINDNEXT.is(e) || FINDNEXT2.is(e) || FINDPREV.is(e) || FINDPREV2.is(e)) {
        final boolean vis = search.isVisible();
        search.activate(text.copy());
        jump(vis ? FINDNEXT.is(e) || FINDNEXT2.is(e) ?
          SearchDir.FORWARD : SearchDir.BACKWARD : SearchDir.CURRENT, true);
        return;
      }
    }

    // ignore modifier keys
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
    final int pc = text.getCaret();
    text.pos(pc);
    if(!PREVLINE.is(e) && !NEXTLINE.is(e)) lastCol = -1;

    if(SELECTALL.is(e)) {
      selectAll();
      return;
    }

    // necessary on Macs as the shift button is pressed for REDO
    final boolean marking = e.isShiftDown() &&
      !DELNEXT.is(e) && !DELPREV.is(e) && !PASTE2.is(e) && !COMMENT.is(e) &&
      !DELLINE.is(e) && !REDOSTEP.is(e) && !PREVPAGE_RO.is(e);
    final boolean nomark = !text.selecting();
    if(marking && nomark) text.startSelect();
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
      if(!marking) text.noSelect();
      text.pos(0);
      down = false;
    } else if(TEXTEND.is(e)) {
      if(!marking) text.noSelect();
      text.pos(text.size());
    } else if(LINESTART.is(e)) {
      text.home(marking);
      down = false;
    } else if(LINEEND.is(e)) {
      text.eol(marking);
    } else if(PREVPAGE.is(e) || !hist.active() && PREVPAGE_RO.is(e)) {
      up(getHeight() / fh, marking);
      down = false;
    } else if(NEXTPAGE.is(e) || !hist.active() && NEXTPAGE_RO.is(e)) {
      down(getHeight() / fh, marking);
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
    } else {
      consumed = false;
    }

    if(marking) {
      // refresh scroll position
      text.finishSelect();
    } else if(hist.active()) {
      // edit operations...
      if(CUT1.is(e) || CUT2.is(e)) {
        if(copy()) text.delete();
      } else if(PASTE1.is(e) || PASTE2.is(e)) {
        final String clip = clip();
        if(clip != null) {
          if(text.selected()) text.delete();
          text.add(clip);
        }
      } else if(UNDOSTEP.is(e)) {
        final byte[] t = hist.prev();
        if(t != null) {
          text.text(t);
          text.pos(hist.cursor());
        }
      } else if(REDOSTEP.is(e)) {
        final byte[] t = hist.next();
        if(t != null) {
          text.text(t);
          text.pos(hist.cursor());
        }
      } else if(COMMENT.is(e)) {
        text.comment(rend.getSyntax());
      } else if(DELLINE.is(e)) {
        text.deleteLine();
      } else if(DELLINEEND.is(e) || DELNEXTWORD.is(e) || DELNEXT.is(e)) {
        if(nomark) {
          if(text.pos() == text.size()) return;
          text.startSelect();
          if(DELNEXTWORD.is(e)) {
            text.nextToken(true);
          } else if(DELLINEEND.is(e)) {
            text.eol(true);
          } else {
            text.next(true);
          }
          text.finishSelect();
        }
        text.delete();
      } else if(DELLINESTART.is(e) || DELPREVWORD.is(e) || DELPREV.is(e)) {
        if(nomark) {
          if(text.pos() == 0) return;
          text.startSelect();
          if(DELPREVWORD.is(e)) {
            text.prevToken(true);
          } else if(DELLINESTART.is(e)) {
            text.bol(true);
          } else {
            text.prev();
          }
          text.finishSelect();
        }
        text.delete();
        down = false;
      } else {
        consumed = false;
      }
    }
    if(consumed) e.consume();

    text.setCaret();
    final byte[] tmp = text.text();
    if(txt != tmp) {
      hist.store(tmp, pc, text.getCaret());
      calcCode.invokeLater(down);
    } else {
      showCursor(down ? 2 : 0);
    }
  }

  /** Thread counter. */
  private final GUICode calcCode = new GUICode() {
    @Override
    public void eval(final Object arg) {
      rend.calc();
      showCursor((Boolean) arg ? 2 : 0);
    }
  };

  /** Thread counter. */
  private final GUICode cursorCode = new GUICode() {
    @Override
    public void eval(final Object arg) {
      // updates the visible area
      final int p = scroll.pos();
      final int y = rend.cursorY();
      final int m = y + rend.fontH() * 3 - getHeight();
      if(p < m || p > y) {
        final int align = (Integer) arg;
        scroll.pos(align == 0 ? y : align == 1 ? y - getHeight() / 2 : m);
        rend.repaint();
      }
    }
  };

  /**
   * Displays the currently edited text area.
   * @param align vertical alignment
   */
  final void showCursor(final int align) {
    cursorCode.invokeLater(align);
  }

  /**
   * Moves the cursor down. The current column position is remembered in
   * {@link #lastCol} and, if possible, restored.
   * @param l number of lines to move cursor
   * @param mark mark flag
   */
  private void down(final int l, final boolean mark) {
    if(!mark) text.noSelect();
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
    if(!mark) text.noSelect();
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
    if(!hist.active() || control(e) || DELNEXT.is(e) || DELPREV.is(e) || ESCAPE.is(e))
      return;

    final int pc = text.getCaret();
    text.pos(pc);

    // remember if marked text is to be deleted
    final StringBuilder sb = new StringBuilder(1).append(e.getKeyChar());
    final boolean indent = TAB.is(e) && text.indent(sb, e.isShiftDown());

    // delete marked text
    if(text.selected() && !indent) text.delete();

    if(ENTER.is(e)) {
      text.open(sb);
    } else if(sb.length() != 0 && "}])".indexOf(sb.toString()) != -1) {
      text.close();
    }

    if(sb.length() != 0) text.add(sb.toString());
    text.setCaret();
    hist.store(text.text(), pc, text.getCaret());
    calcCode.invokeLater(true);
    e.consume();
  }

  /**
   * Releases a key or mouse. Can be overwritten to react on events.
   * @param action action
   */
  @SuppressWarnings("unused")
  protected void release(final Action action) { }

  // EDITOR COMMANDS ==========================================================

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  final boolean copy() {
    final String txt = text.copy();
    if(txt.isEmpty()) {
      text.noSelect();
      return false;
    }

    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    clip.setContents(new StringSelection(txt), null);
    return true;
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
    return null;
  }

  /**
   * Finishes a command.
   * @param old old cursor position; store entry to history if position != -1
   */
  void finish(final int old) {
    text.setCaret();
    if(old != -1) hist.store(text.text(), old, text.getCaret());
    calcCode.invokeLater(true);
    release(Action.CHECK);
  }

  /** Cursor. */
  private final Timer cursor = new Timer(500, new ActionListener() {
    @Override
    public void actionPerformed(final ActionEvent e) {
      rend.cursor(!rend.cursor());
    }
  });

  /**
   * Stops an old cursor thread and, if requested, starts a new one.
   * @param start start/stop flag
   */
  final void cursor(final boolean start) {
    cursor.stop();
    if(start) cursor.start();
    rend.cursor(start);
  }

  @Override
  public final void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    rend.repaint();
  }

  /** Calculation counter. */
  private final GUICode resizeCode = new GUICode() {
    @Override
    public void eval(final Object arg) {
      rend.calc();
      // update scrollbar to display value within valid range
      scroll.pos(scroll.pos());
      rend.repaint();
    }
  };

  @Override
  public final void componentResized(final ComponentEvent e) {
    resizeCode.invokeLater();
  }

  /** Undo command. */
  class UndoCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!hist.active()) return;
      final byte[] t = hist.prev();
      if(t == null) return;
      text.text(t);
      text.pos(hist.cursor());
      finish(-1);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(!hist.first());
    }
    @Override
    public String label() {
      return UNDO;
    }
  }

  /** Redo command. */
  class RedoCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!hist.active()) return;
      final byte[] t = hist.next();
      if(t == null) return;
      text.text(t);
      text.pos(hist.cursor());
      finish(-1);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(!hist.last());
    }
    @Override
    public String label() {
      return REDO;
    }
  }

  /** Cut command. */
  class CutCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!hist.active()) return;
      final int tc = text.getCaret();
      text.pos(tc);
      if(!copy()) return;
      text.delete();
      text.setCaret();
      finish(tc);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.selected());
    }
    @Override
    public String label() {
      return CUT;
    }
  }

  /** Copy command. */
  class CopyCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      copy();
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.selected());
    }
    @Override
    public String label() {
      return COPY;
    }
  }

  /** Paste command. */
  class PasteCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!hist.active()) return;
      final int tc = text.getCaret();
      text.pos(tc);
      final String clip = clip();
      if(clip == null) return;
      if(text.selected()) text.delete();
      text.add(clip);
      finish(tc);
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
  class DelCmd extends GUIBaseCmd {
    @Override
    public void execute(final GUI main) {
      if(!hist.active()) return;
      final int tc = text.getCaret();
      text.pos(tc);
      text.delete();
      finish(tc);
    }
    @Override
    public void refresh(final GUI main, final AbstractButton button) {
      button.setEnabled(text.selected());
    }
    @Override
    public String label() {
      return DELETE;
    }
  }

  /** Select all command. */
  class AllCmd extends GUIBaseCmd {
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
