package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static java.math.BigInteger.ZERO;
import static java.util.regex.Pattern.compile;
import static java.util.Collections.*;

import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.query.value.item.QNm;
import org.basex.util.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST-XQ path template.
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
final class PathMatcher
{
  /** Default matcher for empty path templates. */
  private static final PathMatcher EMPTY = new PathMatcher("/", Collections.<QNm>emptyList(),
    1, 0, 0, ZERO);
  /** Variable names defined in the path template. */
  final List<QNm> variableNames;
  /** Compiled regular expression which matches paths defined by the path annotation. */
  final Pattern pattern;
  /** Number of literals in the path template (without variableNames). */
  final int literalCount;
  /** Number of regular expressions. */
  final int regexCount;
  /** Number of path segments. */
  final int segmentCount;
  /** Bit array with variable positions within the path template. */
  final BigInteger variablePositions;

  /**
   * Construct an new path template.
   * @param regex regular expression which matches paths defined by the path annotation
   * @param vars variable names defined in the path template
   */
  private PathMatcher(final String regex, final List<QNm> vars, final int literalCount,
    final int regexCount, final int segmentCount, final BigInteger varPos)
  {
    this.variableNames = vars;
    this.pattern = compile(regex);
    this.literalCount = literalCount;
    this.regexCount = regexCount;
    this.segmentCount = segmentCount;
    this.variablePositions = varPos;
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
   * Get variable values for the given path.
   * @param path from which to read the values
   * @return map with variable values
   */
  Map<QNm, String> getVariableValues(final String path) {
    final Map<QNm, String> result = new HashMap<>();
    final Matcher m = matcher(path);
    if (m.matches()) {
      for (int i = 0; i < m.groupCount();) result.put(variableNames.get(i), m.group(++i));
    }
    // TODO else?
    return result;
  }

  /**
   * Create pattern matcher for the given string.
   * @param input input string
   * @return pattern matcher
   */
  private Matcher matcher(final String input) {
    String p = decode(input);
    if(p.isEmpty() || p.charAt(0) != '/') p += '/';
    return pattern.matcher(p);
  }

  /**
   * Parse a path template.
   * @param template path template string to be parsed
   * @param info input info
   * @return parsed path template
   * @throws QueryException if given template is invalid
   */
  static PathMatcher parse(final String template, final InputInfo info) throws QueryException {
    if(template.isEmpty()) return EMPTY;

    final ArrayList<QNm> vars = new ArrayList<>();
    final StringBuilder regex = new StringBuilder();
    final StringBuilder currentLiterals = new StringBuilder();
    final TokenBuilder currentVariable = new TokenBuilder();
    final StringBuilder currentRegex = new StringBuilder();
    final BitSet variablePositions = new BitSet();
    int literalCount = 0;
    int regexCount = 0;
    int currentSegment = 0;

    final CharIterator i = new CharIterator(template);
    if(template.charAt(0) == '/') i.next();
    currentLiterals.append('/');

    while(i.hasNext()) {
      char ch = i.next();
      if (ch == '{') {
        // variable

        literalCount += decodeAndEscapeLiterals(currentLiterals, regex);

        if (!i.hasNext() || i.nextNonWS() != '$') throw error(info, INV_TEMPLATE, template);

        // default variable regular expression
        currentRegex.append("[^/]+?");

        int braceCount = 1;
        while (i.hasNext()) {
          ch = i.nextNonWS();

          if (ch == '=') {
            currentRegex.setLength(0);
            addRegex(i, currentRegex);
            if (currentRegex.length() <= 0) throw error(info, INV_TEMPLATE, template);
            ++regexCount;
            break;
          } else if (ch == '{') {
            ++braceCount;
            currentVariable.add(ch);
          } else if (ch == '}' && --braceCount == 0) {
            break;
          } else {
            currentVariable.add(ch);
          }
        }

        final byte[] var = currentVariable.toArray();
        if (!XMLToken.isQName(var)) throw error(info, INV_VARNAME, currentVariable);
        vars.add(new QNm(var));
        currentVariable.reset();
        variablePositions.set(currentSegment);

        regex.append('(').append(currentRegex).append(')');
        currentRegex.setLength(0);
      }
      else {
        if (ch == '/') ++currentSegment;
        currentLiterals.append(ch);
      }
    }

    literalCount += decodeAndEscapeLiterals(currentLiterals, regex);

    final BigInteger variablePositionsBitMask = variablePositions.cardinality() == 0 ?
            ZERO : new BigInteger(variablePositions.toByteArray());

    return new PathMatcher(regex.toString(), unmodifiableList(vars), literalCount, regexCount,
      currentSegment + 1, variablePositionsBitMask);
  }

  /**
   * Parse a regular expression defined for a template variable.
   * @param i character iterator positioned before the first character of the regex.
   * @param result string builder where the parsed regular expression will be appended to.
   */
  private static void addRegex(final CharIterator i, final StringBuilder result) {
    int braceCount = 1;
    while (i.hasNext()) {
      char ch = i.nextNonWS();
      if (ch == '{') ++braceCount;
      else if (ch == '}' && --braceCount == 0) break;
      result.append(ch);
    }
  }

  /**
   * URL decode and escape regex characters in path template literals.
   * @param literals literals to escape
   * @param result string builder where the escaped literals will be appended to.
   * @return number of added literals (without regex escape backslashes)
   */
  private static int decodeAndEscapeLiterals(final StringBuilder literals,
    final StringBuilder result)
  {
    int n = 0;
    if (literals.length() > 0) {
      final String decodedLiterals = decode(literals.toString());
      n = decodedLiterals.length();
      for (int i = 0; i < decodedLiterals.length(); ++i) {
        final char c = decodedLiterals.charAt(i);
        if (isRegexChar(c)) result.append('\\');
        result.append(c);
      }
      literals.setLength(0);
    }
    return n;
  }

  /**
   * Check if a character is a regex character.
   * @param c character to check.
   * @return {@code true} if a regex character
   */
  private static boolean isRegexChar(final char c) {
    return ".^&!?-:<>()[]{}$=,*+|".indexOf(c) >= 0;
  }

  /**
   * Create a query exception.
   * @param info input info
   * @param msg exception message
   * @param e text extensions
   * @return query exception
   */
  private static QueryException error(final InputInfo info, final String msg, final Object... e) {
    return Err.BASX_RESTXQ.get(info, Util.info(msg, e));
  }


  /**
   * URL decode the given string.
   * @param p string to decode.
   * @return decoded value
   */
  private static String decode(final String p) {
    try {
      return URLDecoder.decode(p, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Character iterator. */
  private static final class CharIterator {
    /** Input text to iterate over. */
    private final String input;
    /** Input text length. */
    private final int len;
    /** Current iterator position. */
    private int pos = 0;

    /**
     * Construct a new character iterator for the given input text.
     * @param input input text to iterator over.
     */
    CharIterator(final String input) {
      this.input = input;
      this.len = input.length();
    }

    /**
     * Check if there are more characters to iterate over.
     * @return {@code false} if text end is reached
     */
    boolean hasNext() {
      return len > pos;
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
      } while (Character.isWhitespace(ch) && hasNext());
      return ch;
    }
  }
}
