package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;

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
import org.basex.util.*;

/**
 * Renders and provides edit capabilities for text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class TextPanel extends BaseXPanel {
  /** Vertical alignment of the caret after scrolling. */
  private enum Align {
    /** Caret at the top. */ TOP,
    /** Caret in the middle. */ CENTER,
    /** Caret at the bottom. */ BOTTOM
  }

  /** Text editor. */
  public final TextEditor editor;
  /** Undo history. */
  public final History hist;

  /** Code completion popup. */
  private final CompletionPopup completion;
  /** Proposals of the current code completion, ordered by relevance (initially empty). */
  private ArrayList<ArrayList<Completion>> proposals = new ArrayList<>();
  /** Popup with the signature of the current function call. */
  private final SignaturePopup signature;
  /** Name of the call with the resolved signature (can be {@code null}: none). */
  private String signatureName;
  /** Resolved signature (can be {@code null}: the called function is unknown). */
  private Signature signatureValue;

  /** Text caret. */
  private final Timer caretTimer;
  /** Renderer reference. */
  private final TextRenderer rend;
  /** Scrollbar reference. */
  private final BaseXScrollBar scroll;
  /** Horizontal scrollbar reference. */
  private final BaseXScrollBar hscroll;
  /** Panel with the horizontal scrollbar (hidden if long lines are wrapped). */
  private final BaseXBack hpanel;
  /** Editable flag. */
  private final boolean editable;

  /** Search bar (can be {@code null}). */
  protected SearchBar search;
  /** Link listener (can be {@code null}). */
  private LinkListener linkListener;
  /** Edit listener (can be {@code null}). */
  private EditListener editListener;

  /** Indicates if the last key press was processed by the completion popup. */
  private boolean completed;
  /** Last number of mouse clicks. */
  private int clicks;
  /** Last horizontal position. */
  private int lastX = -1;

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
    final EditorOptions opts = new EditorOptions(gui.gopts);
    editor = new TextEditor(opts);
    completion = new CompletionPopup(this);
    signature = new SignaturePopup(this);

    setFocusable(true);
    setFocusTraversalKeysEnabled(!editable);
    setBackground(backColor);
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
        completion.hide();
        signature.hide();
      }
    });

    setFont(dmfont);
    layout(new BorderLayout());

    scroll = new BaseXScrollBar(this);
    hscroll = new BaseXScrollBar(this, true);

    // the filler keeps the horizontal scrollbar clear of the vertical one
    final BaseXBack filler = new BaseXBack();
    BaseXLayout.setWidth(filler, scroll.getPreferredSize().width);
    hpanel = new BaseXBack().layout(new BorderLayout());
    hpanel.add(hscroll, BorderLayout.CENTER);
    hpanel.add(filler, BorderLayout.EAST);

    rend = new TextRenderer(editor, scroll, hscroll, editable, opts);
    hpanel.setVisible(!rend.wrap());

    add(rend, BorderLayout.CENTER);
    add(scroll, BorderLayout.EAST);
    add(hpanel, BorderLayout.SOUTH);

    hist = new History(editable ? EMPTY : null);
    setText(text);
    // the initial text is no undoable change
    if(editable) hist.init(editor.text());

    final ArrayList<GUICommand> cmds = new ArrayList<>(Arrays.asList(
      new FindCmd(), new FindHitCmd(true), new FindHitCmd(false),
      new ToggleCmd(Text.MATCH_CASE, MATCHCASE, sb -> sb.mcase),
      new ToggleCmd(Text.WHOLE_WORD, WHOLEWORD, sb -> sb.word),
      new ToggleCmd(Text.REGULAR_EXPR, REGEX, sb -> sb.regex),
      new ToggleCmd(Text.DOT_ALL, DOTALL, sb -> sb.dotall), null));
    if(editable) {
      cmds.addAll(Arrays.asList(new HistoryCmd(true), new HistoryCmd(false), null,
        new AllCmd(), new CutCmd(), new CopyCmd(), new PasteCmd(), new DelCmd()));
    } else {
      cmds.addAll(Arrays.asList(new AllCmd(), new CopyCmd()));
    }
    new BaseXPopup(this, cmds.toArray(GUICommand[]::new));

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
  public final int[] caretPos() {
    return rend.caretPos();
  }

  /**
   * Sets the output text.
   * @param text output text
   * @param size text size
   */
  public final void setText(final byte[] text, final int size) {
    byte[] txt = text.length == size ? text : Arrays.copyOf(text, size);
    // remove carriage returns
    txt = Token.replace(txt, new byte[] { '\r' }, Token.EMPTY);
    if(editor.text(txt)) hist.store(txt, editor.pos(), 0);
    resetError();
    updateCode.invokeLater();
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
    rend.syntax(syntax);
    updateCode.invokeLater();
  }

  /**
   * Sets the caret to the specified position. A text selection will be removed.
   * @param pos caret position
   */
  public final void setCaret(final int pos) {
    editor.pos(pos);
    updateCode.invokeLater(Align.CENTER);
    caret(true);
  }

  /**
   * Returns the caret position.
   * @return caret position
   */
  public final int getCaret() {
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
      // a visibility change of the scrollbar must be propagated to the layout
      hpanel.setVisible(!rend.wrap());
      revalidate();
      updateCode.invokeLater(Align.BOTTOM);
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
    finish(caret, editor.comment(rend.syntax()));
  }

  /**
   * Case conversion.
   * @param cs case type
   */
  public final void toCase(final Case cs) {
    final int caret = editor.pos();
    finish(caret, editor.toCase(cs));
  }

  /**
   * Jumps to a matching bracket.
   */
  public final void bracket() {
    setCaret(editor.bracket(rend.syntax()));
  }

  /**
   * Sorts text.
   */
  public final void sort() {
    final int caret = editor.pos();
    final DialogSort ds = new DialogSort(gui);
    finish(caret, ds.ok() && editor.sort());
  }

  /**
   * Removes trailing whitespace and appends a final newline.
   * @param trim remove trailing whitespace
   * @param nl append a final newline
   * @return {@code true} if text has changed
   */
  public final boolean tidy(final boolean trim, final boolean nl) {
    final int caret = editor.pos();
    if(!editor.tidy(trim, nl)) return false;
    // no edit notification: the tidied text is written to disk right afterwards
    hist.store(editor.text(), caret, editor.pos());
    resetError();
    updateCode.invokeLater(Align.CENTER);
    caret(true);
    return true;
  }

  /**
   * Formats the selected text.
   */
  public final void format() {
    final int caret = editor.pos();
    finish(caret, editor.format(rend.syntax()));
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
   * Installs an edit listener.
   * @param el edit listener
   */
  public final void setEditListener(final EditListener el) {
    editListener = el;
  }

  /**
   * Notifies the edit listener that the text was changed by an edit command.
   */
  private void edited() {
    if(editListener != null) editListener.edited();
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
   * @return search bar, or {@code null}
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
    editor.search(sc, jump);
  }

  /**
   * Replaces the text. May throw if the replacement references a non-existent group.
   * @param rc replace context
   */
  final void replace(final ReplaceContext rc) {
    final int[] range = editor.replace(rc);
    if(rc.text != null) {
      // a replacement in a selection reselects its new range; otherwise, the caret is preserved
      setText(rc.text);
      if(range != null) editor.select(range[0], range[1]);
      else setCaret(rc.caret);
      edited();
    }
  }

  /**
   * Replaces the current search hit and moves the caret behind the replacement.
   * May throw if the replacement references a non-existent group.
   * @param rc replace context
   * @return {@code true} if a hit was replaced
   */
  final boolean replaceNext(final ReplaceContext rc) {
    final int[] select = editor.replaceHit(rc);
    if(select == null) return false;
    setText(rc.text);
    setCaret(select[1]);
    edited();
    return true;
  }

  /**
   * Updates the search-hit markers of the scroll bar.
   */
  final void marks() {
    rend.marks();
  }

  /**
   * Jumps to the current, next or previous search string.
   * @param dir search direction
   * @param select select hit
   */
  protected final void jump(final SearchDir dir, final boolean select) {
    SwingUtilities.invokeLater(() -> {
      scroll(rend.jump(dir, select), Align.CENTER);
      if(search != null) search.refreshCount();
    });
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
    if(!SwingUtilities.isLeftMouseButton(e) || linkListener == null) return;

    editor.endSelection();
    // evaluate link
    if(!editor.isSelected()) {
      final TextIterator iter = rend.jump(e.getPoint());
      final String link = iter.link();
      if(link != null) linkListener.linkClicked(link);
    }
  }

  @Override
  public final void mouseDragged(final MouseEvent e) {
    if(SwingUtilities.isLeftMouseButton(e) && clicks == 1) {
      select(e.getPoint(), false);
      final int y = Math.max(20, Math.min(e.getY(), getHeight() - 20));
      if(y != e.getY()) scroll.pos(scroll.pos() + e.getY() - y);
      final int x = Math.max(20, Math.min(e.getX(), getWidth() - 20));
      if(x != e.getX()) rend.moveX(e.getX() - x);
    }
  }

  @Override
  public final void mousePressed(final MouseEvent e) {
    signature.hide();
    // copy and paste text with middle mouse button (Unix only)
    if(SwingUtilities.isMiddleMouseButton(e)) {
      if(!Prop.WIN && !Prop.MAC) {
        if(editor.isSelected()) {
          copy();
          editor.resetSelection();
          rend.repaint();
        } else if(editable && isEnabled()) {
          final ArrayList<Object> clips = BaseXLayout.fromClipboard(null);
          if(!clips.isEmpty()) paste(clips.getFirst().toString());
        }
      }
      return;
    }

    if(!isEnabled() || !isFocusable()) return;

    completion.hide();
    requestFocusInWindow();
    caret(true);

    final boolean shift = e.isShiftDown(), selected = editor.isSelected();
    if(SwingUtilities.isLeftMouseButton(e)) {
      clicks = e.getClickCount();
      if(clicks == 1) {
        // selection mode
        if(shift) editor.startSelection(true);
        select(e.getPoint(), !shift);
      } else if(clicks == 2) {
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
    final int p = rend.jump(point).pos();
    if(start) editor.selectFrom(p);
    else editor.selectTo(p);
    editor.atRowEnd(rend.rowEnd());
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
    completion.hide();
    signature.hide();
    return true;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    // navigate in the completion popup
    completed = false;
    if(completion.visible() && completion.key(e)) {
      completed = true;
      e.consume();
      return;
    }

    // ignore modifier keys
    if(specialKey(e) || modifier(e)) {
      e.consume();
      return;
    }

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
    boolean down = true, moved = true;

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
      moveRow(false, shift);
      down = false;
    } else if(LINEEND.is(e)) {
      moveRow(true, shift);
    } else if(PREVPAGE_RO.is(e) && !hist.active()) {
      lc = moveCaret(-(getHeight() / fh), false);
      down = false;
    } else if(NEXTPAGE_RO.is(e) && !hist.active()) {
      lc = moveCaret(getHeight() / fh, false);
    } else if(PREVPAGE.is(e) && !sc(e)) {
      lc = moveCaret(-(getHeight() / fh), shift);
      down = false;
    } else if(NEXTPAGE.is(e) && !sc(e)) {
      lc = moveCaret(getHeight() / fh, shift);
    } else if(NEXTLINE.is(e) && !MOVEDOWN.is(e)) {
      lc = moveCaret(1, shift);
    } else if(PREVLINE.is(e) && !MOVEUP.is(e)) {
      lc = moveCaret(-1, shift);
      down = false;
    } else if(NEXTCHAR.is(e)) {
      editor.nextChar(shift);
    } else if(PREVCHAR.is(e)) {
      editor.prevChar(shift);
      down = false;
    } else {
      moved = false;
    }
    lastX = lc == Integer.MIN_VALUE ? -1 : lc;

    // edit text
    boolean edited = false;
    if(hist.active()) {
      if(COMPLETE.is(e)) {
        complete(true);
        return;
      }

      edited = true;
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
        edited = false;
      }
    }
    if(moved || edited) e.consume();

    final byte[] tmp = editor.text();
    final boolean changed = txt != tmp;
    // text has changed: add old text to history
    if(changed) hist.store(tmp, pos, editor.pos());
    // text, cursor position or selection state has changed
    if(changed || pos != editor.pos() || selected != editor.isSelected()) {
      updateCode.invokeLater(down ? Align.BOTTOM : Align.TOP);
    }
    // refresh completions, or show them after a delay if the cursor was moved
    if(hist.active() && (moved || edited)) {
      refreshCompletion(true);
      signatureCode.invokeLater(false);
    } else if(control(e) || e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
      // close the popups: the key press will be processed as a shortcut
      completion.hide();
      signature.hide();
    }
  }

  /**
   * Moves the caret up or down by the specified number of rendered rows.
   * @param count number of rows (negative: upwards)
   * @param select selection flag
   * @return new horizontal position, or {@code -1} if the text has not been rendered yet
   */
  private int moveCaret(final int count, final boolean select) {
    final int[] caret = rend.caretRows(count, lastX);
    // no rendered text: fall back to logical lines
    if(caret == null) {
      editor.lines(count, select);
      return -1;
    }
    editor.moveTo(caret[0], select);
    editor.atRowEnd(rend.rowEnd());
    return caret[1];
  }

  /**
   * Moves the caret to the beginning or end of the rendered row.
   * @param end end of row
   * @param select selection flag
   */
  private void moveRow(final boolean end, final boolean select) {
    final int p = rend.caretRow(end);
    // no rendered text: fall back to logical lines
    if(p == -1) {
      if(end) editor.lineEnd(select);
      else editor.lineStart(select);
      return;
    }
    if(end) editor.rowEnd(p, select);
    else editor.rowStart(p, select);
    editor.atRowEnd(rend.rowEnd());
  }

  /** Recomputes the text height and adjusts the scroll bar ({@code null}: keep the position). */
  private final GUICode<Align> updateCode = new GUICode<>() {
    @Override
    public void execute(final Align align) {
      if(!isShowing()) return;
      rend.computeHeight();
      if(align != null) {
        scroll(rend.cursorY(), align);
      } else {
        // keep the scroll positions within the valid range
        scroll.pos(scroll.pos());
        hscroll.pos(hscroll.pos());
        rend.repaint();
      }
    }
  };

  /** Refreshes the signature popup ({@code true}: show the signature of a new call). */
  private final GUICode<Boolean> signatureCode = new GUICode<>() {
    @Override
    public void execute(final Boolean show) {
      refreshSignature(show);
    }
  };

  /**
   * Scrolls to the specified position.
   * @param y new vertical position
   * @param align vertical alignment of the caret
   */
  private void scroll(final int y, final Align align) {
    if(y != -1) {
      final int h = getHeight(), m = y + (rend.fontHeight() << 1) - h, p = scroll.pos();
      if(p < m || p > y) {
        scroll.pos(switch(align) {
          case TOP -> y;
          case CENTER -> y - h / 2;
          case BOTTOM -> m;
        });
      }
    }
    rend.scrollX();
    rend.repaint();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    // the key press was consumed by the completion popup
    if(completed) {
      e.consume();
      return;
    }
    if(!hist.active() || control(e) || DELNEXT.is(e) || DELPREV.is(e) || ESCAPE.is(e) || CUT.is(e))
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
    updateCode.invokeLater(Align.BOTTOM);
    e.consume();

    // refresh completions, or show them after a delay if a completion was started
    final char ch = e.getKeyChar();
    refreshCompletion(Character.isLetter(ch) || rend.syntax().completeStart(ch));
    // a signature is proposed if an argument list is opened or continued
    signatureCode.invokeLater(ch == '(' || ch == ',');
  }

  /**
   * Refreshes the layout.
   * @param f used font
   */
  public final void refreshLayout(final Font f) {
    setFont(f);
    scroll.refreshLayout();
    hscroll.refreshLayout();
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
    finish(pos, true);
  }

  /**
   * Copies the selected text to the clipboard.
   * @return true if text was copied
   */
  private boolean copy() {
    final String txt = editor.selected();
    final boolean copy = !txt.isEmpty();
    if(copy) BaseXLayout.toClipboard(txt);
    return copy;
  }

  /**
   * Finishes an edit command: records history and re-checks if the text changed, and always
   * recomputes the layout.
   * @param old cursor position before the edit ({@code -1} to skip the history entry)
   * @param changed whether the text was modified
   */
  private void finish(final int old, final boolean changed) {
    if(changed) {
      if(old != -1) hist.store(editor.text(), old, editor.pos());
      edited();
    }
    updateCode.invokeLater(Align.BOTTOM);
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
    completion.hide();
    signature.hide();
    final int units = e.getUnitsToScroll() * 20;
    if(e.isShiftDown()) rend.moveX(units);
    else scroll.pos(scroll.pos() + units);
    rend.repaint();
  }

  @Override
  public final void componentResized(final ComponentEvent e) {
    updateCode.invokeLater();
  }

  @Override
  public final void componentShown(final ComponentEvent e) {
    updateCode.invokeLater();
  }

  /** Undo/redo command. */
  private class HistoryCmd extends GUIPopupCmd {
    /** Undo/redo flag. */
    private final boolean undo;

    /**
     * Constructor.
     * @param undo undo/redo flag
     */
    HistoryCmd(final boolean undo) {
      super(undo ? Text.UNDO : Text.REDO, undo ? UNDOSTEP : REDOSTEP);
      this.undo = undo;
    }

    @Override
    public void execute() { history(undo); }
    @Override
    public boolean enabled(final GUI main) { return hasHistory(undo); }
  }

  /**
   * Undoes or redoes the last modification.
   * @param undo undo/redo flag
   */
  public final void history(final boolean undo) {
    if(!hist.active()) return;
    final byte[] text = undo ? hist.prev() : hist.next();
    if(text == null) return;
    editor.text(text);
    editor.pos(hist.caret());
    finish(-1, true);
  }

  /**
   * Indicates if a modification can be undone or redone.
   * @param undo undo/redo flag
   * @return result of check
   */
  public final boolean hasHistory(final boolean undo) {
    return undo ? !hist.first() : !hist.last();
  }

  /** Cut command. */
  private class CutCmd extends GUIPopupCmd {
    /** Constructor. */
    CutCmd() { super(Text.CUT, CUT); }

    @Override
    public void execute() {
      final int pos = editor.pos();
      if(!copy()) return;
      editor.delete();
      finish(pos, true);
    }
    @Override
    public boolean enabled(final GUI main) { return hist.active() && editor.isSelected(); }
  }

  /** Copy command. */
  private class CopyCmd extends GUIPopupCmd {
    /** Constructor. */
    CopyCmd() { super(Text.COPY, COPY); }

    @Override
    public void execute() { copy(); }
    @Override
    public boolean enabled(final GUI main) { return editor.isSelected(); }
  }

  /** Paste command. */
  private class PasteCmd extends GUIPopupCmd {
    /** Constructor. */
    PasteCmd() { super(Text.PASTE, PASTE); }

    @Override
    public void execute() {
      final ArrayList<Object> contents = BaseXLayout.fromClipboard(null);
      if(!contents.isEmpty()) paste(contents.getFirst().toString());
    }
    @Override
    public boolean enabled(final GUI main) {
      return hist.active() && !BaseXLayout.fromClipboard(null).isEmpty();
    }
  }

  /** Delete command. */
  private class DelCmd extends GUIPopupCmd {
    /** Constructor. */
    DelCmd() { super(Text.DELETE, DELNEXT); }

    @Override
    public void execute() {
      final int pos = editor.pos();
      editor.delete();
      finish(pos, true);
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

  /** Open search bar. */
  private class FindCmd extends GUIPopupCmd {
    /** Constructor. */
    FindCmd() { super(Text.FIND + Text.ELLIPSIS, FIND); }

    @Override
    public void execute() { find(); }
    @Override
    public boolean enabled(final GUI main) { return searchable(); }
  }

  /**
   * Activates the search bar.
   */
  public final void find() {
    // the adopted selection is a hit of the new search, and restricts no replacement
    search.activate(searchString(), true, false);
  }

  /**
   * Indicates if the text is attached to a search bar.
   * @return result of check
   */
  public final boolean searchable() {
    return search != null;
  }

  /** Find next or previous hit. */
  private class FindHitCmd extends GUIPopupCmd {
    /** Next/previous flag. */
    private final boolean next;

    /**
     * Constructor.
     * @param next next/previous flag
     */
    FindHitCmd(final boolean next) {
      super(next ? Text.FIND_NEXT : Text.FIND_PREVIOUS, next ? FINDNEXT : FINDPREV);
      this.next = next;
    }

    @Override
    public void execute() { search(next); }
    @Override
    public boolean enabled(final GUI main) { return searchable(); }
  }

  /** Toggles a search mode. */
  private class ToggleCmd extends GUIPopupCmd {
    /** Button of the search mode. */
    private final Function<SearchBar, AbstractButton> button;

    /**
     * Constructor.
     * @param label label
     * @param shortcut shortcut
     * @param button button of the search mode
     */
    ToggleCmd(final String label, final BaseXKeys shortcut,
        final Function<SearchBar, AbstractButton> button) {
      super(label, shortcut);
      this.button = button;
    }

    @Override
    public void execute() { search.toggle(button.apply(search)); }
    @Override
    public boolean toggle() { return true; }
    @Override
    public boolean enabled(final GUI main) {
      return search != null && button.apply(search).isEnabled();
    }
    @Override
    public boolean selected(final GUI main) { return button.apply(search).isSelected(); }
  }

  /**
   * Highlights the next/previous hit.
   * @param next next/previous hit
   */
  public final void search(final boolean next) {
    // a hidden search bar adopts the selected string: its first hit is the current one
    search.find(searchString(), search.isVisible() ?
      next ? SearchDir.FORWARD : SearchDir.BACKWARD : SearchDir.CURRENT);
  }

  /**
   * Jumps to a specific line.
   */
  public final void gotoLine() {
    final DialogLine dl = new DialogLine(gui, caretPos()[0]);
    if(!dl.ok()) return;

    final byte[] text = editor.text();
    final int tl = text.length, el = dl.line();
    int line = 1, pos = 0;
    for(int t = 0; t < tl && line < el; t++) {
      if(text[t] != '\n') continue;
      pos = t + 1;
      ++line;
    }
    setCaret(pos);
    gui.editor.posCode.invokeLater();
  }

  /**
   * Indicates if the current text can have function and variable declarations.
   * @return result of check
   */
  public final boolean hasDeclarations() {
    return rend.syntax().hasDeclarations();
  }

  /**
   * Returns the function and variable declarations of the current text.
   * @return declarations
   */
  private ArrayList<Declaration> declarations() {
    return rend.syntax().declarations(editor.text());
  }

  /**
   * Jumps to a declaration of the current text.
   */
  public final void gotoDeclaration() {
    // the button and the menu entry can be enabled until the controls are refreshed
    if(!hasDeclarations()) return;

    // the caret follows the selection and stays where the dialog leaves it
    DialogDeclaration.show(gui, declarations(), editor.pos(), this::setCaret);
    gui.editor.posCode.invokeLater();
  }

  /**
   * Code completion.
   * @param explicit invoked via keyboard shortcut
   */
  void complete(final boolean explicit) {
    if(selected()) return;

    // the popup is placed at the beginning of the completed string, before the cursor is moved
    final int start = editor.completionStart(), caret = editor.pos();
    final int[] cursor = rend.cursor();
    final Point point = new Point(Math.max(0, cursor[0] - rend.width(start, caret)), cursor[1]);
    // the cursor jumps to the end of the edited string
    if(explicit) setCaret(editor.completionEnd());

    final ArrayList<Completion> candidates = candidates(start, explicit);
    if(explicit && candidates.size() == 1) {
      // insert single candidate
      complete(candidates.getFirst().value(), start);
    } else if(!candidates.isEmpty()) {
      completion.show(candidates, word(start), start, point);
      // both popups are placed at the same position: the candidates take precedence
      signature.hide();
    }
  }

  /**
   * Returns the completion candidates for the string before the cursor. The text is scanned:
   * as long as the popup stays open, the resulting candidates are only filtered again.
   * @param start start position of the string
   * @param explicit invoked via keyboard shortcut
   * @return candidates
   */
  private ArrayList<Completion> candidates(final int start, final boolean explicit) {
    final byte[] text = editor.text();
    final Syntax syntax = rend.syntax();
    // no completions in strings, comments, tags and other non-code tokens
    if(!completing(start, explicit) || !syntax.completable(text, editor.pos()))
      return new ArrayList<>();

    proposals = syntax.completions(text, start);
    return Completions.candidates(word(start), proposals);
  }

  /**
   * Indicates if candidates may be proposed for the string before the cursor.
   * @param start start position of the string
   * @param explicit invoked via keyboard shortcut
   * @return result of check
   */
  private boolean completing(final int start, final boolean explicit) {
    if(explicit) return true;
    final byte[] text = editor.text();
    final int caret = editor.pos();
    // the cursor must be placed at the end of the completed string
    if(caret != editor.completionEnd()) return false;
    // a completion is started by a name character, or by the character before an empty string
    final int ch = caret == start ? Syntax.prev(text, start) : cp(text, start);
    if(!rend.syntax().completeStart(ch) && !XMLToken.isNCStartChar(ch)) return false;
    // a function call is already complete
    return caret >= text.length || text[caret] != '(';
  }

  /**
   * Returns the string that is completed.
   * @param start start position
   * @return string
   */
  private String word(final int start) {
    return string(editor.text(), start, editor.pos() - start);
  }

  /**
   * Refreshes the signature of the function call that encloses the caret.
   * @param show show the signature of a call that has no visible signature yet
   */
  private void refreshSignature(final boolean show) {
    if(!show && !signature.active()) return;

    // both popups are placed at the same position: the candidates take precedence
    final Syntax syntax = rend.syntax();
    final TextEditor.Call call = completion.visible() ? null : editor.call(syntax);
    if(call != null) {
      final String name = string(editor.text(), call.start(), call.end() - call.start());
      // the signature is resolved once per name: a declaration lookup scans the text
      if(!name.equals(signatureName)) {
        signatureName = name;
        signatureValue = signature(name);
      }
      final Signature sig = signatureValue;
      // the signature is shown if the function is known and can take a further argument
      if(sig != null && call.arg() < sig.params()) {
        // a keyword argument emphasizes the parameter it names, others the one at its position
        final String keyword = call.keyword();
        final int index = keyword != null ? sig.param(keyword) : call.arg();
        // the popup is aligned with the called function, below the rendered row with the caret
        final int y = rend.cursorBottom();
        if(y != -1) {
          signature.show(sig, name, index, new Point(rend.x(call.start()), y));
          return;
        }
      }
    }
    signature.hide();
  }

  /**
   * Returns the signature of a built-in or declared function.
   * @param name function name
   * @return signature, or {@code null} if the function is unknown
   */
  private Signature signature(final String name) {
    final Signature sig = rend.syntax().signature(name);
    if(sig != null) return sig;
    // declared functions are looked up in the text
    for(final Declaration declaration : declarations()) {
      if(declaration.name().equals(name)) return Signature.get(declaration.args());
    }
    return null;
  }

  /**
   * Refreshes the candidates of a visible completion popup.
   * @param schedule show a new popup after a delay if none is visible
   */
  private void refreshCompletion(final boolean schedule) {
    if(completion.visible()) {
      // the popup is closed if the cursor leaves the completed string
      final int start = completion.start();
      // the text is not scanned again: the proposals of the visible popup are filtered
      final ArrayList<Completion> candidates = editor.completionStart() == start &&
        completing(start, false) ? Completions.candidates(word(start), proposals) :
        new ArrayList<>();
      if(candidates.isEmpty()) completion.hide();
      else completion.update(candidates, word(start));
    }
    // completions are only proposed automatically if the corresponding option is activated
    if(schedule && !completion.visible() && gui.gopts.get(GUIOptions.COMPLETION) == 0)
      completion.schedule();
  }

  /**
   * Auto-completes a string at the specified position.
   * @param string string
   * @param start start position
   */
  void complete(final String string, final int start) {
    final int pos = editor.pos();
    editor.complete(string, start);
    finish(pos, true);
    // an inserted function call is annotated with its signature
    signatureCode.invokeLater(true);
  }
}
