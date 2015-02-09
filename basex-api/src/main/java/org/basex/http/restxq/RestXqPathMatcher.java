package org.basex.http.restxq;

import static java.math.BigInteger.*;
import static org.basex.http.restxq.RestXqText.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * RESTXQ path template.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
final class RestXqPathMatcher {
  /** Default matcher for empty path templates. */
  private static final RestXqPathMatcher EMPTY =
      new RestXqPathMatcher("/", Collections.<QNm>emptyList(), 0, ZERO);
  /** Variable names defined in the path template. */
  final List<QNm> vars;
  /** Compiled regular expression which matches paths defined by the path annotation. */
  final Pattern pattern;
  /** Number of path segments. */
  final int segments;
  /** Bit array with variable positions within the path template. */
  final BigInteger varsPos;

  /**
   * Constructor.
   * @param regex regular expression which matches paths defined by the path annotation
   * @param vars variable names defined in the path template
   * @param segments segment count
   * @param varsPos variable position
   */
  private RestXqPathMatcher(final String regex, final List<QNm> vars, final int segments,
    final BigInteger varsPos) {
    this.vars = vars;
    this.segments = segments;
    this.varsPos = varsPos;
    pattern = Pattern.compile(regex);
  }

  /**
   * Checks if the given path matches.
   * @param path path to match
   * @return result of check
   */
  boolean matches(final String path) {
    return matcher(path).matches();
  }

  /**
   * Gets variable values for the given path.
   * @param path from which to read the values
   * @return map with variable values
   */
  Map<QNm, String> values(final String path) {
    final Map<QNm, String> result = new HashMap<>();
    final Matcher m = matcher(path);
    if(m.matches()) {
      final int ml = m.groupCount();
      for(int i = 0; i < ml; i++) result.put(vars.get(i), m.group(i + 1));
    }
    return result;
  }

  /**
   * Creates a pattern matcher for the given string.
   * @param input input string
   * @return pattern matcher
   */
  private Matcher matcher(final String input) {
    return pattern.matcher(input);
  }

  /**
   * Parses a path template.
   * @param path path template string to be parsed
   * @param info input info
   * @return parsed path template
   * @throws QueryException if given template is invalid
   */
  static RestXqPathMatcher parse(final String path, final InputInfo info) throws QueryException {
    if(path.isEmpty()) return EMPTY;

    final ArrayList<QNm> vars = new ArrayList<>();
    final StringBuilder result = new StringBuilder();
    final StringBuilder literals = new StringBuilder();
    final TokenBuilder variable = new TokenBuilder();
    final StringBuilder regex = new StringBuilder();
    final BitSet varsPos = new BitSet();
    int segment = 0;

    final CharIterator i = new CharIterator(path);
    if(path.charAt(0) == '/') i.next();
    literals.append('/');

    while(i.hasNext()) {
      char ch = i.next();
      if(ch == '{') {
        decodeAndEscape(literals, result);

        // variable
        if(!i.hasNext() || i.nextNonWS() != '$') throw error(info, INV_TEMPLATE, path);

        // default variable regular expression
        regex.append("[^/]+?");

        int braces = 1;
        while(i.hasNext()) {
          ch = i.nextNonWS();

          if(ch == '=') {
            regex.setLength(0);
            addRegex(i, regex);
            if(regex.length() == 0) throw error(info, INV_TEMPLATE, path);
            break;
          } else if(ch == '{') {
            ++braces;
            variable.add(ch);
          } else if(ch == '}' && --braces == 0) {
            break;
          } else {
            variable.add(ch);
          }
        }

        final byte[] var = variable.toArray();
        if(!XMLToken.isQName(var)) throw error(info, INV_VARNAME, variable);
        vars.add(new QNm(var));
        variable.reset();
        varsPos.set(segment);

        result.append('(').append(regex).append(')');
        regex.setLength(0);
      } else {
        if(ch == '/') ++segment;
        literals.append(ch);
      }
    }
    decodeAndEscape(literals, result);

    final BigInteger vp = varsPos.cardinality() == 0 ? ZERO : new BigInteger(varsPos.toByteArray());
    return new RestXqPathMatcher(result.toString(), vars, segment + 1, vp);
  }

  /**
   * Parses a regular expression defined for a template variable.
   * @param i character iterator positioned before the first character of the regex.
   * @param result string builder where the parsed regular expression will be appended to.
   */
  private static void addRegex(final CharIterator i, final StringBuilder result) {
    int braces = 1;
    while(i.hasNext()) {
      final char ch = i.nextNonWS();
      if(ch == '{') ++braces;
      else if(ch == '}' && --braces == 0) break;
      result.append(ch);
    }
  }

  /**
   * Decodes the URL and escapes regex characters in path template literals.
   * @param literals literals to escape
   * @param result string builder where the escaped literals will be appended to.
   */
  private static void decodeAndEscape(final StringBuilder literals, final StringBuilder result) {
    if(literals.length() > 0) {
      final String decoded = HTTPContext.decode(literals.toString());
      final int n = decoded.length();
      for(int i = 0; i < n; ++i) {
        final char c = decoded.charAt(i);
        if(isRegexChar(c)) result.append('\\');
        result.append(c);
      }
      literals.setLength(0);
    }
  }

  /**
   * Checks if a character is a regex character.
   * @param c character to check.
   * @return result of check
   */
  private static boolean isRegexChar(final char c) {
    return ".^&!?-:<>()[]{}$=,*+|".indexOf(c) >= 0;
  }

  /**
   * Creates a query exception.
   * @param info input info
   * @param msg exception message
   * @param e text extensions
   * @return query exception
   */
  private static QueryException error(final InputInfo info, final String msg, final Object... e) {
    return QueryError.BASX_RESTXQ_X.get(info, Util.info(msg, e));
  }

  /** Character iterator. */
  private static final class CharIterator {
    /** Input text to iterate over. */
    private final String input;
    /** Input text length. */
    private final int len;
    /** Current iterator position. */
    private int pos;

    /**
     * Construct a new character iterator for the given input text.
     * @param input input text to iterator over.
     */
    CharIterator(final String input) {
      this.input = input;
      len = input.length();
    }

    /**
     * Check if there are more characters to iterate over.
     * @return {@code false} if text end is reached
     */
    boolean hasNext() {
      return pos < len;
    }

    /**
     * Get next character.
     * @return next character
     */
    char next() {
      return input.charAt(pos++);
    }

    /**
     * Get next non-white-space character.
     * @return non-white-space character
     */
    char nextNonWS() {
      char ch;
      do {
        ch = next();
      } while(Character.isWhitespace(ch) && hasNext());
      return ch;
    }
  }
}
