package org.basex.gui.editor;

import static org.basex.util.Token.*;

import org.basex.gui.*;
import org.basex.gui.editor.Editor.SearchDir;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class contains the rendered text.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class EditorText {
  /** Tab width. */
  static final int TAB = 2;

  /** Search context. */
  private SearchContext search;
  /** Start and end positions of search terms. */
  IntList[] spos = { new IntList(), new IntList() };

  /** Text array to be written. */
  private byte[] text = EMPTY;
  /** Current cursor position. */
  private int pc;
  /** Start position of a token. */
  private int ps;
  /** End position of a token. */
  private int pe;
  /** Start position of a text selection. */
  private int ms = -1;
  /** End position of a text selection (+1). */
  private int me = -1;
  /** Start position of an error highlighting. */
  private int es = -1;
  /** Current search position. */
  private int sp;
  /** The GUI is used to highlight selected text. */
  private GUI gui;

  /**
   * Constructor.
   * @param t text
   */
  EditorText(final byte[] t) {
    text = t;
  }

  /**
   * Initializes the iterator.
   */
  void init() {
    ps = 0;
    pe = 0;
    sp = 0;
  }

  /**
   * Sets a new text.
   * @param t new text
   */
  void text(final byte[] t) {
    text = t;
    noSelect();
    if(search != null) spos = search.search(t);
  }

  /**
   * Checks if the text contains more words.
   * @return result of check
   */
  boolean moreTokens() {
    // quit if text has ended
    final byte[] txt = text;
    final int tl = txt.length;
    int ppe = pe;
    if(ppe >= tl) return false;
    ps = ppe;

    // find next token boundary
    int ch = cp(txt, ppe);
    ppe += cl(txt, ppe);
    if(ftChar(ch)) {
      while(ppe < tl) {
        ch = cp(txt, ppe);
        if(!ftChar(ch)) break;
        ppe += cl(txt, ppe);
      }
    }
    pe = ppe;
    return true;
  }

  /**
   * Returns the token as string.
   * @return string
   */
  public String nextString() {
    final byte[] txt = text;
    final int ppe = pe;
    final int pps = ps;
    return ppe <= txt.length ? string(txt, pps, ppe - pps) : "";
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   * @return character
   */
  int next(final boolean select) {
    return noSelect(select, true) ? curr() : next();
  }

  /**
   * Sets a new search processor.
   * @param sc search processor
   */
  void search(final SearchContext sc) {
    // skip search if criteria have not changed
    if(sc.equals(search)) {
      sc.nr = search.nr;
      sc.panel.refresh(sc);
    } else {
      spos = sc.search(text);
      search = sc;
    }
  }

  /**
   * Replaces the text.
   * @param rc replace context
   * @return selection offsets
   */
  int[] replace(final ReplaceContext rc) {
    final int start = selected() ? Math.min(ms, me) : 0;
    final int end = selected() ? Math.max(ms, me) : text.length;
    return rc.replace(search, text, start, end);
  }

  /**
   * Moves one token forward.
   * @param select selection flag
   */
  void nextToken(final boolean select) {
    int ch = next(select);
    if(ch == '\n') return;
    if(Character.isLetterOrDigit(ch)) {
      while(Character.isLetterOrDigit(ch)) ch = next();
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    } else if(Character.isWhitespace(ch)) {
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    } else {
      while(ch != '\n' && !Character.isLetterOrDigit(ch) &&
          !Character.isWhitespace(ch)) ch = next();
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    }
    if(ps != text.length) prev();
  }

  /**
   * Moves one token back.
   * @param select selection flag
   */
  void prevToken(final boolean select) {
    int ch = prev(select);
    if(ch == '\n') return;
    if(Character.isLetterOrDigit(ch)) {
      while(Character.isLetterOrDigit(ch)) ch = prev();
    } else if(Character.isWhitespace(ch)) {
      while(ch != '\n' && Character.isWhitespace(ch)) ch = prev();
      while(Character.isLetterOrDigit(ch)) ch = prev();
    } else {
      while(ch != '\n' && !Character.isLetterOrDigit(ch) &&
          !Character.isWhitespace(ch)) ch = prev();
    }
    if(ps != 0) next();
  }

  /**
   * Checks if the character position equals the word end.
   * @return result of check
   */
  boolean more() {
    return ps < pe && ps < text.length;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  public int curr() {
    return ps < 0 || ps >= text.length ? '\n' : cp(text, ps);
  }

  /**
   * Returns the original text array.
   * @return text
   */
  public byte[] text() {
    return text;
  }

  /**
   * Moves one character forward.
   * @return current character
   */
  int next() {
    final int c = curr();
    if(ps < text.length) ps += cl(text, ps);
    return c;
  }

  /**
   * Sets the iterator position.
   * @param p iterator position
   */
  void pos(final int p) {
    ps = p;
  }

  /**
   * Returns the iterator position.
   * @return iterator position
   */
  int pos() {
    return ps;
  }

  // POSITION ===========================================================================

  /**
   * Moves to the beginning of the line.
   * @param select selection flag
   * @return number of passed characters
   */
  int bol(final boolean select) {
    if(ps == 0) return 0;
    int c = 0;
    do c += curr() == '\t' ? TAB : 1; while(prev(select) != '\n');
    if(ps != 0 || curr() == '\n') next(select);
    return c;
  }

  /**
   * Moves to the first character or the beginning of the line.
   * @param select selection flag
   */
  void home(final boolean select) {
    final int p = ps;
    boolean s = true;
    // find beginning of line
    while(prev(select) != '\n') s &= ws(curr());
    if(ps != 0 || curr() == '\n') next(select);
    // move to first non-whitespace character
    if(p == ps || !s) while(ws(curr()) && curr() != '\n') next(select);
  }

  /**
   * Moves to the end of the line.
   * @param select selection flag
   */
  void eol(final boolean select) {
    forward(Integer.MAX_VALUE, select);
  }

  /**
   * Moves one character back and returns the found character.
   * @param select selection flag
   * @return character
   */
  int prev(final boolean select) {
    return noSelect(select, false) ? curr() : prev();
  }

  /**
   * Moves one character back and returns the found character. A newline character is
   * returned if the cursor is placed at the beginning of the text.
   * @return character
   */
  int prev() {
    if(ps == 0) return '\n';
    while(--ps > 0 && text[ps] < -64 && text[ps] >= -128);
    return curr();
  }

  /**
   * Moves to the specified position of to the of the line.
   * @param p position to move to
   * @param select selection flag
   */
  void forward(final int p, final boolean select) {
    int nc = 0;
    while(curr() != '\n') {
      nc += curr() == '\t' ? TAB : 1;
      if(nc >= p) return;
      next(select);
    }
  }

  /**
   * Adds a string at the current position.
   * @param str string
   */
  void add(final String str) {
    final TokenBuilder tb = new TokenBuilder(str.length() << 1);
    final int cl = str.length();
    for(int c = 0; c < cl; ++c) {
      // ignore invalid characters
      int ch = str.charAt(c);
      if(ch == '\r') continue;
      if(ch < ' ' && !ws(ch)) {
        ch = '\n';
      } else if(Character.isHighSurrogate((char) ch) && c + 1 < cl) {
        ch = Character.toCodePoint((char) ch, str.charAt(++c));
      }
      tb.add(ch);
    }
    final int tl = text.length;
    final int ts = tb.size();
    final byte[] tmp = new byte[tl + ts];
    System.arraycopy(text, 0, tmp, 0, ps);
    System.arraycopy(tb.finish(), 0, tmp, ps, ts);
    System.arraycopy(text, ps, tmp, ps + ts, tl - ps);
    text(tmp);
    ps += ts;
  }

  /**
   * Indents lines.
   * @param s start position
   * @param e end position
   * @param sh shift flag
   */
  void indent(final int s, final int e, final boolean sh) {
    // extend selection to match whole lines
    pos(s);
    bol(true);
    startSelect();
    pos(e);
    forward(Integer.MAX_VALUE, true);
    next(true);
    finishSelect();

    // decide if to use tab or spaces
    boolean tab = false;
    for(final byte t : text) tab |= t == '\t';
    byte[] add = { '\t' };
    if(!tab) {
      add = new byte[TAB];
      for(int a = 0; a < TAB; a++) add[a] = ' ';
    }

    // build new text
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text, 0, ms);
    final int pl = text.length;
    for(int p = ms; p < ps; p += cl(text, p)) {
      if(p == 0 || text[p - 1] == '\n') {
        if(sh) {
          // remove indentation
          if(text[p] == '\t') {
            me--;
            continue;
          }
          if(text[p] == ' ') {
            me--;
            for(int i = 1; i < TAB && p + i < pl && text[p + i] == ' '; i++) {
              me--;
              p++;
            }
            continue;
          }
        } else {
          // add new indentation
          tb.add(add);
          me += add.length;
        }
      }
      tb.add(cp(text, p));
    }
    tb.add(text, ps, text.length);
    ps = me;
    final int ss = ms;
    text(tb.finish());
    ms = ss;
    me = ps;
  }

  /**
   * (Un)comments highlighted text or line.
   * @param syntax syntax highlighter
   */
  void comment(final Syntax syntax) {
    final byte[] st = syntax.commentOpen();
    final byte[] en = syntax.commentEnd();
    boolean add = true;
    int min = ps;
    int max = ps;

    if(selected()) {
      min = ps < ms ? ps : ms;
      max = ps > ms ? ps : ms;
      if(max > min && text[max - 1] == '\n') max--;

      // selected
      final int mn = Math.max(min + st.length, max - en.length);
      // check if selected area already has a comment
      if(indexOf(text, st, min) == min && indexOf(text, en, mn) == mn) {
        final TokenBuilder tb = new TokenBuilder();
        tb.add(text, 0, min);
        tb.add(text, min + st.length, max - en.length);
        tb.add(text, max, text.length);
        text(tb.finish());
        ms = min;
        me = max - st.length - en.length;
        ps = me;
        add = false;
      }
    } else {
      while(min > 0 && text[min - 1] != '\n') --min;
      while(max < size() && text[max] != '\n') ++max;
    }

    if(add) {
      pos(max);
      add(string(en));
      pos(min);
      add(string(st));
      ms = min;
      me = max + st.length + en.length;
      ps = me;
    }
  }

  /**
   * Indents the current line or text.
   * @param sb typed in string
   * @param shift shift key
   * @return indentation flag
   */
  boolean indent(final StringBuilder sb, final boolean shift) {
    // no selection, shift pressed: select current character
    if(!selected() && shift) select(ps + 1, ps);

    // check if something is selected
    boolean i = false;
    if(selected()) {
      // check if lines are to be indented
      final int s = Math.min(ps, start());
      final int l = Math.max(ps, start()) - 1;
      int p = s;
      for(; p <= l && p < text.length; p++) i |= text[p] != '\n';
      i |= p == text.length;
      if(i) {
        indent(s, l, shift);
        sb.setLength(0);
      }
    } else {
      boolean c = true;
      for(int p = ps - 1; p >= 0 && c; p--) {
        final byte b = text[p];
        c = ws(b);
        if(b == '\n') break;
      }
      if(c) {
        sb.setLength(0);
        sb.append("  ");
      }
    }
    return i;
  }

  /**
   * Processes the enter key.
   * @param sb typed in string
   */
  void open(final StringBuilder sb) {
    // adopt indentation from previous line
    int s = 0;
    for(int p = ps - 1; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(b == '\t') {
        s += EditorText.TAB;
      } else if(b == ' ') {
        s++;
      } else {
        s = 0;
      }
    }
    // indent after opening bracket
    if(ps > 0) {
      final int l = text[ps - 1];
      if(l == '(' || l == '{' || l == '[') s += EditorText.TAB;
    }
    // unindent before closing bracket
    if(ps < text.length) {
      final int l = text[ps];
      if(l == ')' || l == '}' || l == ']') s -= EditorText.TAB;
    }
    for(int p = 0; p < s; p++) sb.append(' ');
  }

  /**
   * Closes a bracket.
   */
  void close() {
    int p = ps - 1;
    for(; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(!ws(b)) return;
    }
    if(++p >= ps) return;
    ms = Math.max(ps - TAB, p);
    me = Math.max(ps, p);
    if(ms != me) delete();
  }

  /**
   * Deletes the current character or selection.
   * Assumes that the current position allows a deletion.
   */
  void delete() {
    final int tl = text.length;
    if(tl == 0) return;
    final int s = selected() ? Math.min(ms, me) : ps;
    final int e = selected() ? Math.max(ms, me) : ps + cl(text, ps);
    final byte[] tmp = new byte[tl - e + s];
    System.arraycopy(text, 0, tmp, 0, s);
    System.arraycopy(text, e, tmp, s, tl - e);
    text(tmp);
    ps = s;
  }

  /**
   * Deletes the current line.
   */
  void deleteLine() {
    bol(false);
    startSelect();
    eol(true);
    next(true);
    finishSelect();
    delete();
  }

  // TEXT SELECTION =====================================================================

  /**
   * Setter.
   * @param gui0 Sets the GUI used for highlight selections.
   */
  void setGUI(final GUI gui0) {
    gui = gui0;
  }

  /**
   * Is called whenever the selected text changes. Updates the visible searched text.
   */
  private void selectionChanged() {
    if(gui == null) return;
    String str = copy().trim();
    if(str.indexOf('\n') >= 0 || str.indexOf('\r') >= 0) str = "";
    final SearchPanel tmp = new SearchPanel(gui);
    tmp.mcase.setSelected(true);
    tmp.word.setSelected(false);
    tmp.regex.setSelected(false);
    tmp.multi.setSelected(false);
    search(new SearchContext(tmp, str));
  }

  /**
   * Jumps to the maximum/minimum position and resets the selection.
   * @param select selection flag
   * @param max maximum/minimum flag
   * @return true if selection was reset
   */
  private boolean noSelect(final boolean select, final boolean max) {
    final boolean rs = !select && selected();
    if(rs) {
      ps = max ^ ms < me ? ms : me;
      noSelect();
    }
    return rs;
  }

  /**
   * Resets the selection.
   */
  void noSelect() {
    ms = -1;
    me = -1;
    selectionChanged();
  }

  /**
   * Sets the start of a text selection.
   */
  void startSelect() {
    ms = ps;
    me = ps;
    selectionChanged();
  }

  /**
   * Extends the text selection.
   */
  void extendSelect() {
    me = ps;
    selectionChanged();
  }

  /**
   * Finishes a text selection.
   */
  void finishSelect() {
    me = ps;
    checkSelect();
  }

  /**
   * Selects the specified area.
   * @param s start position
   * @param e end position
   */
  void select(final int s, final int e) {
    ms = s;
    me = e;
    checkSelect();
  }

  /**
   * Checks the validity of the selection.
   */
  void checkSelect() {
    if(ms == me) noSelect();
    else selectionChanged();
  }

  /**
   * Returns the start of the text selection. The value is {@code -1} if no
   * text is selected.
   * @return start selection
   */
  int start() {
    return ms;
  }

  /**
   * Tests if text is currently being selected, or has already been selected.
   * @return result of check
   */
  boolean selecting() {
    return ms != -1;
  }

  /**
   * Tests if text has been selected.
   * @return result of check
   */
  boolean selected() {
    return ms != me;
  }

  /**
   * Tests if the current text position is selected.
   * @return result of check
   */
  boolean selectStart() {
    return selected() &&
        (inSelect() || (ms < me ? ms >= ps && ms < pe : me >= ps && me < pe));
  }

  /**
   * Tests if the current position is selected.
   * @return result of check
   */
  boolean inSelect() {
    return ms < me ? ps >= ms && ps < me : ps >= me && ps < ms;
  }

  /**
   * Returns the selected string.
   * @return string
   */
  String copy() {
    final TokenBuilder tb = new TokenBuilder();
    final int e = ms < me ? me : ms;
    for(int s = ms < me ? ms : me; s < e; s += cl(text, s)) {
      final int t = cp(text, s);
      if(t < 0 || t >= ' ' || t == 0x0A || t == 0x09) tb.add(t);
    }
    return tb.toString();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectWord() {
    pos(getCaret());
    final boolean ch = ftChar(prev(true));
    while(pos() > 0) {
      final int c = prev(true);
      if(c == '\n' || ch != ftChar(c)) break;
    }
    if(pos() != 0) next(true);
    startSelect();
    while(pos() < size()) {
      final int c = curr();
      if(c == '\n' || ch != ftChar(c)) break;
      next(true);
    }
    finishSelect();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    pos(getCaret());
    bol(true);
    startSelect();
    eol(true);
    finishSelect();
  }

  // ERROR HIGHLIGHTING =================================================================

  /**
   * Tests if the current token is erroneous.
   * @return result of check
   */
  boolean erroneous() {
    return es >= ps && es < pe;
  }

  /**
   * Returns the error position.
   * @return error position
   */
  public int error() {
    return es;
  }

  /**
   * Sets the error position.
   * @param s start position
   */
  public void error(final int s) {
    es = s;
  }

  // SEARCH HIGHLIGHTING ================================================================

  /**
   * Returns true if the cursor focuses a search string.
   * @return result of check
   */
  boolean searchStart() {
    if(search == null) return false;
    final IntList[] sps = spos;
    if(sp == sps[0].size()) return false;
    while(ps > sps[1].get(sp)) {
      if(++sp == sps[0].size()) return false;
    }
    return pe > sps[0].get(sp);
  }

  /**
   * Tests if the current position is within a search term.
   * @return result of check
   */
  boolean inSearch() {
    if(sp >= spos[0].size() || ps < spos[0].get(sp)) return false;
    final boolean in = ps < spos[1].get(sp);
    if(!in) sp++;
    return in;
  }

  /**
   * Selects a search string.
   * @param dir search direction
   * @param select select hit
   * @return new cursor position, or {@code -1}
   */
  int jump(final SearchDir dir, final boolean select) {
    if(spos[0].isEmpty()) {
      if(select) noSelect();
      return -1;
    }

    int s = spos[0].sortedIndexOf(!select || selected() ? pc : pc - 1);
    switch(dir) {
      case CURRENT:  s = s < 0 ? -s - 1 : s;     break;
      case FORWARD:  s = s < 0 ? -s - 1 : s + 1; break;
      case BACKWARD: s = s < 0 ? -s - 2 : s - 1; break;
    }
    final int sl = spos[0].size();
    if(s < 0) s = sl - 1;
    else if(s == sl) s = 0;
    final int pos = spos[0].get(s);
    if(select) {
      pc = pos;
      ms = pos;
      me = spos[1].get(s);
    } else {
      pc = pos;
    }
    return pos;
  }

  // CURSOR =============================================================================

  /**
   * Checks if the text cursor moves over the current token.
   * @return result of check
   */
  boolean edited() {
    return pc >= ps && pc < pe;
  }

  /**
   * Sets the text cursor to the specified position. A text selection will be removed.
   * @param c cursor position
   */
  void setCaret(final int c) {
    noSelect();
    pc = c;
    ps = c;
  }

  /**
   * Sets the text cursor to the current position.
   */
  void setCaret() {
    pc = ps;
  }

  /**
   * Returns the position of the text cursor.
   * @return cursor position
   */
  int getCaret() {
    return pc;
  }

  /**
   * Returns the text size.
   * @return text size
   */
  int size() {
    return text.length;
  }

  @Override
  public String toString() {
    return copy();
  }
}
