package org.basex.http.restxq;

import static java.math.BigInteger.*;
import static org.basex.http.web.WebText.*;

import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * RESTXQ path template.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
final class RestXqPathMatcher {
  /** Default matcher for empty path templates. */
  private static final RestXqPathMatcher EMPTY =
      new RestXqPathMatcher("/", Collections.emptyList(), 0, ZERO);
  /** Variable names defined in the path template. */
  final List<QNm> varNames;
  /** Compiled regular expression which matches paths defined by the path annotation. */
  final Pattern pattern;
  /** Number of path segments. */
  final int segments;
  /** Bit array with variable positions within the path template. */
  final BigInteger varsPos;

  /**
   * Constructor.
   * @param regex regular expression which matches paths defined by the path annotation
   * @param varNames variable names defined in the path template
   * @param segments segment count
   * @param varsPos variable position
   */
  private RestXqPathMatcher(final String regex, final List<QNm> varNames, final int segments,
      final BigInteger varsPos) {
    this.varNames = varNames;
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
  QNmMap<String> values(final String path) {
    final QNmMap<String> result = new QNmMap<>();
    final Matcher m = matcher(path);
    if(m.matches()) {
      final int groupCount = m.groupCount();
      if(varNames.size() <= groupCount) {
        int group = 1;
        for(final QNm var : varNames) {
          result.put(var, m.group(group));
          // skip nested groups
          final int end = m.end(group);
          while(++group <= groupCount && m.start(group) < end);
        }
      }
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
   * @param ii input info
   * @return parsed path template
   * @throws QueryException query exception
   */
  static RestXqPathMatcher parse(final String path, final InputInfo ii) throws QueryException {
    if(path.isEmpty()) return EMPTY;

    final ArrayList<QNm> varNames = new ArrayList<>();
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
        decodeAndEscape(literals, result, ii);

        // variable
        if(!i.hasNext() || i.nextNonWS() != '$')
          throw RestXqFunction.error(ii, INV_TEMPLATE_X, path);

        // default variable regular expression
        regex.append("[^/]+?");

        int braces = 1;
        while(i.hasNext()) {
          ch = i.nextNonWS();

          if(ch == '=') {
            regex.setLength(0);
            addRegex(i, regex);
            if(regex.length() == 0) throw RestXqFunction.error(ii, INV_TEMPLATE_X, path);
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
        if(!XMLToken.isQName(var)) throw RestXqFunction.error(ii, INV_VARNAME_X, variable);
        varNames.add(new QNm(var));
        variable.reset();
        varsPos.set(segment);

        result.append('(').append(regex).append(')');
        regex.setLength(0);
      } else {
        if(ch == '/') ++segment;
        literals.append(ch);
      }
    }
    decodeAndEscape(literals, result, ii);

    final BigInteger vp = varsPos.cardinality() == 0 ? ZERO : new BigInteger(varsPos.toByteArray());
    return new RestXqPathMatcher(result.toString(), varNames, segment + 1, vp);
  }

  /**
   * Parses a regular expression defined for a template variable.
   * @param i character iterator positioned before the first character of the regex
   * @param result string builder where the parsed regular expression will be appended to
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
   * @param ii input info
   * @param result string builder where the escaped literals will be appended to
   * @throws QueryException query exception
   */
  private static void decodeAndEscape(final StringBuilder literals, final StringBuilder result,
      final InputInfo ii) throws QueryException {

    if(literals.length() > 0) {
      final byte[] path = Token.decodeUri(Token.token(literals.toString()));
      if(path == null) throw RestXqFunction.error(ii, INV_ENCODING_X, literals);
      final TokenBuilder tb = new TokenBuilder(path.length);
      for(final byte b : path) {
        if(".^&!?-:<>()[]{}$=,*+|".indexOf(b) >= 0) tb.addByte((byte) '\\');
        tb.addByte(b);
      }
      result.append(tb);
      literals.setLength(0);
    }
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
     * @param input input text to iterator over
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
