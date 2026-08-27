package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for Markdown files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxMD extends Syntax {
  /** Mode: text. */
  private static final int TEXT = 0;
  /** Mode: table. */
  private static final int TABLE = 1;
  /** Mode: heading. */
  private static final int HEADING = 2;
  /** Mode: thematic break. */
  private static final int RULE = 3;
  /** Mode: code block fenced with backticks. */
  private static final int FENCE_B = 4;
  /** Mode: code block fenced with tildes. */
  private static final int FENCE_T = 5;
  /** Mode: link, image or reference label. */
  private static final int LABEL = 6;
  /** Mode: link destination. */
  private static final int URL = 7;
  /** Mode: HTML tag or bracketed autolink. */
  private static final int TAG = 8;
  /** Mode: bare autolink. */
  private static final int AUTOLINK = 9;
  /** Mode: emphasis delimited by asterisks. */
  private static final int EMPHASIS_A = 10;
  /** Mode: emphasis delimited by underscores. */
  private static final int EMPHASIS_U = 11;
  /** Mode: strikethrough. */
  private static final int STRIKE = 12;
  /** Mode: code span delimited by a single backtick. */
  private static final int CODE1 = 13;
  /** Mode: code span delimited by two backticks. */
  private static final int CODE2 = 14;
  /** Mode: code span delimited by three or more backticks. */
  private static final int CODE3 = 15;

  /** Punctuation that is dropped at the end of a bare autolink. */
  private static final String TRAILING = "?!.,:*_~)";

  @Override
  boolean code(final int mode) {
    // brackets are only paired in text: elsewhere, they are literal characters
    return mode == TEXT || mode == TABLE || mode == LABEL || mode == URL ||
      mode == EMPHASIS_A || mode == EMPHASIS_U || mode == STRIKE;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.COMM_C;
  }

  @Override
  Color color(final int mode) {
    return switch(mode) {
      case HEADING, LABEL, TAG, AUTOLINK -> blue;
      case RULE -> cyan;
      case FENCE_B, FENCE_T, CODE1, CODE2, CODE3 -> brown;
      case URL, EMPHASIS_A, EMPHASIS_U, STRIKE -> purple;
      default -> plain;
    };
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    // only code blocks and tables span more than one line
    if(ch == '\n') {
      while(bounded(state[MODE])) close(0);
      if(state[MODE] == TABLE && blank(text, pos + 1)) close(0);
      return color(mode);
    }
    return switch(mode) {
      case FENCE_B, FENCE_T -> {
        // the block is closed by a fence of at least three characters
        final int fence = mode == FENCE_B ? '`' : '~';
        if(ch == fence && lineStart(text, pos)) {
          final int fences = run(text, pos, fence);
          if(fences >= 3) close(fences - 1);
        }
        yield brown;
      }
      case CODE1, CODE2, CODE3 -> {
        // the span is closed by a backtick string of the same length
        if(ch == '`') {
          final int ticks = run(text, pos, '`'), delim = mode - CODE1 + 1;
          if(ticks == delim || ticks > delim && delim == 3) close(ticks - 1);
        }
        yield brown;
      }
      case HEADING -> blue;
      case RULE -> cyan;
      case TAG -> {
        if(ch == '>') close(0);
        yield blue;
      }
      case AUTOLINK -> {
        // the link ends at whitespace; punctuation that ends it is no part of it
        if(!linkChar(ch) || TRAILING.indexOf(ch) != -1 && !linkChar(cp(text, pos + 1))) {
          close(0);
          yield plain;
        }
        yield blue;
      }
      case LABEL -> {
        if(ch == ']') {
          close(0);
          yield blue;
        }
        yield inline(text, pos, ch, blue);
      }
      case URL -> {
        if(ch == '\\') state[SKIP] = 1;
        else if(ch == '(') enter(URL, 0);
        else if(ch == ')') close(0);
        yield purple;
      }
      case EMPHASIS_A, EMPHASIS_U, STRIKE -> {
        if(ch == delimiter(mode) && closes(text, pos, ch)) {
          close(0);
          yield purple;
        }
        yield inline(text, pos, ch, purple);
      }
      default -> {
        if(mode == TEXT && lineStart(text, pos)) {
          final Color color = block(text, pos, ch);
          if(color != null) yield color;
          // the header row of a table is followed by a delimiter row
          if(delimiters(text, nextLine(text, pos))) enter(TABLE, 0);
        }
        if(ch == '|' && state[MODE] == TABLE) yield cyan;
        if(markerChar(ch) && marker(text, pos)) yield cyan;
        yield inline(text, pos, ch, plain);
      }
    };
  }

  /**
   * Indicates if a mode is bounded by the end of the line.
   * @param mode mode
   * @return result of check
   */
  private static boolean bounded(final int mode) {
    return mode != TEXT && mode != TABLE && mode != FENCE_B && mode != FENCE_T;
  }

  // BLOCKS =======================================================================================

  /**
   * Returns the color of a character that starts a block, and advances the state.
   * @param text text
   * @param pos position of the character
   * @param ch character
   * @return color, or {@code null} if no block starts at the position
   */
  private Color block(final byte[] text, final int pos, final int ch) {
    // heading: up to six hashes, followed by whitespace
    if(ch == '#') {
      final int hashes = run(text, pos, '#');
      final int next = cp(text, pos + hashes);
      if(hashes <= 6 && (next == 0 || ws(next))) {
        enter(HEADING, hashes - 1);
        return blue;
      }
    }
    // code block: fence of at least three backticks or tildes
    if(ch == '`' || ch == '~') {
      final int fences = run(text, pos, ch);
      if(fences >= 3) {
        enter(ch == '`' ? FENCE_B : FENCE_T, fences - 1);
        return brown;
      }
    }
    // heading: line of dashes or equal signs below a paragraph
    if((ch == '-' || ch == '=') && rule(text, pos, ch, 1) && paragraph(text, pos)) {
      enter(HEADING, 0);
      return blue;
    }
    // thematic break: line of at least three dashes, asterisks or underscores
    if((ch == '-' || ch == '*' || ch == '_') && rule(text, pos, ch, 3)) {
      enter(RULE, 0);
      return cyan;
    }
    return null;
  }

  /**
   * Checks if the specified position is part of the block quote and list markers of its line.
   * @param text text
   * @param pos position
   * @return result of check
   */
  private static boolean marker(final byte[] text, final int pos) {
    // walk back to the start of the line: markers are only preceded by other markers
    int start = pos;
    for(int p = pos - 1; p >= 0; p--) {
      final int ch = text[p];
      if(ch == '\n') break;
      if(!ws(ch) && !markerChar(ch)) return false;
      start = p;
    }
    // consume the markers that precede the position
    for(int p = start; p <= pos;) {
      int ch = cp(text, p);
      while(ch != '\n' && ws(ch)) ch = cp(text, ++p);
      int e = p;
      if(ch == '>') {
        e = p + 1;
      } else if((ch == '-' || ch == '+' || ch == '*') && ws(cp(text, p + 1))) {
        e = p + 1;
      } else if(digit(ch)) {
        int d = p;
        while(digit(cp(text, d))) d++;
        final int delim = cp(text, d);
        if((delim == '.' || delim == ')') && ws(cp(text, d + 1))) e = d + 1;
      }
      if(e == p) return false;
      if(pos < e) return true;
      p = e;
    }
    return false;
  }

  /**
   * Checks if a character may occur in a block quote or list marker.
   * @param ch character
   * @return result of check
   */
  private static boolean markerChar(final int ch) {
    return ch == '>' || ch == '-' || ch == '+' || ch == '*' || ch == '.' || ch == ')' || digit(ch);
  }

  /**
   * Checks if the line at the specified position delimits the columns of a table.
   * @param text text
   * @param pos start of the line
   * @return result of check
   */
  private static boolean delimiters(final byte[] text, final int pos) {
    boolean dash = false, pipe = false;
    for(int p = pos, ch; (ch = cp(text, p)) != 0 && ch != '\n'; p++) {
      if(ch == '-') dash = true;
      else if(ch == '|') pipe = true;
      else if(ch != ':' && !ws(ch)) return false;
    }
    return dash && pipe;
  }

  /**
   * Checks if the rest of the line consists of whitespace and the specified character.
   * @param text text
   * @param pos position
   * @param ch character
   * @param min minimum number of occurrences
   * @return result of check
   */
  private static boolean rule(final byte[] text, final int pos, final int ch, final int min) {
    int count = 0;
    for(int p = pos, c; (c = cp(text, p)) != 0 && c != '\n'; p += cl(text, p)) {
      if(c == ch) count++;
      else if(!ws(c)) return false;
    }
    return count >= min;
  }

  /**
   * Checks if the line before the specified position contains text.
   * @param text text
   * @param pos position in the current line
   * @return result of check
   */
  private static boolean paragraph(final byte[] text, final int pos) {
    int start = pos;
    while(start > 0 && text[start - 1] != '\n') start--;
    for(int p = start - 2; p >= 0; p--) {
      final int ch = text[p];
      if(ch == '\n') break;
      if(!ws(ch)) return true;
    }
    return false;
  }

  /**
   * Checks if the line at the specified position is empty.
   * @param text text
   * @param pos start of the line
   * @return result of check
   */
  private static boolean blank(final byte[] text, final int pos) {
    for(int p = pos, ch; (ch = cp(text, p)) != 0 && ch != '\n'; p++) {
      if(!ws(ch)) return false;
    }
    return true;
  }

  /**
   * Returns the start of the line that follows the specified position.
   * @param text text
   * @param pos position
   * @return position
   */
  private static int nextLine(final byte[] text, final int pos) {
    int p = pos;
    final int tl = text.length;
    while(p < tl && text[p] != '\n') p++;
    return p + 1;
  }

  // INLINE MARKUP ================================================================================

  /**
   * Returns the color of a character of inline content, and advances the state.
   * @param text text
   * @param pos position of the character
   * @param ch character
   * @param color color of the enclosing mode
   * @return color
   */
  private Color inline(final byte[] text, final int pos, final int ch, final Color color) {
    // a backslash escapes the next character
    if(ch == '\\') {
      state[SKIP] = 1;
      return purple;
    }
    if(ch == '`') {
      final int ticks = run(text, pos, '`');
      enter(CODE1 + Math.min(ticks, 3) - 1, ticks - 1);
      return brown;
    }
    if(ch == '*' || ch == '_' || ch == '~') {
      // strikethrough is delimited by one or two tildes
      final int delims = run(text, pos, ch);
      if((ch != '~' || delims <= 2) && opens(text, pos, ch, delims)) {
        // one mode per delimiter: each of them is closed by one delimiter of the closing run
        final int emphasis = ch == '*' ? EMPHASIS_A : ch == '_' ? EMPHASIS_U : STRIKE;
        enter(emphasis, delims - 1);
        for(int d = 1; d < delims; d++) push(emphasis);
        return purple;
      }
    }
    if(ch == '[') {
      enter(LABEL, 0);
      return blue;
    }
    // the exclamation mark of an image
    if(ch == '!' && cp(text, pos + 1) == '[') return blue;
    // the destination of a link directly follows its label
    if(ch == '(' && prev(text, pos) == ']') {
      enter(URL, 0);
      return purple;
    }
    if(ch == '<' && tag(text, pos)) {
      enter(TAG, 0);
      return blue;
    }
    if((ch == 'h' || ch == 'w') && autolink(text, pos)) {
      enter(AUTOLINK, 0);
      return blue;
    }
    if(SyntaxMarkup.reference(text, pos)) return purple;
    return color;
  }

  /**
   * Returns the delimiter of an emphasis or strikethrough mode.
   * @param mode mode
   * @return delimiter
   */
  private static int delimiter(final int mode) {
    return mode == EMPHASIS_A ? '*' : mode == EMPHASIS_U ? '_' : '~';
  }

  /**
   * Checks if a delimiter run opens emphasis or strikethrough.
   * @param text text
   * @param pos start of the run
   * @param ch delimiter
   * @param delims length of the run
   * @return result of check
   */
  private static boolean opens(final byte[] text, final int pos, final int ch, final int delims) {
    final int next = cp(text, pos + delims);
    if(next == 0 || ws(next)) return false;
    // underscores do not emphasize inside a word
    return ch != '_' || !Character.isLetterOrDigit(prev(text, pos));
  }

  /**
   * Checks if a delimiter closes emphasis or strikethrough.
   * @param text text
   * @param pos position of the delimiter
   * @param ch delimiter
   * @return result of check
   */
  private static boolean closes(final byte[] text, final int pos, final int ch) {
    final int previous = prev(text, pos);
    if(previous == 0 || ws(previous)) return false;
    // underscores do not emphasize inside a word
    return ch != '_' || !Character.isLetterOrDigit(cp(text, pos + 1));
  }

  /**
   * Checks if an angle bracket opens an HTML tag or a bracketed autolink.
   * @param text text
   * @param pos position of the angle bracket
   * @return result of check
   */
  private static boolean tag(final byte[] text, final int pos) {
    final int ch = cp(text, pos + 1);
    return ch == '/' || ch == '!' || ch == '?' || Character.isLetter(ch);
  }

  /**
   * Checks if a bare autolink starts at the specified position.
   * @param text text
   * @param pos position
   * @return result of check
   */
  private static boolean autolink(final byte[] text, final int pos) {
    // the link must be preceded by whitespace or an opening delimiter
    final int previous = prev(text, pos);
    if(previous != 0 && !ws(previous) && "*_~(".indexOf(previous) == -1) return false;
    return startsWith(text, pos, "http://") || startsWith(text, pos, "https://") ||
      startsWith(text, pos, "www.");
  }

  /**
   * Checks if a character continues a bare autolink.
   * @param ch character
   * @return result of check
   */
  private static boolean linkChar(final int ch) {
    return ch != 0 && !ws(ch) && ch != '<';
  }

  // CHARACTERS ===================================================================================

  /**
   * Checks if the specified position is only preceded by whitespace in its line.
   * @param text text
   * @param pos position
   * @return result of check
   */
  private static boolean lineStart(final byte[] text, final int pos) {
    for(int p = pos - 1; p >= 0; p--) {
      final int ch = text[p];
      if(ch == '\n') break;
      if(!ws(ch)) return false;
    }
    return true;
  }

  /**
   * Returns the length of the run of the specified character at the given position.
   * @param text text
   * @param pos position
   * @param ch character
   * @return number of occurrences
   */
  private static int run(final byte[] text, final int pos, final int ch) {
    int p = pos;
    while(cp(text, p) == ch) p++;
    return p - pos;
  }
}
