package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This class defines syntax highlighting for XQuery files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxXQuery extends SyntaxMarkup {
  /** Opening brackets. */
  private static final String OPENING = "{(";
  /** Closing brackets. */
  private static final String CLOSING = "})";
  /** Reserved words and type names. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();
  /** Names of built-in functions. */
  private static final HashSet<String> FUNCTIONS = new HashSet<>();
  /** Maximum length of a keyword. */
  private static final int MAXKEY = 64;

  /** Mode: code. */
  private static final int CODE = MODES;
  /** Mode: double-quoted string. */
  private static final int STRING_D = MODES + 1;
  /** Mode: single-quoted string. */
  private static final int STRING_S = MODES + 2;
  /** Mode: string template ({@code `...`}). */
  private static final int TEMPLATE = MODES + 3;
  /** Mode: string constructor ({@code ``[...]``}). */
  private static final int CONSTRUCTOR = MODES + 4;
  /** Mode: comment (nesting is tracked by the mode stack). */
  private static final int COMMENT = MODES + 5;
  /** Mode: pragma. */
  private static final int PRAGMA = MODES + 6;
  /** Mode: URI of an EQName ({@code Q{...}}). */
  private static final int EQNAME = MODES + 7;

  // initialize keywords
  static {
    try {
      for(final Field f : QueryText.class.getFields()) {
        if("IGNORE".equals(f.getName())) break;
        KEYWORDS.add((String) f.get(null));
      }
      for(final BasicType type : BasicType.values()) {
        final QNm name = type.qname();
        final byte[] prefix = NSGlobal.prefix(name.uri());
        KEYWORDS.add((prefix.length != 0 ? string(prefix) + ':' : "") + string(name.local()));
      }
      for(final Axis axis : Axis.values()) KEYWORDS.add(axis.name);
      for(final CmpOp op : CmpOp.values()) {
        KEYWORDS.add(op.toValueString());
        Collections.addAll(KEYWORDS, op.nodes);
      }
      for(final QNm name : Functions.BUILT_IN) {
        final String local = string(name.local());
        final byte[] prefix = NSGlobal.prefix(name.uri());
        if(prefix.length != 0) FUNCTIONS.add(string(prefix) + ':' + local);
        // functions of the default function namespace can be addressed without prefix
        if(eq(name.uri(), QueryText.FN_URI)) FUNCTIONS.add(local);
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /** Indicates if the last resolved name is a keyword. */
  private boolean nameKeyword;

  @Override
  int initialMode() {
    return CODE;
  }

  @Override
  boolean quoteEscape() {
    return true;
  }

  @Override
  Color color(final int mode) {
    return switch(mode) {
      case STRING_D, STRING_S, TEMPLATE, CONSTRUCTOR, EQNAME -> brown;
      case COMMENT, PRAGMA -> cyan;
      default -> super.color(mode);
    };
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    return switch(mode) {
      case CODE -> code(text, pos, ch);
      case COMMENT -> {
        // comments nest: an inner comment pushes the outer one onto the mode stack
        if(ch == '(' && cp(text, pos + 1) == ':') enter(COMMENT, 1);
        else if(ch == ':' && cp(text, pos + 1) == ')') close(1);
        yield cyan;
      }
      case PRAGMA -> {
        if(ch == '#' && cp(text, pos + 1) == ')') close(1);
        yield cyan;
      }
      case EQNAME -> {
        if(ch == '}') close(0);
        yield brown;
      }
      case STRING_D, STRING_S -> {
        if(reference(text, pos)) yield purple;
        final int quote = mode == STRING_D ? '"' : '\'';
        if(ch == quote) {
          // doubled quotes are escaped
          if(cp(text, pos + 1) == quote) state[SKIP] = 1;
          else close(0);
        }
        yield brown;
      }
      case TEMPLATE -> {
        if(ch == '`') {
          if(cp(text, pos + 1) == '`') state[SKIP] = 1;
          else close(0);
        } else if(enclosed(text, pos, TEMPLATE)) {
          yield plain;
        }
        yield brown;
      }
      case CONSTRUCTOR -> {
        if(ch == ']' && startsWith(text, pos, "]``")) close(2);
        else if(ch == '`' && cp(text, pos + 1) == '{') enter(CODE, 1);
        yield brown;
      }
      default -> super.mode(text, pos, end, ch, mode);
    };
  }

  /**
   * Determines the color of a character in code.
   * @param text text
   * @param pos position
   * @param ch current character
   * @return color
   */
  private Color code(final byte[] text, final int pos, final int ch) {
    if(ch == '(') {
      final int next = cp(text, pos + 1);
      if(next == ':' || next == '#') {
        enter(next == ':' ? COMMENT : PRAGMA, 1);
        return cyan;
      }
      return plain;
    }
    if(ch == '"' || ch == '\'') {
      enter(ch == '"' ? STRING_D : STRING_S, 0);
      return brown;
    }
    if(ch == '`') {
      final boolean constr = startsWith(text, pos, "``[");
      enter(constr ? CONSTRUCTOR : TEMPLATE, constr ? 2 : 0);
      return brown;
    }
    // URI of an EQName: must not be parsed as code
    if(ch == 'Q' && cp(text, pos + 1) == '{') {
      enter(EQNAME, 1);
      return brown;
    }
    if(ch == '<') return open(text, pos);
    if(ch == '{') {
      enter(CODE, 0);
      return plain;
    }
    if(ch == '}') {
      close(0);
      return plain;
    }
    if(ch == '$') return green;
    if(ch == '%' && XMLToken.isNCStartChar(cp(text, pos + 1))) return blue;

    if(name(text, pos)) {
      final int prev = nameStart > 0 ? text[nameStart - 1] : 0;
      if(prev == '$') return green;
      return prev == '%' || nameKeyword ? blue : plain;
    }
    // numeric literals (a dot is only a decimal point if it is not part of a name)
    return digit(ch) || ch == '.' && (digit(cp(text, pos + 1)) || digit(prev(text, pos))) ?
      purple : plain;
  }

  @Override
  boolean element(final byte[] text, final int pos) {
    return state[MODE] == CONTENT || !operand(text, pos);
  }

  @Override
  boolean enclosed(final byte[] text, final int pos, final int mode) {
    final int ch = cp(text, pos);
    if(ch != '{' && ch != '}') return false;
    // doubled curly braces are escaped
    if(cp(text, pos + 1) == ch) {
      state[SKIP] = 1;
      return false;
    }
    if(ch == '}') return false;
    enter(CODE, 0);
    return true;
  }

  @Override
  void classify(final byte[] text, final int start, final int end) {
    nameKeyword = keyword(text, start, end);
  }

  @Override
  boolean operandName(final byte[] text, final int pos) {
    // numbers, variables and user-defined names end an operand; keywords do not
    if(!name(text, pos)) return true;
    if(nameStart > 0 && text[nameStart - 1] == '$') return true;
    return !nameKeyword;
  }

  /**
   * Checks if the specified name is highlighted as a keyword.
   * @param text text
   * @param start start of the name
   * @param end end of the name
   * @return result of check
   */
  private static boolean keyword(final byte[] text, final int start, final int end) {
    if(end - start > MAXKEY) return false;
    String name = string(text, start, end - start);
    int first = start;

    // an EQName is resolved via its braced URI; a lexical prefix is ignored (XQuery 4.0, 'EQName')
    final int brace = braced(text, start);
    if(brace != -1) {
      final byte[] prefix = NSGlobal.prefix(substring(text, brace + 1, start - 1));
      if(prefix.length == 0) return false;
      final int colon = name.indexOf(':');
      name = string(prefix) + ':' + (colon == -1 ? name : name.substring(colon + 1));
      first = brace - 1;
    }

    // built-in functions are only highlighted if they are called: 'count(1)', 'count#1', 'a::b'
    final int next = skipWs(text, end);
    final int nc = cp(text, next);
    if(nc == '(' || nc == '#' || nc == ':' && cp(text, next + 1) == ':')
      return KEYWORDS.contains(name) || FUNCTIONS.contains(name);

    // reserved words are no keywords in name tests: '//name', '@id', 'child::text', '$map?key'
    final int prev = skipWsBack(text, first), pc = cp(text, prev);
    final boolean step = pc == '/' || pc == '@' || pc == '?' ||
      pc == ':' && cp(text, back(text, prev)) == ':';
    return !step && KEYWORDS.contains(name);
  }

  /**
   * Returns the opening brace of the braced URI that precedes an EQName.
   * @param text text
   * @param start start of the name
   * @return position, or {@code -1} if the name has no braced URI
   */
  private static int braced(final byte[] text, final int start) {
    if(start < 3 || text[start - 1] != '}') return -1;
    // a braced URI contains no braces
    for(int p = start - 2; p > 0; p--) {
      if(text[p] == '}') return -1;
      if(text[p] == '{') return text[p - 1] == 'Q' ? p : -1;
    }
    return -1;
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.XQCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.XQCOMM_C;
  }

  @Override
  public byte[] format(final byte[] text, final byte[] spaces) {
    final TokenBuilder tb = new TokenBuilder();
    final int tl = text.length;
    int quoted = 0, comments = 0, indents = 0;
    for(int t = 0; t < tl; t++) {
      final byte curr = text[t];
      final int open = OPENING.indexOf(curr), close = CLOSING.indexOf(curr);
      final int next = t + 1 < tl ? text[t + 1] : 0, prev = t > 0 ? text[t - 1] : 0;
      if(quoted != 0) {
        if(curr == quoted) quoted = 0;
      } else if("\"'`".indexOf(curr) != -1) {
        quoted = curr;
      } else if(curr == '(' && next == ':') {
        comments++;
      } else if(prev == ':' && curr == ')') {
        comments--;
      } else if(comments == 0) {
        if(open != -1) {
          indents++;
          tb.addByte(curr);
          if(next != '\n' && !matches(CLOSING.charAt(open), t, text, true)) {
            tb.add('\n');
            for(int i = 0; i < indents; i++) tb.add(spaces);
          }
          continue;
        } else if(close != -1) {
          indents--;
          if(!endingWithWs(tb) && !matches(OPENING.charAt(close), t, text, false)) {
            tb.add('\n');
            for(int i = 0; i < indents; i++) tb.add(spaces);
          }
        }
      }
      tb.addByte(curr);
    }
    return tb.finish();
  }

  /**
   * Checks if the previous line contains only spaces.
   * @param text text
   * @return result of check
   */
  private static boolean endingWithWs(final TokenBuilder text) {
    for(int t = text.size() - 1; t >= 0; t--) {
      final byte c = text.get(t);
      if(c == '\n') break;
      if(!ws(c)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified character is found near the current position.
   * @param ch character to be found
   * @param pos current position
   * @param text text
   * @param after search after or before the current position
   * @return result of check
   */
  private static boolean matches(final char ch, final int pos, final byte[] text,
      final boolean after) {
    final int dist = after ? 3 : -3;
    for(int d = 0; after ? d < dist : d > dist; d += after ? 1 : -1) {
      final int p = pos + d;
      if(p >= 0 && p < text.length && text[p] == ch) return true;
    }
    return false;
  }
}
