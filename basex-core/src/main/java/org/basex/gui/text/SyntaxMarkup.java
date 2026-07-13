package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;

import org.basex.util.*;

/**
 * Syntax highlighting for markup languages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class SyntaxMarkup extends Syntax {
  /** Mode: element content. */
  static final int CONTENT = 0;
  /** Mode: start tag. */
  static final int TAG = 1;
  /** Mode: end tag. */
  static final int ETAG = 2;
  /** Mode: double-quoted attribute value. */
  static final int ATTR_D = 3;
  /** Mode: single-quoted attribute value. */
  static final int ATTR_S = 4;
  /** Mode: CDATA section. */
  static final int CDATA = 5;
  /** Mode: comment. */
  static final int XMLCOMMENT = 6;
  /** Mode: processing instruction. */
  static final int XMLPI = 7;
  /** Mode: doctype declaration. */
  static final int DOCTYPE = 8;
  /** First mode that may be assigned by a subclass. */
  static final int MODES = 9;

  /** Start of the last resolved name range. */
  int nameStart;
  /** End of the last resolved name range. */
  int nameEnd;
  /** Indicates if the last resolved range is a name. */
  private boolean nameFound;

  @Override
  void reset() {
    super.reset();
    nameStart = 0;
    nameEnd = 0;
  }

  @Override
  final boolean elementOpen(final byte[] text, final int pos) {
    // a slash indicates a self-closing tag, which opens no element
    return modeBefore() == TAG && modeAfter() == CONTENT && prev(text, pos) != '/';
  }

  @Override
  final boolean elementClose() {
    return modeAfter() == ETAG && modeBefore() != ETAG;
  }

  @Override
  Indent indent(final byte[] text, final int pos, final int last, final int mode,
      final int newlines, final Indent previous) {
    // the attributes of a tag are indented
    return tag() ? new Indent(1, 1, 0, true) : Indent.NONE;
  }

  @Override
  final boolean tag() {
    return modeBefore() == TAG;
  }

  @Override
  final boolean formatted() {
    // markup has no separators, but its tags and its boundary whitespace are indented
    return true;
  }

  @Override
  boolean content(final int mode) {
    return mode == CONTENT;
  }

  @Override
  boolean code(final int mode) {
    // element content is no code: its brackets are literal text, its whitespace may be significant
    return mode == TAG || mode == ETAG;
  }

  @Override
  Color color(final int mode) {
    return switch(mode) {
      case ATTR_D, ATTR_S -> brown;
      case CDATA, XMLCOMMENT, XMLPI, DOCTYPE -> cyan;
      case TAG, ETAG -> blue;
      default -> plain;
    };
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    return switch(mode) {
      case CONTENT -> {
        if(ch == '<') yield open(text, pos);
        if(reference(text, pos)) yield purple;
        enclosed(text, pos, CONTENT);
        yield plain;
      }
      case TAG -> {
        if(ch == '"' || ch == '\'') {
          enter(ch == '"' ? ATTR_D : ATTR_S, 0);
          yield brown;
        }
        if(ch == '>') {
          state[MODE] = CONTENT;
          yield blue;
        }
        if(ch == '/' && cp(text, pos + 1) == '>') {
          close(1);
          yield blue;
        }
        if(ch == '=') yield blue;
        // the element name directly follows the opening angle bracket
        yield name(text, pos) ? nameStart > 0 && text[nameStart - 1] == '<' ? blue : purple :
          plain;
      }
      case ETAG -> {
        if(ch == '>') close(0);
        yield blue;
      }
      case ATTR_D, ATTR_S -> {
        final int quote = mode == ATTR_D ? '"' : '\'';
        if(reference(text, pos)) yield purple;
        if(ch == quote) {
          // in XQuery, doubled quotes are escaped
          if(quoteEscape() && cp(text, pos + 1) == quote) state[SKIP] = 1;
          else close(0);
        } else if(enclosed(text, pos, mode)) {
          yield plain;
        }
        yield brown;
      }
      case CDATA -> {
        if(ch == ']' && startsWith(text, pos, "]]>")) close(2);
        yield cyan;
      }
      case XMLCOMMENT -> {
        if(ch == '-' && startsWith(text, pos, "-->")) close(2);
        yield cyan;
      }
      case XMLPI -> {
        if(ch == '?' && cp(text, pos + 1) == '>') close(1);
        yield cyan;
      }
      case DOCTYPE -> {
        if(ch == '>') close(0);
        yield cyan;
      }
      default -> super.mode(text, pos, end, ch, mode);
    };
  }

  /**
   * Opens a tag, end tag, CDATA section, comment, processing instruction or doctype declaration.
   * @param text text
   * @param pos position of the opening angle bracket
   * @return color
   */
  final Color open(final byte[] text, final int pos) {
    final int mode, skip;
    if(startsWith(text, pos, "</")) {
      // the enclosing mode was pushed by the start tag
      state[MODE] = ETAG;
      state[SKIP] = 1;
      return blue;
    }
    if(startsWith(text, pos, "<![CDATA[")) {
      mode = CDATA;
      skip = 8;
    } else if(startsWith(text, pos, "<!--")) {
      mode = XMLCOMMENT;
      skip = 3;
    } else if(startsWith(text, pos, "<?")) {
      mode = XMLPI;
      skip = 1;
    } else if(startsWith(text, pos, "<!")) {
      mode = DOCTYPE;
      skip = 1;
    } else if(XMLToken.isNCStartChar(cp(text, pos + 1)) && element(text, pos)) {
      mode = TAG;
      skip = 0;
    } else {
      // comparison or node operator
      return plain;
    }
    enter(mode, skip);
    return color(mode);
  }

  /**
   * Checks if the specified position is part of an entity or character reference.
   * @param text text
   * @param pos position
   * @return result of check
   */
  static boolean reference(final byte[] text, final int pos) {
    // look back to the ampersand, and forward to the semicolon: no state required
    int s = cp(text, pos) == ';' ? back(text, pos) : pos;
    while(s >= 0 && refChar(cp(text, s))) s = back(text, s);
    if(s < 0 || cp(text, s) != '&') return false;
    final int tl = text.length;
    int e = s + 1;
    while(e < tl && refChar(cp(text, e))) e += cl(text, e);
    return e > s + 1 && e < tl && text[e] == ';' && pos <= e;
  }

  /**
   * Checks if a character may occur inside an entity or character reference.
   * @param ch character
   * @return result of check
   */
  private static boolean refChar(final int ch) {
    return ch == '#' || XMLToken.isNCChar(ch);
  }

  // OVERRIDABLE METHODS ==========================================================================

  /**
   * Checks if an angle bracket opens an element constructor.
   * @param text text
   * @param pos position
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean element(final byte[] text, final int pos) {
    return true;
  }

  /**
   * Processes an enclosed expression and its escaped curly braces.
   * @param text text
   * @param pos position
   * @param mode enclosing mode
   * @return {@code true} if an enclosed expression was entered
   */
  @SuppressWarnings("unused")
  boolean enclosed(final byte[] text, final int pos, final int mode) {
    return false;
  }

  /**
   * Indicates if a doubled quote escapes a quote in an attribute value.
   * @return result of check
   */
  boolean quoteEscape() {
    return false;
  }

  /**
   * Classifies a name that was resolved by {@link #name(byte[], int)}.
   * @param text text
   * @param start start of the name
   * @param end end of the name
   */
  @SuppressWarnings("unused")
  void classify(final byte[] text, final int start, final int end) { }

  // NAMES ========================================================================================

  /**
   * Assigns the range of the QName that encloses the specified position.
   * @param text text
   * @param pos position
   * @return {@code true} if the position is part of a name
   */
  final boolean name(final byte[] text, final int pos) {
    // all tokens of a name yield the same range: the text is invariant within a rendering pass
    if(pos >= nameStart && pos < nameEnd) return nameFound;

    // colons are only part of a name if they separate prefix and local name
    int p = pos;
    if(cp(text, p) == ':') {
      if(!separator(text, p)) return false;
      p++;
    }
    if(!XMLToken.isNCChar(cp(text, p))) return false;

    // characters that must not start a name (digits, hyphens, dots) are no names
    final int run = nameStart(text, p);
    int start = first(text, run);
    if(start == -1 || start > p) {
      nameStart = run;
      nameEnd = start == -1 ? end(text, run) : start;
      nameFound = false;
      return false;
    }
    // preceding prefix
    if(start > 0 && text[start - 1] == ':' && separator(text, start - 1)) {
      final int prefix = first(text, nameStart(text, start - 1));
      if(prefix != -1) start = prefix;
    }
    // end of the name, including a subsequent local name
    int end = end(text, p);
    if(separator(text, end)) end = end(text, end + 1);

    nameStart = start;
    nameEnd = end;
    nameFound = true;
    classify(text, start, end);
    return true;
  }

  /**
   * Returns the first character of a sequence of name characters that may start a name.
   * @param text text
   * @param pos start of the sequence
   * @return position, or {@code -1} if there is none
   */
  private static int first(final byte[] text, final int pos) {
    final int tl = text.length;
    for(int p = pos; p < tl; p += cl(text, p)) {
      final int ch = cp(text, p);
      if(!XMLToken.isNCChar(ch)) break;
      if(XMLToken.isNCStartChar(ch)) return p;
    }
    return -1;
  }

  /**
   * Returns the end of the sequence of name characters that starts at the specified position.
   * @param text text
   * @param pos position inside the sequence
   * @return end position
   */
  private static int end(final byte[] text, final int pos) {
    int end = pos;
    final int tl = text.length;
    while(end < tl && XMLToken.isNCChar(cp(text, end))) end += cl(text, end);
    return end;
  }

  /**
   * Checks if the specified position separates the prefix and local name of a QName.
   * @param text text
   * @param pos position
   * @return result of check
   */
  private static boolean separator(final byte[] text, final int pos) {
    return cp(text, pos) == ':' && XMLToken.isNCChar(prev(text, pos)) &&
      XMLToken.isNCStartChar(cp(text, pos + 1));
  }
}
