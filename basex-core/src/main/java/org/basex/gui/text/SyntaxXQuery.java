package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.query.QueryText.*;
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
  /** Reserved words and type names. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();
  /** Names of built-in functions. */
  private static final HashSet<String> FUNCTIONS = new HashSet<>();
  /** Maximum length of a keyword. */
  private static final int MAXKEY = 64;

  /** Prolog declaration for boundary whitespace. */
  private static final byte[] BOUNDARY = token("boundary-space");
  /** Clauses of a FLWOR expression (without {@code return}). */
  private static final HashSet<String> CLAUSES = new HashSet<>(Arrays.asList(
    COUNT, FOR, GROUP, LET, ORDER, STABLE, WHERE, WINDOW));

  /** Line type: no clause. */
  private static final int NONE = 0;
  /** Line type: clause that is followed by further clauses. */
  private static final int CLAUSE = 1;
  /** Line type: last clause of a FLWOR expression ({@code return}). */
  private static final int FINAL = 2;
  /** Operators that are followed by an expression (no asterisk: '/*', 'xs:string*'). */
  private static final String OPERATORS = "=+-<>|!/";
  /** Operator keywords that follow an operand. */
  private static final HashSet<String> INFIX = new HashSet<>(Arrays.asList(
    AND, CAST, CASTABLE, DIV, EXCEPT, IDIV, INSTANCE, INTERSECT, MOD, OR, OTHERWISE, TO, TREAT,
    UNION));
  /** Keywords that are followed by an expression. */
  private static final HashSet<String> DANGLING = new HashSet<>(Arrays.asList(
    AS, CASE, DEFAULT, ELSE, IN, OF, RETURN, SATISFIES, THEN, WHERE));
  static {
    // comparison operators are infix operators as well; every infix operator dangles
    for(final CmpOp op : CmpOp.values()) INFIX.add(op.toValueString());
    INFIX.add(CmpOp.EQ.toNodeString());
    DANGLING.addAll(INFIX);
  }

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
  boolean code(final int mode) {
    // tags are code as well (see SyntaxMarkup)
    return mode == CODE || super.code(mode);
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
        if(reference(text, pos)) yield purple;
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
      // a name glued to a preceding digit is the tail of a numeric literal: '10_000', '1e5', '0xF'
      if(digit(prev)) return purple;
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
  Indent indent(final byte[] text, final int pos, final int last, final int mode,
      final int newlines, final Indent previous) {
    // the attributes of a tag are indented, as in XML
    if(tag()) return super.indent(text, pos, last, mode, newlines, previous);

    final boolean code = mode == CODE;
    final int type = clause(text, pos), ref = previous.reference();
    // the commas of a clause separate its own operands, not the operands of an enclosing list
    final boolean separates = type != CLAUSE;
    // a clause continuation opens a new baseline; a non-clause continuation is transparent and
    // preserves the clause context, so that a following clause stays aligned with the continued one
    if(continued(text, pos, last, code, newlines)) return type != NONE ?
      new Indent(1, 1, type, separates) : new Indent(1, ref, previous.type(), separates);
    // consecutive clauses are indented alike
    if(type != NONE && previous.type() == CLAUSE) return new Indent(ref, ref, type, separates);
    // further operands of a clause are indented; the clause remains the reference
    if(code && prev(text, last) == ',' && previous.type() == CLAUSE)
      return new Indent(ref + 1, ref, CLAUSE, false);
    return new Indent(0, 0, type, separates);
  }

  /**
   * Checks if a line continues the expression of the previous one.
   * @param text text
   * @param pos start of the line
   * @param last position after the last character of the previous line
   * @param code indicates if the previous line ends with code
   * @param newlines number of line breaks between the two lines
   * @return result of check
   */
  private boolean continued(final byte[] text, final int pos, final int last, final boolean code,
      final int newlines) {
    // annotations continue a declaration
    if(cp(text, pos) == '%') return true;
    if(newlines != 1 || !code) return false;
    // an operator keyword at the start of the line continues the preceding expression
    if(INFIX.contains(startName(text, pos))) return true;
    final int p = skipWsBack(text, last), ch = cp(text, p);
    if(OPERATORS.indexOf(ch) != -1) return true;
    return XMLToken.isNCChar(ch) && name(text, p) &&
      DANGLING.contains(string(text, nameStart, nameEnd - nameStart));
  }

  /**
   * Returns the type of the FLWOR clause that starts at the specified position.
   * @param text text
   * @param pos start of the line
   * @return {@link #NONE}, {@link #CLAUSE} or {@link #FINAL}
   */
  private int clause(final byte[] text, final int pos) {
    final String name = startName(text, pos);
    return RETURN.equals(name) ? FINAL : CLAUSES.contains(name) ? CLAUSE : NONE;
  }

  /**
   * Returns the name that starts at the specified position.
   * @param text text
   * @param pos position
   * @return name, or {@code null} if no name starts at the position
   */
  private String startName(final byte[] text, final int pos) {
    return name(text, pos) && nameStart == pos ? string(text, nameStart, nameEnd - nameStart) :
      null;
  }

  @Override
  String separators() {
    // colons are no separators: they occur in QNames, axes, map entries and ':='
    return ",";
  }

  @Override
  String lists() {
    // curly braces enclose no lists: their commas may separate let clauses or map entries
    return "([";
  }

  @Override
  boolean boundarySpace(final byte[] text) {
    // all occurrences are checked: the first one may be part of a comment or a string
    for(int p = indexOf(text, BOUNDARY); p != -1; p = indexOf(text, BOUNDARY, p + 1)) {
      if(startsWith(text, skipWs(text, p + BOUNDARY.length), "preserve")) return false;
    }
    return true;
  }
}
