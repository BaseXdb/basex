package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.dialog.*;
import org.basex.gui.layout.*;
import org.basex.gui.listener.*;
import org.basex.gui.text.SearchBar.*;
import org.basex.gui.text.TextEditor.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Renders and provides edit capabilities for text.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class TextPanel extends BaseXPanel {
  /** Text caret. */
  private final Timer caretTimer;

  /** Editor action. */
  public enum Action {
    /** Check for changes; do nothing if input has not changed. */
    CHECK,
    /** Enforce parsing of input. */
    PARSE,
    /** Enforce execution of input. */
    EXECUTE,
    /** Enforce testing of input. */
    TEST
  }

  /** Text editor. */
  public final TextEditor editor;
  /** Undo history. */
  public final History hist;

  /** Renderer reference. */
  private final TextRenderer rend;
  /** Scrollbar reference. */
  private final BaseXScrollBar scroll;
  /** Editable flag. */
  private final boolean editable;

  /** Search bar. */
  protected SearchBar search;
  /** Link listener. */
  private LinkListener linkListener;

  /**
   * Default constructor.
   * @param win parent window
   * @param editable editable flag
   */
  public TextPanel(final BaseXWindow win, final boolean editable) {
    this(win, "", editable);
  }

  /**
   * Default constructor.
   * @param win parent window
   * @param text initial text
   * @param editable editable flag
   */
  public TextPanel(final BaseXWindow win, final String text, final boolean editable) {
    super(win);
    this.editable = editable;
    editor = new TextEditor(gui);

    setFocusable(true);
    setFocusTraversalKeysEnabled(!editable);
    setBackground(BACK);
    setOpaque(editable);

    addMouseMotionListener(this);
    addMouseWheelListener(this);
    addComponentListener(this);
    addMouseListener(this);
    addKeyListener(this);

    addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e) {
        if(isEnabled()) caret(true);
      }
      @Override
      public void focusLost(final FocusEvent e) {
        caret(false);
        rend.caret(false);
      }
    });

    setFont(dmfont);
    layout(new BorderLayout());

    scroll = new BaseXScrollBar(this);
    rend = new TextRenderer(editor, scroll, editable, gui);

    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);

    setText(text);
    hist = new History(editable ? editor.text() : null);

    new BaseXPopup(this, editable ?
      new GUICommand[] {
        new FindCmd(), new FindNextCmd(), new FindPrevCmd(),
        new MatchCaseCmd(), new WholeWordCmd(), new RegExCmd(), new MultiLineCmd(), null,
        new GotoCmd(), null,
        new UndoCmd(), new RedoCmd(), null,
        new AllCmd(), new CutCmd(), new CopyCmd(), new PasteCmd(), new DelCmd() } :
      new GUICommand[] {
        new FindCmd(), new FindNextCmd(), new FindPrevCmd(),
        new MatchCaseCmd(), new WholeWordCmd(), new RegExCmd(), new MultiLineCmd(), null,
        new GotoCmd(), null,
        new AllCmd(), new CopyCmd() }
    );

    caretTimer = new Timer(500, e -> rend.caret(!rend.caret()));
  }

  /**
   * Sets the output text.
   * @param t output text
   */
  public void setText(final String t) {
    setText(token(t));
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
   * Returns a currently marked string if it does not extend over more than one line.
   * @return search string
   */
  public final String searchString() {
    final String string = editor.selected();
    return string.indexOf('\n') == -1 ? string : "";
  }

  /**
   * Returns the line and column of the current caret position.
   * @return line/column
   */
  public final int[] pos() {
    return rend.pos();
  }

  /**
   * Sets the output text.
   * @param text output text
   * @param size text size
   */
  public final void setText(final byte[] text, final int size) {
    byte[] txt = text;
    if(Token.contains(text, '\r')) {
      // remove carriage returns
      int ns = 0;
      for(int r = 0; r < size; ++r) {
        final byte b = text[r];
        if(b != '\r') text[ns++] = b;
      }
      // new text is different...
      txt = Arrays.copyOf(text, ns);
    } else if(text.length != size) {
      txt = Arrays.copyOf(text, size);
    }
    if(editor.text(txt)) {
      if(hist != null) hist.store(txt, editor.pos(), 0);
    }
    if(isShowing()) resizeCode.invokeLater();
  }

  /**
   * Sets a syntax highlighter, based on the file format.
   * @param file file reference
   * @param opened indicates if file was opened from disk
   */
  protected final void setSyntax(final IO file, final boolean opened) {
    setSyntax(!opened || file.hasSuffix(IO.XQSUFFIXES) ? new SyntaxXQuery() :
      file.hasSuffix(IO.JSONSUFFIX) ? new SyntaxJSON() :
      file.hasSuffix(IO.JSSUFFIXES) ? new SyntaxJS() :
      file.hasSuffix(gui.gopts.xmlSuffixes()) || file.hasSuffix(IO.HTMLSUFFIXES) ||
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
   * @param syntax syntax reference
   */
  public final void setSyntax(final Syntax syntax) {
    rend.setSyntax(syntax);
  }

  /**
   * Sets the caret to the specified position. A text selection will be removed.
   * @param pos caret position
   */
  public final void setCaret(final int pos) {
    editor.pos(pos);
    updateScrollpos.invokeLater(1);
    caret(true);
  }

  /**
   * Returns the current text cursor.
   * @return cursor position
   */
  private int getCaret() {
    return editor.pos();
  }

  /**
   * Returns the output text.
   * @return output text
   */
  public final byte[] getText() {
    return editor.text();
  }

  /**
   * Tests if text has been selected.
   * @return result of check
   */
  public final boolean selected() {
    return editor.isSelected();
  }

  @Override
  public final void setFont(final Font f) {
    super.setFont(f);
    if(rend != null) {
      rend.setFont(f);
      updateScrollbar.invokeLater(true);
    }
  }

  /**
   * Removes the error marker.
   */
  public final void resetError() {
    editor.error(-1);
    rend.repaint();
  }

  /**
   * Sets the error marker.
   * @param pos start of optional error mark
   */
  public final void error(final int pos) {
    editor.error(pos);
    rend.repaint();
  }

  /**
   * Adds or removes a comment.
   */
  public final void comment() {
    final int caret = editor.pos();
    if(editor.comment(rend.getSyntax())) hist.store(editor.text(), caret, editor.pos());
    updateScrollbar.invokeLater(true);
  }

  /**
   * Case conversion.
   * @param cs case type
   */
  public final void toCase(final Case cs) {
    final int caret = editor.pos();
    if(editor.toCase(cs)) hist.store(editor.text(), caret, editor.pos());
    updateScrollbar.invokeLater(true);
  }

  /**
   * Jumps to a matching bracket.
   */
  public final void bracket() {
    setCaret(editor.bracket());
  }

  /**
   * Sorts text.
   */
  public final void sort() {
    final int caret = editor.pos();
    final DialogSort ds = new DialogSort(gui);
    if(!ds.ok() || !editor.sort()) return;

    hist.store(editor.text(), caret, editor.pos());
    updateScrollbar.invokeLater(true);
    repaint();
  }

  /**
   * Formats the selected text.
   */
  public final void format() {
    final int caret = editor.pos();
    if(editor.format(rend.getSyntax())) hist.store(editor.text(), caret, editor.pos());
    updateScrollbar.invokeLater(true);
  }

  @Override
  public final void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
    rend.setEnabled(enabled);
    scroll.setEnabled(enabled);
    caret(enabled);
  }

  /**
   * Selects the whole text.
   */
  private void selectAll() {
    editor.selectAll();
    rend.repaint();
  }

  // SEARCH OPERATIONS ============================================================================

  /**
   * Installs a link listener.
   * @param ll link listener
   */
  public final void setLinkListener(final LinkListener ll) {
    linkListener = ll;
  }

  /**
   * Installs a search bar.
   * @param s search bar
   */
  final void setSearch(final SearchBar s) {
    search = s;
  }

  /**
   * Returns the search bar.
   * @return search bar
   */
  public final SearchBar getSearch() {
    return search;
  }

  /**
   * Performs a search.
   * @param sc search context
   * @param jump jump to next hit
   */
  final void search(final SearchContext sc, final boolean jump) {
    rend.search(sc, jump);
  }

  /**
   * Replaces the text.
   * @param rc replace context
   */
  final void replace(final ReplaceContext rc) {
    try {
      final int[] select = rend.replace(rc);
      if(rc.text != null) {
        final boolean sel = editor.isSelected();
        setText(rc.text);
        editor.select(select[0], select[sel ? 1 : 0]);
        release(Action.CHECK);
      }
      gui.status.setText(Text.STRINGS_REPLACED);
    } catch(final Exception ex) {
      final String msg = Util.message(ex).replaceAll(Prop.NL + ".*", "");
      gui.status.setError(Text.REGULAR_EXPR + Text.COLS + msg);
    }
  }

  /**
   * Jumps to the current, next or previous search string.
   * @param dir search direction
   * @param select select hit
   */
  protected final void jump(final SearchDir dir, final boolean select) {
    SwingUtilities.invokeLater(() -> scroll(rend.jump(dir, select), 1));
  }

  // MOUSE INTERACTIONS ===========================================================================

  @Override
  public final void mouseEntered(final MouseEvent e) {
    gui.cursor(CURSORTEXT);
  }

  @Override
  public final void mouseExited(final MouseEvent e) {
    gui.cursor(CURSORARROW);
  }

  @Override
  public final void mouseMoved(final MouseEvent e) {
    if(linkListener == null) return;
    final TextIterator iter = rend.jump(e.getPoint());
    gui.cursor(iter.link() != null ? CURSORHAND : CURSORARROW);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if(linkListener == null) return;

    if(SwingUtilities.isLeftMouseButton(e)) {
      editor.endSelection();
      // evaluate link
      if(!editor.isSelected()) {
        final TextIterator iter = rend.jump(e.getPoint());
        final String link = iter.link();
        if(link != null) linkListener.linkClicked(link);
      }
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
    select(e.getPoint(), false);
    final int y = Math.max(20, Math.min(e.getY(), getHeight() - 20));
    if(y != e.getY()) scroll.pos(scroll.pos() + e.getY() - y);
  }

  @Override
  public final void mousePressed(final MouseEvent e) {
    if(!isEnabled() || !isFocusable()) return;

    requestFocusInWindow();
    caret(true);

    if(SwingUtilities.isMiddleMouseButton(e)) copy();

    final boolean shift = e.isShiftDown();
    final boolean selected = editor.isSelected();
    if(SwingUtilities.isLeftMouseButton(e)) {
      final int c = e.getClickCount();
      if(c == 1) {
        // selection mode
        if(shift) editor.startSelection(true);
        select(e.getPoint(), !shift);
      } else if(c == 2) {
        editor.selectWord();
      } else {
        editor.selectLine();
      }
    } else if(!selected) {
      select(e.getPoint(), true);
    }
  }

  /**
   * Selects the text at the specified position.
   * @param point mouse position
   * @param start states if selection has just been started
   */
  private void select(final Point point, final boolean start) {
    editor.select(rend.jump(point).pos(), start);
    rend.repaint();
  }

  // KEY INTERACTIONS ===========================================================================

  /**
   * Invokes special keys.
   * @param e key event
   * @return {@code true} if special key was processed
   */
  private boolean specialKey(final KeyEvent e) {
    if(PREVTAB.is(e)) {
      gui.editor.tab(false);
    } else if(NEXTTAB.is(e)) {
      gui.editor.tab(true);
    } else if(CLOSETAB.is(e)) {
      gui.editor.close(null);
    } else if(search != null && ESCAPE.is(e)) {
      search.deactivate(true);
    } else {
      return false;
    }
    e.consume();
    return true;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    // ignore modifier keys
    if(specialKey(e) || modifier(e)) return;

    // re-animate cursor
    caret(true);

    // operations without cursor movement...
    final int fh = rend.fontHeight();
    if(SCROLLDOWN.is(e)) {
      scroll.pos(scroll.pos() + fh);
      return;
    }
    if(SCROLLUP.is(e)) {
      scroll.pos(scroll.pos() - fh);
      return;
    }

    // set cursor position
    final boolean selected = editor.isSelected();
    final int pos = editor.pos();

    final boolean shift = e.isShiftDown();
    boolean down = true, consumed = true;

    // move caret
    int lc = Integer.MIN_VALUE;
    final byte[] txt = editor.text();
    if(NEXTWORD.is(e)) {
      editor.nextWord(shift);
    } else if(PREVWORD.is(e)) {
      editor.prevWord(shift);
      down = false;
    } else if(TEXTSTART.is(e)) {
      editor.textStart(shift);
      down = false;
    } else if(TEXTEND.is(e)) {
      editor.textEnd(shift);
    } else if(LINESTART.is(e)) {
      editor.lineStart(shift);
      down = false;
    } else if(LINEEND.is(e)) {
      editor.lineEnd(shift);
    } else if(PREVPAGE_RO.is(e) && !hist.active()) {
      lc = editor.linesUp(getHeight() / fh, false, lastCol);
      down = false;
    } else if(NEXTPAGE_RO.is(e) && !hist.active()) {
      lc = editor.linesDown(getHeight() / fh, false, lastCol);
    } else if(PREVPAGE.is(e) && !sc(e)) {
      lc = editor.linesUp(getHeight() / fh, shift, lastCol);
      down = false;
    } else if(NEXTPAGE.is(e) && !sc(e)) {
      lc = editor.linesDown(getHeight() / fh, shift, lastCol);
    } else if(NEXTLINE.is(e) && !MOVEDOWN.is(e)) {
      lc = editor.linesDown(1, shift, lastCol);
    } else if(PREVLINE.is(e) && !MOVEUP.is(e)) {
      lc = editor.linesUp(1, shift, lastCol);
      down = false;
    } else if(NEXTCHAR.is(e)) {
      editor.next(shift);
    } else if(PREVCHAR.is(e)) {
      editor.previous(shift);
      down = false;
    } else {
      consumed = false;
    }
    lastCol = lc == Integer.MIN_VALUE ? -1 : lc;

    // edit text
    if(hist.active()) {
      if(COMPLETE.is(e)) {
        complete();
        return;
      }

      if(MOVEDOWN.is(e)) {
        editor.move(true);
      } else if(MOVEUP.is(e)) {
        editor.move(false);
      } else if(DUPLLINES.is(e)) {
        editor.duplLines();
      } else if(DELLINES.is(e)) {
        editor.deleteLines();
      } else if(DELNEXTWORD.is(e)) {
        editor.deleteNext(true);
      } else if(DELLINEEND.is(e)) {
        editor.deleteNext(false);
      } else if(DELNEXT.is(e)) {
        editor.delete();
      } else if(DELPREVWORD.is(e)) {
        editor.deletePrev(true);
        down = false;
      } else if(DELLINESTART.is(e)) {
        editor.deletePrev(false);
        down = false;
      } else if(DELPREV.is(e)) {
        editor.deletePrev();
        down = false;
      } else {
        consumed = false;
      }
    }
    if(consumed) e.consume();

    final byte[] tmp = editor.text();
    if(txt != tmp) {
      // text has changed: add old text to history
      hist.store(tmp, pos, editor.pos());
      updateScrollbar.invokeLater(down);
    } else if(pos != editor.pos() || selected != editor.isSelected()) {
      // cursor position or selection state has changed
      updateScrollpos.invokeLater(down ? 2 : 0);
    }
  }

  /** Updates size and position of the scroll bar. */
  private final GUICode updateScrollbar = new GUICode() {
    @Override
    public void execute(final Object down) {
      rend.updateScrollbar();
      updateScrollpos.execute((Boolean) down ? 2 : 0);
    }
  };

  /** Updates the position of the scroll bar. */
  private final GUICode updateScrollpos = new GUICode() {
    @Override
    public void execute(final Object align) {
      scroll(rend.cursorY(), (Integer) align);
    }
  };

  /**
   * Scrolls to the specified position.
   * @param y new vertical position
   * @param align alignment (0: scroll up, 1: jump, 2: scroll down)
   */
  private void scroll(final int y, final int align) {
    if(y != -1) {
      final int h = getHeight(), m = y + (rend.fontHeight() << 1) - h, p = scroll.pos();
      if(p < m || p > y) {
        scroll.pos(align == 0 ? y : align == 1 ? y - h / 2 : m);
      }
    }
    rend.repaint();
  }

  /** Last horizontal position. */
  private int lastCol = -1;

  @Override
  public void keyTyped(final KeyEvent e) {
    if(!hist.active() || control(e) || DELNEXT.is(e) || DELPREV.is(e) || ESCAPE.is(e) || CUT2.is(e))
      return;

    final int caret = editor.pos();

    // remember if marked text is to be deleted
    final StringBuilder sb = new StringBuilder(1).append(e.getKeyChar());
    final boolean indent = TAB.is(e) && editor.indent(sb, e.isShiftDown());

    // delete marked text
    final boolean selected = editor.isSelected() && !indent;
    if(selected) editor.delete();

    final int move = ENTER.is(e) ? editor.enter(sb) : editor.add(sb, selected);

    // refresh history and adjust cursor position
    hist.store(editor.text(), caret, editor.pos());
    if(move != 0) editor.pos(Math.min(editor.size(), caret + move));

    // adjust text height
    updateScrollbar.invokeLater(true);
    e.consume();
  }

  /**
   * Releases a key or mouse. Can be overwritten to react on events.
   * @param action action
   */
  @SuppressWarnings("unused")
  protected void release(final Action action) { }

  /**
   * Refreshes the layout.
   * @param f used font
   */
  public final void refreshLayout(final Font f) {
    setFont(f);
    scroll.refreshLayout();
  }

  // EDITOR COMMANDS ==============================================================================

  /**
   * Pastes a string.
   * @param string string to be pasted
   */
  private void paste(final String string) {
    final int pos = editor.pos();
    if(editor.isSelected()) editor.delete();
    editor.insert(string);
    finish(pos);
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  private boolean copy() {
    final String txt = editor.selected();
    if(txt.isEmpty()) return false;

    // copy selection to clipboard
    BaseXLayout.copy(txt);
    return true;
  }

  /**
   * Returns the clipboard text.
   * @return text or {@code null}
   */
  private static String clip() {
    // copy selection to clipboard
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable tr = clip.getContents(null);
    if(tr != null) {
      final ArrayList<Object> contents = BaseXLayout.contents(tr);
      if(!contents.isEmpty()) return contents.get(0).toString();
    } else {
      Util.debug("Clipboard has no contents.");
    }
    return null;
  }

  /**
   * Finishes a command.
   * @param old old cursor position; store entry to history if position != -1
   */
  private void finish(final int old) {
    if(old != -1) hist.store(editor.text(), old, editor.pos());
    updateScrollbar.invokeLater(true);
    release(Action.CHECK);
  }

  /**
   * Stops an old text cursor thread and, if requested, starts a new one.
   * @param start start/stop flag
   */
  private void caret(final boolean start) {
    caretTimer.stop();
    if(start) caretTimer.start();
    rend.caret(start);
  }

  @Override
  public final void mouseWheelMoved(final MouseWheelEvent e) {
    scroll.pos(scroll.pos() + e.getUnitsToScroll() * 20);
    rend.repaint();
  }

  /** Calculation counter. */
  private final GUICode resizeCode = new GUICode() {
    @Override
    public void execute(final Object arg) {
      rend.updateScrollbar();
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
  private class UndoCmd extends GUIPopupCmd {
    /** Constructor. */
    UndoCmd() { super(Text.UNDO, UNDOSTEP); }

    @Override
    public void execute() {
      if(!hist.active()) return;
      final byte[] t = hist.prev();
      if(t == null) return;
      editor.text(t);
      editor.pos(hist.caret());
      finish(-1);
    }
    @Override
    public boolean enabled(final GUI main) { return !hist.first(); }
  }

  /** Redo command. */
  private class RedoCmd extends GUIPopupCmd {
    /** Constructor. */
    RedoCmd() { super(Text.REDO, REDOSTEP); }

    @Override
    public void execute() {
      if(!hist.active()) return;
      final byte[] t = hist.next();
      if(t == null) return;
      editor.text(t);
      editor.pos(hist.caret());
      finish(-1);
    }
    @Override
    public boolean enabled(final GUI main) { return !hist.last(); }
  }

  /** Cut command. */
  private class CutCmd extends GUIPopupCmd {
    /** Constructor. */
    CutCmd() { super(Text.CUT, CUT1, CUT2); }

    @Override
    public void execute() {
      final int pos = editor.pos();
      if(!copy()) return;
      editor.delete();
      finish(pos);
    }
    @Override
    public boolean enabled(final GUI main) { return hist.active() && editor.isSelected(); }
  }

  /** Copy command. */
  private class CopyCmd extends GUIPopupCmd {
    /** Constructor. */
    CopyCmd() { super(Text.COPY, COPY1, COPY2); }

    @Override
    public void execute() { copy(); }
    @Override
    public boolean enabled(final GUI main) { return editor.isSelected(); }
  }

  /** Paste command. */
  private class PasteCmd extends GUIPopupCmd {
    /** Constructor. */
    PasteCmd() { super(Text.PASTE, PASTE1, PASTE2); }

    @Override
    public void execute() {
      final String clip = clip();
      if(clip != null) paste(clip);
    }
    @Override
    public boolean enabled(final GUI main) { return hist.active() && clip() != null; }
  }

  /** Delete command. */
  private class DelCmd extends GUIPopupCmd {
    /** Constructor. */
    DelCmd() { super(Text.DELETE, DELNEXT); }

    @Override
    public void execute() {
      final int pos = editor.pos();
      editor.delete();
      finish(pos);
    }
    @Override
    public boolean enabled(final GUI main) { return hist.active() && editor.isSelected(); }
  }

  /** Select all command. */
  private class AllCmd extends GUIPopupCmd {
    /** Constructor. */
    AllCmd() { super(Text.SELECT_ALL, SELECTALL); }

    @Override
    public void execute() { selectAll(); }
  }

  /** Find next hit. */
  private class FindCmd extends GUIPopupCmd {
    /** Constructor. */
    FindCmd() { super(Text.FIND + Text.DOTS, FIND); }

    @Override
    public void execute() { search.activate(searchString(), true, false); }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
  }

  /** Find next hit. */
  private class FindNextCmd extends GUIPopupCmd {
    /** Constructor. */
    FindNextCmd() { super(Text.FIND_NEXT, FINDNEXT1, FINDNEXT2); }

    @Override
    public void execute() { search(true); }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
  }

  /** Find previous hit. */
  private class FindPrevCmd extends GUIPopupCmd {
    /** Constructor. */
    FindPrevCmd() { super(Text.FIND_PREVIOUS, FINDPREV1, FINDPREV2); }

    @Override
    public void execute() { search(false); }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
  }

  /** Match-case search. */
  private class MatchCaseCmd extends GUIPopupCmd {
    /** Constructor. */
    MatchCaseCmd() { super(Text.MATCH_CASE, MATCHCASE); }

    @Override
    public void execute() { search.toggle(search.mcase); }
    @Override
    public boolean toggle() { return true; }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
    @Override
    public boolean selected(final GUI main) { return search.mcase.isSelected(); }
  }

  /** Whole-word search. */
  private class WholeWordCmd extends GUIPopupCmd {
    /** Constructor. */
    WholeWordCmd() { super(Text.WHOLE_WORD, WHOLEWORD); }

    @Override
    public void execute() { search.toggle(search.word); }
    @Override
    public boolean toggle() { return true; }
    @Override
    public boolean enabled(final GUI main) { return search != null && search.word.isEnabled(); }
    @Override
    public boolean selected(final GUI main) { return search.word.isSelected(); }
  }

  /** Regular-expression search. */
  private class RegExCmd extends GUIPopupCmd {
    /** Constructor. */
    RegExCmd() { super(Text.REGULAR_EXPR, REGEX); }

    @Override
    public void execute() {
      search.toggle(search.regex); }
    @Override
    public boolean toggle() { return true; }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
    @Override
    public boolean selected(final GUI main) { return search.regex.isSelected(); }
  }

  /** Multi-line search. */
  private class MultiLineCmd extends GUIPopupCmd {
    /** Constructor. */
    MultiLineCmd() { super(Text.MULTI_LINE, MULTILINE); }

    @Override
    public void execute() { search.toggle(search.multi); }
    @Override
    public boolean toggle() { return true; }
    @Override
    public boolean enabled(final GUI main) { return search != null && search.multi.isEnabled(); }
    @Override
    public boolean selected(final GUI main) { return search.multi.isSelected(); }
  }

  /**
   * Highlights the next/previous hit.
   * @param next next/previous hit
   */
  private void search(final boolean next) {
    final boolean vis = search.isVisible();
    search.activate(searchString(), false, false);
    jump(vis ? next ? SearchDir.FORWARD : SearchDir.BACKWARD : SearchDir.CURRENT, true);
  }

  /** Go to line. */
  private class GotoCmd extends GUIPopupCmd {
    /** Constructor. */
    GotoCmd() { super(Text.GO_TO_LINE + Text.DOTS, GOTOLINE); }

    @Override
    public void execute() { gotoLine(); }
    @Override
    public boolean enabled(final GUI main) { return search != null; }
  }

  /**
   * Jumps to a specific line.
   */
  private void gotoLine() {
    final byte[] last = editor.text();
    final int ll = last.length;
    final int cr = getCaret();
    int l = 1;
    for(int e = 0; e < ll && e < cr; e += cl(last, e)) {
      if(last[e] == '\n') ++l;
    }
    final DialogLine dl = new DialogLine(gui, l);
    if(!dl.ok()) return;
    final int el = dl.line();
    l = 1;
    int p = 0;
    for(int e = 0; e < ll && l < el; e += cl(last, e)) {
      if(last[e] != '\n') continue;
      p = e + 1;
      ++l;
    }
    setCaret(p);
    gui.editor.posCode.invokeLater();
  }

  /**
   * Code completion.
   */
  private void complete() {
    if(selected()) return;

    // find first character
    final int caret = editor.pos(), start = editor.completionStart();
    final String input = string(substring(editor.text(), start, caret)).toLowerCase(Locale.ENGLISH);

    // find insertion candidates
    final ArrayList<Pair<String, String>> pairs = new ArrayList<>();
    final int il = LISTS.size();
    for(int i = 0; i < il; i++) {
      if(i > 0) pairs.add(null);
      for(final Pair<String, String> pair : LISTS.get(i)) {
        final String name = pair.name();
        if(name.startsWith(input) || name.replace(":", "").startsWith(input)) pairs.add(pair);
      }
    }
    if(pairs.size() < il) {
      pairs.clear();
      for(int i = 0; i < il; i++) {
        if(i > 0) pairs.add(null);
        for(final Pair<String, String> pair : LISTS.get(i)) {
          if(SmartStrings.matches(pair.name(), input)) pairs.add(pair);
        }
      }
    }

    // remove duplicate and trailing separators
    for(int l = 0; l < pairs.size();) {
      if(pairs.get(l) == null && (l == 0 || l + 1 == pairs.size() || pairs.get(l + 1) == null)) {
        pairs.remove(l);
      } else {
        l++;
      }
    }

    if(pairs.size() == 1) {
      // insert single candidate
      complete(pairs.get(0).value(), start);
    } else if(!pairs.isEmpty()) {
      // show popup menu
      final JPopupMenu pm = new JPopupMenu();
      final ActionListener al = ae -> complete(
        ae.getActionCommand().replaceAll("^.*?] ", ""), start);
      for(final Pair<String, String> entry : pairs) {
        if(entry == null) {
          pm.addSeparator();
        } else {
          final JMenuItem mi = new JMenuItem(entry.value());
          pm.add(mi);
          mi.addActionListener(al);
        }
        if(pm.getComponentCount() >= 20) {
          final JMenuItem mi = new JMenuItem("... " + Util.info(Text.RESULTS_X, pairs.size()));
          mi.setEnabled(false);
          pm.add(mi);
          break;
        }
      }

      final int[] cursor = rend.cursor();
      pm.show(this, cursor[0], cursor[1]);

      // highlight first entry
      final MenuElement[] me = { pm, (JMenuItem) pm.getComponent(0) };
      MenuSelectionManager.defaultManager().setSelectedPath(me);
    }
  }

  /**
   * Auto-completes a string at the specified position.
   * @param string string
   * @param start start position
   */
  private void complete(final String string, final int start) {
    final int pos = editor.pos();
    editor.complete(string, start);
    finish(pos);
  }

  /** Replacement lists. */
  private static final ArrayList<ArrayList<Pair<String, String>>> LISTS = new ArrayList<>();

  /* Reads in the property file. */
  static {
    for(int i = 0; i < 5; i++) LISTS.add(new ArrayList<>());
    final String file = "/completions.properties";
    final InputStream is = TextPanel.class.getResourceAsStream(file);
    if(is == null) {
      Util.errln(file + " not found.");
    } else {
      // add custom completions
      try(NewlineInput nli = new NewlineInput(is)) {
        for(String line; (line = nli.readLine()) != null;) {
          final int i = line.indexOf('=');
          if(i == -1 || Strings.startsWith(line, '#')) continue;
          LISTS.get(0).add(new Pair<>(line.substring(0, i),
              line.substring(i + 1).replace("\\n", "\n")));
        }
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    }
    // add functions (default functions first)
    for(final FuncDefinition fd : Functions.DEFINITIONS) {
      final String func = fd.toString();
      final String name = func.replaceAll("^fn:|\\(.*", "");
      final String value = name + (func.contains("()") ? "()" : "(_)");
      if(fd.uri() == QueryText.FN_URI) {
        if(name.contains("-")) LISTS.get(1).add(
            new Pair<>(name.replaceAll("(.)[^-A-Z]*-?", "$1").
            toLowerCase(Locale.ENGLISH), value));
        LISTS.get(2).add(new Pair<>(name.toLowerCase(Locale.ENGLISH), value));
      } else {
        LISTS.get(3).add(new Pair<>(name.replaceAll("(:?.)[^-:A-Z]*-?", "$1").
            toLowerCase(Locale.ENGLISH), value));
        LISTS.get(4).add(new Pair<>(name.toLowerCase(Locale.ENGLISH), value));
      }
    }
  }
}
