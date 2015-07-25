package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.gui.*;
import org.basex.gui.text.SearchBar.SearchDir;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Provides methods for editing a text that is visualized by the {@link TextPanel}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class TextEditor {
  /** Opening brackets. */
  private static final String OPENING = "{([";
  /** Closing brackets. */
  private static final String CLOSING = "})]";

  /** Start and end positions of search terms. */
  IntList[] searchPos;
  /** Start position of a text selection. */
  int start = -1;
  /** End position of a text selection (+1). */
  int end = -1;
  /** Start position of an error highlighting. */
  int error = -1;

  /** GUI options. */
  private final GUIOptions gopts;
  /** Search context. */
  private SearchContext search;
  /** Text array to be written. */
  private byte[] text = EMPTY;
  /** Number of lines. Required for displaying line numbers. */
  private int lines = -1;
  /** Caret/edit position. */
  private int pos;

  /**
   * Constructor.
   * @param gui gui reference
   */
  TextEditor(final GUI gui) {
    gopts = gui.gopts;
  }

  /**
   * Sets a new text.
   * @param txt new text
   * @return {@code true} if text has changed
   */
  boolean text(final byte[] txt) {
    if(eq(txt, text)) return false;
    final int tl = txt.length;
    text = txt;
    lines = -1;
    noSelect();
    if(search != null) searchPos = search.search(txt);
    if(pos > tl) pos = tl;
    return true;
  }

  /**
   * Sets a new search context.
   * @param sc search context
   */
  void search(final SearchContext sc) {
    // skip search if criteria have not changed
    if(sc.equals(search)) {
      sc.nr = search.nr;
      sc.bar.refresh(sc);
    } else {
      searchPos = sc.search(text);
      search = sc;
    }
  }

  /**
   * Replaces the text.
   * @param rc replace context
   * @return selection offsets
   */
  int[] replace(final ReplaceContext rc) {
    // only adopt selection if it extends over more than one line
    final int ts = size();
    int s = Math.min(start, end), e = Math.max(start, end);
    boolean sel = selected();
    if(sel) {
      int p = s - 1;
      while(++p < e && text[p] != '\n');
      sel = p < e;
    }
    if(!sel) {
      s = 0;
      e = ts;
    }
    return rc.replace(search, text, s, e);
  }

  /**
   * Counts the number of lines in the text.
   * @return number of new lines in the text
   */
  int lines() {
    if(lines == -1) {
      int c = 1;
      for(final byte ch : text) if(ch == '\n') ++c;
      lines = c;
    }
    return lines;
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   */
  private void forward(final boolean select) {
    if(select || !selected()) {
      next();
    } else {
      pos(Math.max(start, end));
    }
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   */
  void next(final boolean select) {
    if(select && !selected()) startSelect();
    forward(select);
    if(select) endSelection();
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   */
  void previous(final boolean select) {
    if(select && !selected()) startSelect();
    back(select);
    if(select) endSelection();
  }

  /**
   * Moves one token forward.
   * @param select selection flag
   */
  void nextWord(final boolean select) {
    if(select && !selected()) startSelect();

    int ch = curr();
    forward(select);
    if(ch != '\n') {
      if(FTToken.lod(ch)) {
        while(FTToken.lod(ch)) ch = next();
        while(ch != '\n' && FTToken.ws(ch)) ch = next();
      } else if(FTToken.ws(ch)) {
        while(ch != '\n' && FTToken.ws(ch)) ch = next();
      } else {
        while(ch != '\n' && !FTToken.lod(ch) && !FTToken.ws(ch)) ch = next();
        while(ch != '\n' && FTToken.ws(ch)) ch = next();
      }
      if(pos != size()) prev();
    }
    if(select) endSelection();
  }

  /**
   * Moves one token back.
   * @param select selection flag
   */
  void prevWord(final boolean select) {
    if(select && !selected()) startSelect();

    int ch = back(select);
    if(ch != '\n') {
      if(FTToken.lod(ch)) {
        while(FTToken.lod(ch)) ch = prev();
      } else if(FTToken.ws(ch)) {
        while(ch != '\n' && FTToken.ws(ch)) ch = prev();
        while(FTToken.lod(ch)) ch = prev();
      } else {
        while(ch != '\n' && !FTToken.lod(ch) && !FTToken.ws(ch)) ch = prev();
      }
      if(pos != 0) next();
    }
    if(select) endSelection();
  }

  /**
   * Returns the position of first character of the current auto-completion input.
   * @return position
   */
  int completionStart() {
    int p = pos;
    while(p > 0 && !ws(text[p - 1])) --p;
    return p;
  }

  /**
   * Jumps to the beginning of the text.
   * @param select selection flag
   */
  void textStart(final boolean select) {
    startSelection(select);
    pos = 0;
    if(select) endSelection();
  }

  /**
   * Jumps to the end of the text.
   * @param select selection flag
   */
  void textEnd(final boolean select) {
    startSelection(select);
    pos = size();
    if(select) endSelection();
  }

  /**
   * Returns the original text array.
   * @return text
   */
  public byte[] text() {
    return text;
  }

  // POSITION ===========================================================================

  /**
   * Returns the current indentation.
   * @return indentation
   */
  private int indent() {
    return Math.max(1, gopts.get(GUIOptions.INDENT));
  }

  /**
   * Returns the spaces used for indenting text.
   * @return spaces
   */
  private byte[] spaces() {
    final byte[] spaces;
    if(gopts.get(GUIOptions.TABSPACES)) {
      spaces = new byte[indent()];
      Arrays.fill(spaces, (byte) ' ');
    } else {
      spaces = new byte[] { '\t' };
    }
    return spaces;
  }

  /**
   * Moves to the beginning of the line.
   * @param select selection flag
   * @return number of passed characters
   */
  private int bol(final boolean select) {
    if(pos == 0) {
      if(!select) noSelect();
      return 0;
    }
    final int ind = indent();
    int c = 0;
    do c += curr() == '\t' ? ind : 1; while(back(select) != '\n');
    if(pos != 0 || curr() == '\n') forward(select);
    return c;
  }

  /**
   * Moves to the beginning of a line.
   * @param select selection flag
   */
  void lineStart(final boolean select) {
    if(select && !selected()) startSelect();

    final int p = pos;
    boolean s = true;
    // find beginning of line
    while(back(select) != '\n') s &= FTToken.ws(curr());
    if(pos != 0 || curr() == '\n') forward(select);
    // move to first non-whitespace character
    if(p == pos || !s) {
      while(FTToken.ws(curr()) && curr() != '\n') forward(select);
    }

    if(select) endSelection();
  }

  /**
   * Moves to the end of a line.
   * @param select selection flag
   */
  void lineEnd(final boolean select) {
    startSelection(select);
    forward(Integer.MAX_VALUE, select);
    if(select) endSelection();
  }

  /**
   * Moves one character back and returns the found character.
   * @param select selection flag
   * @return previous character
   */
  private int back(final boolean select) {
    if(select || !selected()) return prev();
    pos(Math.min(start, end));
    return curr();
  }

  /**
   * Moves one character back and returns the found character. A newline character is
   * returned if the cursor is placed at the beginning of the text.
   * @return character
   */
  private int prev() {
    if(pos == 0) return '\n';
    // UTF-8 encoded bytes: move to first byte
    while(--pos > 0 && text[pos] < -64 && text[pos] >= -128);
    return curr();
  }

  /**
   * Moves to the specified position or the end of the line.
   * @param p position to move to
   * @param select selection flag
   */
  private void forward(final int p, final boolean select) {
    final int ind = indent();
    int nc = 0;
    while(curr() != '\n') {
      nc += curr() == '\t' ? ind : 1;
      if(nc >= p) return;
      forward(select);
    }
  }

  /**
   * Moves the cursor upwards.
   * @param l number of lines to move cursor
   * @param select selection flag
   * @param lastCol last column position
   * @return last column position
   */
  int linesUp(final int l, final boolean select, final int lastCol) {
    startSelection(select);

    int col = bol(select);
    if(pos() == 0) {
      col = -1;
    } else {
      if(lastCol != -1) col = lastCol;
      for(int i = 0; i < l; ++i) {
        back(select);
        bol(select);
      }
      forward(col, select);
    }
    if(select) endSelection();
    return col;
  }

  /**
   * Moves the cursor down. The current column position is remembered and, if possible, restored.
   * @param l number of lines to move cursor
   * @param select selection flag
   * @param lastCol last column position
   * @return last column position
   */
  int linesDown(final int l, final boolean select, final int lastCol) {
    startSelection(select);

    int lc = lastCol == -1 ? bol(select) : lastCol;
    for(int i = 0; i < l; ++i) {
      forward(Integer.MAX_VALUE, select);
      forward(select);
    }
    forward(lc, select);
    if(pos() == size()) lc = -1;

    if(select) endSelection();
    return lc;
  }

  /**
   * Adds a string at the current position.
   * @param str string
   */
  void add(final String str) {
    final int cl = str.length();
    final TokenBuilder tb = new TokenBuilder(cl);
    for(int c = 0; c < cl; ++c) {
      // skip invalid characters
      int ch = str.charAt(c);
      if(ch == '\r' || ch < ' ' && !ws(ch)) continue;
      if(Character.isHighSurrogate((char) ch) && c + 1 < cl) {
        ch = Character.toCodePoint((char) ch, str.charAt(++c));
      }
      tb.add(ch);
    }
    insert(tb.finish(), pos, pos);
    pos += tb.size();
  }

  /**
   * (Un)comments highlighted text or line.
   * @param syntax syntax highlighter
   * @return {@code true} if text has changed
   */
  boolean comment(final Syntax syntax) {
    final byte[] st = syntax.commentOpen(), en = syntax.commentEnd();
    final byte[] ste = concat(st, SPACE), ene = concat(SPACE, en);
    final int sl = st.length, el = en.length, sle = ste.length, ele = ene.length;

    if(!selected()) {
      // no selection: select line
      start = pos;
      end = pos;
      while(start > 0 && text[start - 1] != '\n') --start;
      while(end < size() && text[end] != '\n') ++end;
    } else if(start > end) {
      // selection -> start < end
      final int s = start;
      start = end;
      end = s;
    }

    // ignore whitespaces
    while(start < end && ws(text[start])) ++start;
    while(end > start && ws(text[end - 1])) --end;

    final int min = start;
    int max = end;
    if(selected() && text[max - 1] == '\n') max--;

    // create new text with or without comment
    final TokenBuilder tb = new TokenBuilder();
    final int mx = Math.max(min + sl, max - el), off;
    final int mxe = Math.max(min + sle, max - ele);
    if(indexOf(text, ste, min) == min && indexOf(text, ene, mxe) == mxe) {
      // remove existing comment
      tb.add(text, min + sle, max - ele);
      off = -sle - ele;
    } else if(indexOf(text, st, min) == min && indexOf(text, en, mx) == mx) {
      // remove existing comment
      tb.add(text, min + sl, max - el);
      off = -sl - el;
    } else {
      // add new comment
      tb.add(ste).add(text, min, max).add(ene);
      off = sle + ele;
    }
    final boolean added = insert(tb.finish(), min, max);
    select(min, max + off);
    return added;
  }

  /**
   * Inserts a string into the given text.
   * @param string new string
   * @param offset offset where to add the new string
   * @param rem offset of remaining text
   * @return {@code true} if text has changed
   */
  private boolean insert(final byte[] string, final int offset, final int rem) {
    final int ts = size(), al = string.length;
    final byte[] tmp = new byte[offset + al + ts - rem];
    System.arraycopy(text, 0, tmp, 0, offset);
    System.arraycopy(string, 0, tmp, offset, al);
    System.arraycopy(text, rem, tmp, offset + al, ts - rem);
    return text(tmp);
  }

  /**
   * Moves the current line or the selected lines up or down.
   * @param down down/up flag
   */
  void move(final boolean down) {
    if(!extend()) return;

    final int s = start, e = end, ts = size();
    final byte[] tmp = Arrays.copyOf(text, ts);
    if(down) {
      if(e == ts) return;
      pos = e;
      lineEnd(true);
      int c = s;
      for(int i = e; i < pos; i++) tmp[c++] = text[i];
      tmp[c++] = '\n';
      for(int i = s; i < e - 1; i++) tmp[c++] = text[i];
      text(tmp);
      select(s + pos - e + 1, Math.min(ts, pos + 1));
    } else {
      if(s == 0) return;
      pos = s - 1;
      bol(true);
      int c = pos;
      for(int i = s; i < e; i++) tmp[c++] = text[i];
      if(tmp[c - 1] != '\n') tmp[c++] = '\n';
      for(int i = pos; i < s && c < ts; i++) tmp[c++] = text[i];
      text(tmp);
      select(pos, pos + e - s);
    }
  }

  /**
   * Inserts the specified value and updates the cursor position.
   * @param value value
   * @param p position to start completion from
   */
  void complete(final String value, final int p) {
    // key found
    String v = value;
    final int car = v.indexOf('_');
    if(car != -1) v = v.replace("_", "");
    // adopt current indentation
    final int ind = open();
    if(ind != 0) {
      final StringBuilder spaces = new StringBuilder();
      for(int i = 0; i < ind; i++) spaces.append(' ');
      v = new TokenBuilder().addSep(v.split("\n"), "\n" + spaces).toString();
    }
    // delete old string, add new one
    replace(p, pos, v);
    // adjust cursor
    if(car != -1) pos = p + car;
  }

  /**
   * Formats the selected text.
   * @param syntax syntax highlighter
   * @return {@code true} if text has changed
   */
  boolean format(final Syntax syntax) {
    final boolean sel = selected();
    final int s = sel ? Math.min(start, end) : 0;
    final int e = sel ? Math.max(start, end) : size();
    final byte[] format = syntax.format(Arrays.copyOfRange(text, s, e), spaces());
    final boolean changed = insert(format, s, e);
    select(s, s + format.length);
    return changed;
  }

  /**
   * Sorts the selected text.
   * @return {@code true} if text has changed
   */
  boolean sort() {
    if(!extend()) return false;

    // collect lines to be sorted
    final int s = start, e = end, ts = size();
    final byte[] tmp = Arrays.copyOf(text, ts);
    final TokenList tl = new TokenList();
    final ByteList bl = new ByteList();
    for(int i = s; i < e; i++) {
      final byte ch = tmp[i];
      if(ch == '\n') {
        tl.add(bl.next());
      } else {
        bl.add(ch);
      }
    }

    // sort data and merge duplicate lines
    if(!bl.isEmpty()) tl.add(bl.finish());
    tl.sort(gopts.get(GUIOptions.CASESORT), gopts.get(GUIOptions.ASCSORT));
    if(gopts.get(GUIOptions.MERGEDUPL)) tl.unique();

    // copy lines back to text
    int i = s;
    for(final byte[] line : tl) {
      final int ll = line.length;
      System.arraycopy(line, 0, tmp, i, ll);
      i += ll;
      if(i < e) tmp[i++] = '\n';
    }
    if(i < e) System.arraycopy(tmp, e, tmp, i, ts - e);
    final boolean changed = text(i == e ? tmp : Arrays.copyOf(tmp, ts - e + i));
    select(s, i);
    return changed;
  }

  /**
   * Indents the current line or text.
   * @param sb typed in string
   * @param shift shift key
   * @return indentation flag
   */
  boolean indent(final StringBuilder sb, final boolean shift) {
    // no selection, shift pressed: select current character
    if(!selected() && shift && size() != 0) selectLine();

    // check if something is selected
    if(selected()) {
      indent(shift);
      sb.setLength(0);
      return selected();
    }

    if(shift) {
      sb.setLength(0);
    } else {
      final boolean c = pos > 0;
      for(int p = pos - 1; p >= 0 && c; p--) {
        final byte b = text[p];
        if(!ws(b)) return false;
        if(b == '\n') break;
      }
      sb.setLength(0);
      sb.append(string(spaces()));
    }
    return false;
  }

  /**
   * Processes the enter key and checks for opening brackets.
   * @param sb typed in string
   * @return number of characters to move forward
   */
  int enter(final StringBuilder sb) {
    // indent after opening bracket
    final boolean opening = pos > 0 && OPENING.indexOf(text[pos - 1]) != -1;
    final boolean closing = pos < size() && CLOSING.indexOf(text[pos]) != -1;

    final int ind = indent();
    int indent = open(), move = 0;
    if(opening) {
      if(closing) {
        for(int p = 0; p < indent + ind; p++) sb.append(' ');
        move = indent + ind + 1;
        sb.append('\n');
      } else {
        indent += ind;
      }
    } else if(closing) {
      // unindent before closing bracket
      indent -= ind;
    }
    for(int p = 0; p < indent; p++) sb.append(' ');
    add(sb, false);
    return move;
  }

  /**
   * Processes and adds the specified string.
   * @param sb string to be added
   * @param selected states if the text was selected
   * @return returns the number spaces to move forward
   */
  int add(final StringBuilder sb, final boolean selected) {
    if(sb.length() == 0) return 0;

    int move = 0;
    if(!selected && gopts.get(GUIOptions.AUTO)) {
      final char ch = sb.charAt(0);
      final int next = pos + 1 < size() ? text[pos + 1] : 0;
      final int curr = pos < size() ? text[pos] : 0;
      final int prev = pos > 0 ? text[pos - 1] : 0;
      final int pprv = pos > 1 ? text[pos - 2] : 0;
      final int opening = OPENING.indexOf(ch);
      if(opening != -1) {
        // adds a closing to an opening bracket
        if(CLOSING.indexOf(curr) != -1 || curr == 0 || ws(curr) || curr == '<') {
          sb.append(CLOSING.charAt(opening));
          move = 1;
        }
      } else if(CLOSING.indexOf(ch) != -1) {
        // closing bracket: ignore if it equals next character
        if(ch == curr) {
          sb.setLength(0);
          move = 1;
        }
        close();
      } else if(ch == '"' || ch == '\'') {
        // quote: ignore if it equals next character
        if(ch == curr) sb.setLength(0);
        // add second quote
        else if(!XMLToken.isNCChar(prev)) sb.append(ch);
        move = 1;
      } else if(ch == '>') {
        // closes an opening element
        closeElem(sb);
        move = 1;
      } else if(ch == ':') {
        // closes XQuery comments
        if(prev == '(') {
          sb.append(':');
          if(curr != ')') sb.append(')');
          move = 1;
        }
      } else if(ch == '~') {
        // closes XQuery comments
        if(prev == ':' && pprv == '(') {
          sb.append("  ");
          if(curr != ':') {
            sb.append(':');
            if(curr != ')') sb.append(')');
          } else if(next != ')') {
            sb.append(')');
          }
          move = 2;
        }
      } else if(ch == '-') {
        // closes XML comments
        if(prev == '-' && pprv == '!' && pos > 2 && text[pos - 3] == '<') {
          sb.append("  -->\n");
          move = 2;
        }
      } else if(ch == '?') {
        // closes XML processing instructions
        if(prev == '<') {
          sb.append(" ?>\n");
          move = 1;
        }
      }
    }
    add(sb.toString());
    return move;
  }

  /**
   * Closes a bracket and unindents leading whitespaces.
   */
  private void close() {
    int p = pos - 1;
    for(; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(!ws(b)) return;
    }
    if(++p >= pos) return;
    start = Math.max(pos - indent(), p);
    end = Math.max(pos, p);
    if(start != end) delete();
  }

  /**
   * Checks if an opening element can automatically be closed.
   * @param sb string builder
   */
  private void closeElem(final StringBuilder sb) {
    final int p = pos;
    while(pos > 0) {
      final int cp = prev();
      if(!XMLToken.isNCChar(cp) && cp != ':') {
        if(cp == '<' && pos < p - 1) {
          // add closing element
          next();
          sb.append("</").append(new TokenBuilder().add(text, pos, p).toString()).append('>');
        }
        break;
      }
    }
    pos = p;
  }

  /**
   * Deletes the previous character or the current selection.
   */
  void deletePrev() {
    if(!selected()) {
      if(pos == 0) return;
      startSelect();
      final int curr = curr(), prev = prev();
      endSelection();

      if(gopts.get(GUIOptions.AUTO)) {
        if(curr == prev && (curr == '"' || curr == '\'')) {
          // remove closing quote
          start++;
        } else {
          // remove closing bracket
          final int open = OPENING.indexOf(prev);
          if(open != -1 && CLOSING.indexOf(curr) == open) start++;
        }
      }
    }
    del();
  }

  /**
   * Deletes the current character or selection.
   * Assumes that the current position allows a deletion.
   */
  void delete() {
    if(!selected()) {
      if(pos == size()) return;
      start = pos;
      end = pos + cl(text, pos);
    }
    del();
  }

  /**
   * Deletes the current selection.
   */
  private void del() {
    final int s = Math.min(start, end), e = Math.max(start, end), ts = size();
    final byte[] tmp = new byte[ts - e + s];
    System.arraycopy(text, 0, tmp, 0, s);
    System.arraycopy(text, e, tmp, s, ts - e);
    text(tmp);
    pos = s;
  }

  /**
   * Deletes a line.
   */
  void deleteLine() {
    selectLine();
    delete();
  }

  /**
   * Deletes the word or line following the cursor.
   * @param word word/line flag
   */
  void deleteNext(final boolean word) {
    if(!selected()) {
      if(pos() == size()) return;
      startSelect();
      if(word) nextWord(true);
      else lineEnd(true);
      endSelection();
    }
    delete();
  }

  /**
   * Deletes the word or line preceding the cursor.
   * @param word word/line flag
   */
  void deletePrev(final boolean word) {
    if(!selected()) {
      if(pos() == 0) return;
      startSelect();
      if(word) prevWord(true);
      else bol(true);
      endSelection();
    }
    delete();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Replaces the specified text area with another string.
   * @param s start
   * @param e end
   * @param value new value
   */
  private void replace(final int s, final int e, final String value) {
    select(s, e);
    delete();
    add(value);
  }

  /**
   * Extends the current selection to the beginning of the first and the end of the last line.
   * @return if text is selected
   */
  private boolean extend() {
    if(!selected()) {
      selectLine();
      if(!selected()) return false;
    }

    int s = Math.min(start, end), e = Math.max(start, end);
    final int ts = size();
    while(s > 0 && text[s - 1] != '\n') s--;
    if(e > 0) while(e < ts && text[e - 1] != '\n') e++;
    start = s;
    end = e;
    return true;
  }

  /**
   * Indents lines.
   * @param shift shift flag
   */
  private void indent(final boolean shift) {
    if(!extend()) return;

    final int s = start, e = end, ind = indent();
    final byte[] spaces = spaces();

    // build new text
    final TokenBuilder tb = new TokenBuilder();
    for(int p = s; p < e; p++) {
      if(p == 0 || text[p - 1] == '\n') {
        // find leading whitespaces
        int i = 0;
        do {
          final int cp = text[p];
          if(cp == '\t') {
            i += ind;
          } else if(cp == ' ') {
            i++;
          } else {
            break;
          }
        } while(++p < e);

        // calculate indentation, add indentation and remaining spaces
        i = shift ? Math.max(0, i - ind) : i + ind;
        for(int c = 0; c < i / ind; c++) tb.add(spaces);
        for(int c = 0; c < i % ind; c++) tb.add(' ');
      }
      if(p < e) tb.addByte(text[p]);
    }

    insert(tb.finish(), s, e);
    select(s, s + tb.size());
  }

  /**
   * Adds indenting spaces to the specified string builder.
   * @return number of spaces to indent
   */
  private int open() {
    // adopt indentation from previous line
    final int ind = indent();
    int indent = 0;
    for(int p = pos - 1; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(b == '\t') {
        indent += ind;
      } else if(b == ' ') {
        indent++;
      } else {
        indent = 0;
      }
    }
    return indent;
  }

  // TEXT NAVIGATION AND SELECTION ================================================================

  /**
   * Returns the text size.
   * @return text size
   */
  int size() {
    return text.length;
  }

  /**
   * Returns the caret position.
   * @return caret position
   */
  int pos() {
    return pos;
  }

  /**
   * Sets the caret position and resets a selection.
   * @param p caret position
   */
  void pos(final int p) {
    pos = p;
    noSelect();
  }

  /**
   * Resets the selection.
   */
  void noSelect() {
    start = -1;
    end = -1;
  }

  /**
   * Selects the text at the specified position.
   * @param p position
   * @param select states if the selection has just been started
   */
  void select(final int p, final boolean select) {
    pos = p;
    if(select) startSelect();
    else end = pos;
  }

  /**
   * Selects the specified area and places the caret at the end of the selection.
   * @param s start position
   * @param e end position
   */
  void select(final int s, final int e) {
    start = s;
    end = e;
    pos = e;
    checkSelection();
  }

  /**
   * Initializes the selection.
   * @param select select flag
   */
  void startSelection(final boolean select) {
    if(select) {
      if(!selected()) startSelect();
    } else {
      noSelect();
    }
  }

  /**
   * Finishes a text selection.
   */
  void endSelection() {
    end = pos;
    checkSelection();
  }

  /**
   * Checks the validity of the selection.
   */
  private void checkSelection() {
    if(start == end) noSelect();
  }

  /**
   * Tests if text has been selected.
   * @return result of check
   */
  boolean selected() {
    return start != end;
  }

  /**
   * Returns the selected string.
   * @return string
   */
  String copy() {
    final TokenBuilder tb = new TokenBuilder();
    final int e = start < end ? end : start;
    for(int s = start < end ? start : end; s < e; s += cl(text, s)) {
      final int cp = cp(text, s);
      if(cp >= ' ' || cp == 0x0A || cp == 0x09) tb.add(cp);
    }
    return tb.toString();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectWord() {
    final boolean ch = FTToken.lod(curr());
    while(pos() > 0) {
      final int cp = back(true);
      if(cp == '\n' || ch != FTToken.lod(cp)) {
        forward(true);
        break;
      }
    }
    startSelect();
    while(pos() < size()) {
      final int cp = curr();
      if(cp == '\n' || ch != FTToken.lod(cp)) break;
      forward(true);
    }
    endSelection();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    bol(false);
    startSelect();
    forward(Integer.MAX_VALUE, true);
    next();
    endSelection();
  }

  /**
   * Returns the current character, or the newline character if the position is out of bounds.
   * @return current character
   */
  private int curr() {
    return pos < 0 || pos >= size() ? '\n' : cp(text, pos);
  }

  /**
   * Returns the current character and moves to the next character.
   * @return current character
   */
  private int next() {
    final int c = curr();
    if(pos < size()) pos += cl(text, pos);
    return c;
  }

  /**
   * Sets the text selection to the current position.
   */
  private void startSelect() {
    start = pos;
    end = pos;
  }

  // ERROR HIGHLIGHTING =================================================================

  /**
   * Sets the error position.
   * @param s start position
   */
  void error(final int s) {
    error = s;
  }

  // SEARCH HIGHLIGHTING ================================================================

  /**
   * Selects a search string.
   * @param dir search direction
   * @param select select hit
   * @return new cursor position, or {@code -1}
   */
  int jump(final SearchDir dir, final boolean select) {
    if(searchPos[0].isEmpty()) {
      if(select) noSelect();
      return -1;
    }

    int s = searchPos[0].sortedIndexOf(!select || selected() ? pos : pos - 1);
    switch(dir) {
      case CURRENT:  s = s < 0 ? -s - 1 : s;     break;
      case FORWARD:  s = s < 0 ? -s - 1 : s + 1; break;
      case BACKWARD: s = s < 0 ? -s - 2 : s - 1; break;
    }
    final int sl = searchPos[0].size();
    if(s < 0) s = sl - 1;
    else if(s == sl) s = 0;
    final int p = searchPos[0].get(s);
    if(select) {
      start = searchPos[1].get(s);
      end = p;
    }
    pos = p;
    return p;
  }

  // CURSOR =============================================================================

  @Override
  public String toString() {
    return copy();
  }
}
