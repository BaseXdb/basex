package org.basex.query.func.fn;

import static java.util.regex.Pattern.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.parse.*;
import org.basex.util.*;
import org.basex.util.Token;
import org.basex.util.hash.*;

/**
 * Regular expression functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class RegExFn extends StandardFunc {
  /** Regex characters. */
  static final byte[] REGEX_CHARS = Token.token("\\^$.|?*+()[]{}");

  /** Pattern cache. */
  private final TokenObjectMap<RegExpr> patterns = new TokenObjectMap<>();

  /**
   * Regular expression pattern.
   */
  static class RegExpr {
    /** Pattern. */
    final Pattern pattern;
    /** Group info. */
    private GroupInfo groupInfo;

    /**
     * Constructor.
     * @param pattern pattern
     */
    RegExpr(final Pattern pattern) {
      this.pattern = pattern;
      groupInfo = null;
    }

    /**
     * Returns the parent group IDs of capturing groups.
     * @return parent group IDs.
     */
    int[] getParentGroups() {
      if(groupInfo == null) groupInfo = GroupScanner.groupInfo(pattern.pattern());
      return groupInfo.parentGroups;
    }

    /**
     * Returns the assertion flags of capturing groups.
     * @return assertion flags.
     */
    boolean[] getAssertionFlags() {
      if(groupInfo == null) groupInfo = GroupScanner.groupInfo(pattern.pattern());
      return groupInfo.assertionFlags;
    }
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern pattern
   * @param flags flags (can be {@code null})
   * @return pattern modifier
   * @throws QueryException query exception
   */
  final Pattern pattern(final byte[] pattern, final byte[] flags)
      throws QueryException {
    return regExpr(pattern, flags).pattern;
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern pattern
   * @param flags flags
   * @return pattern modifier
   * @throws QueryException query exception
   */
  final RegExpr regExpr(final byte[] pattern, final byte[] flags)
      throws QueryException {

    final byte[] key = Token.concat(pattern, '\b', flags);
    synchronized(patterns) {
      RegExpr regExpr = patterns.get(key);
      if(regExpr == null) {
        regExpr = parse(pattern, flags);
        patterns.put(key, regExpr);
      }
      return regExpr;
    }
  }

  /**
   * Tries to convert the regex pattern to a single character.
   * @param pattern pattern
   * @return character or {@code -1}
   */
  static int patternChar(final byte[] pattern) {
    final int sl = pattern.length, separator = sl > 0 && cl(pattern, 0) == sl ? cp(pattern, 0) : -1;
    return separator == -1 || contains(REGEX_CHARS, separator) ? -1 : separator;
  }

  /**
   * Compiles this regular expression to a {@link Pattern}.
   * @param regex regular expression to parse
   * @param modifiers modifiers
   * @return the pattern
   * @throws QueryException query exception
   */
  private RegExpr parse(final byte[] regex, final byte[] modifiers)
      throws QueryException {

    // process modifiers
    int flags = 0;
    boolean strip = false, comments = false, java = false;
    for(final byte mod : modifiers) {
      if(mod == 'i') flags |= CASE_INSENSITIVE | UNICODE_CASE;
      else if(mod == 'm') flags |= MULTILINE;
      else if(mod == 's') flags |= DOTALL;
      else if(mod == 'q') flags |= LITERAL;
      else if(mod == 'x') strip = true;
      else if(mod == 'c') comments = true;
      else if(mod == 'j' || mod == '!') java = true;
      else if(mod != ';') throw REGFLAG_X.get(info, (char) mod);
    }

    try {
      // Java syntax, literal query: no need to change anything
      final Pattern pattern;
      if(java || (flags & LITERAL) != 0) {
        pattern = Pattern.compile(string(regex), flags);
      } else {
        final RegExParser parser = new RegExParser(regex, strip, comments, (flags & DOTALL) != 0,
            (flags & MULTILINE) != 0);
        final String string = parser.parse().toString();

        pattern = Pattern.compile(string, flags);
      }

      return new RegExpr(pattern);
    } catch(final PatternSyntaxException | ParseException | TokenMgrError ex) {
      Util.debug(ex);
      throw REGINVALID_X.get(info, regex);
    }
  }

  /**
   * Information about capturing groups.
   */
  public static class GroupInfo {
    /** Parent group: element i contains the parent group ID of capturing group i+1. */
    public final int[] parentGroups;
    /** Assertion status: element i tells whether capturing group i+1 occurs in an assertion. */
    public final boolean[] assertionFlags;

    /**
     * Constructor.
     * @param parentGroup parent group IDs
     * @param assertionFlags inside of assertion indicators
     */
    GroupInfo(final int[] parentGroup, final boolean[] assertionFlags) {
      this.parentGroups = parentGroup;
      this.assertionFlags = assertionFlags;
    }
  }

  /**
   * Analyze the nesting of capturing groups in a Java regular expression.
   */
  protected static final class GroupScanner {
    /** The pattern. */
    private final String pattern;
    /** Pattern length. */
    private final int len;
    /** Current position. */
    private int pos;
    /** Length of most recent code point. */
    private int chrCount;

    /** Constructor.
     * @param pattern a Java regular expression.
     */
    private GroupScanner(final String pattern) {
      this.pattern = pattern;
      len = pattern.length();
      pos = 0;
    }

    /**
     * Find the parent groups of capturing groups in a Java regular expression.
     * @param pattern the regular expression.
     * @return an array indicating the parent group ID for each capturing group, where element i
     * contains the parent group ID of capturing group i+1.
     */
    public static GroupInfo groupInfo(final String pattern) {
      final GroupScanner gnd = new GroupScanner(pattern);
      final Stack<Integer> open = new Stack<>();
      open.push(0);
      int[] parentGroups = { };
      boolean[] inAssertion = { };
      boolean quoted = false;
      int classLevel = 0;
      int assrtMark = 0;
      for(;;) {
        switch(gnd.nxtToken()) {
          case EOP:
            return new GroupInfo(parentGroups, inAssertion);
          case LBRACKET:
            if(!quoted) ++classLevel;
            break;
          case RBRACKET:
            if(!quoted) --classLevel;
            break;
          case LQUOTE:
            if(classLevel == 0) quoted = true;
            break;
          case RQUOTE:
            if(classLevel == 0) quoted = false;
            break;
          case CAPT_LPAREN:
            if(!quoted && classLevel == 0) {
              parentGroups = Arrays.copyOf(parentGroups, parentGroups.length + 1);
              parentGroups[parentGroups.length - 1] = open.peek();
              inAssertion = Arrays.copyOf(inAssertion, inAssertion.length + 1);
              inAssertion[inAssertion.length - 1] = assrtMark != 0;
              open.push(parentGroups.length);
            }
            break;
          case ASSRT_LPAREN:
            if(!quoted && classLevel == 0) {
              open.push(open.peek());
              if(assrtMark == 0) assrtMark = open.size();
            }
            break;
          case LPAREN:
            if(!quoted && classLevel == 0) open.push(open.peek());
            break;
          case RPAREN:
            if(!quoted && classLevel == 0) {
              if (open.size() == assrtMark) assrtMark = 0;
              open.pop();
            }
            break;
          default:
        }
      }
    }

    /**
     * Return the next token.
     * @return next token
     */
    private Token nxtToken() {
      switch (nxtCp()) {
        case -1: return Token.EOP;
        case ']': return Token.RBRACKET;
        case '[': return Token.LBRACKET;
        case ')': return Token.RPAREN;
        case '\\':
          switch (nxtCp()) {
            case -1: return Token.EOP;
            case 'Q': return Token.LQUOTE;
            case 'E': return Token.RQUOTE;
            default: return Token.OTHER;
          }
        case '(':
          switch(nxtCp()) {
            case '?':
              switch(nxtCp()) {
                case '=':
                case '!':
                  return Token.ASSRT_LPAREN;
                case '<':
                  switch(nxtCp()) {
                    case '=':
                    case '!':
                      return Token.ASSRT_LPAREN;
                    default:
                      reset();
                      return Token.CAPT_LPAREN;
                  }
                default:
                  reset();
                  return Token.LPAREN;
              }
            default:
              reset();
              return Token.CAPT_LPAREN;
          }
        default: return Token.OTHER;
      }
    }

    /**
     * Fetch the next code point.
     * @return the next code point.
     */
    private int nxtCp() {
      final int cp;
      if(pos < len) {
        cp = pattern.codePointAt(pos);
        chrCount = Character.charCount(cp);
        pos += chrCount;
      }
      else {
        cp = -1;
        chrCount = 0;
      }
      return cp;
    }

    /**
     * Reset current position to before the most recent code point.
     */
    private void reset() {
      pos -= chrCount;
    }

    /** Relevant token types. */
    private enum Token {
      /** End of pattern.                         */ EOP,
      /** Capturing group's left parenthesis.     */ CAPT_LPAREN,
      /** Non-capturing group's left parenthesis. */ LPAREN,
      /** Assertion's left parenthesis.           */ ASSRT_LPAREN,
      /** Right parenthesis.                      */ RPAREN,
      /** Left square bracket.                    */ LBRACKET,
      /** Right square bracket.                   */ RBRACKET,
      /** Left quote.                             */ LQUOTE,
      /** Right quote.                            */ RQUOTE,
      /** Anything else.                          */ OTHER,
    }
  }
}
