package org.basex.query.func.fn;

import static java.util.regex.Pattern.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class RegEx extends StandardFunc {
  /** Regex characters. */
  static final byte[] REGEX_CHARS = Token.token("\\^$.|?*+()[]{}");

  /** Pattern cache. */
  private final TokenObjMap<RegExpr> patterns = new TokenObjMap<>();

  /**
   * Regular expression pattern.
   */
  static class RegExpr {
    /** Pattern. */
    Pattern pattern;
    /** Number of groups. */
    int groups;
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern pattern
   * @param flags flags (can be {@code null})
   * @param check check result for empty strings
   * @return pattern modifier
   * @throws QueryException query exception
   */
  final Pattern pattern(final byte[] pattern, final byte[] flags, final boolean check)
      throws QueryException {
    return regExpr(pattern, flags, check).pattern;
  }

  /**
   * Returns a regular expression pattern.
   * @param pattern pattern
   * @param flags flags
   * @param check check result for empty strings
   * @return pattern modifier
   * @throws QueryException query exception
   */
  final RegExpr regExpr(final byte[] pattern, final byte[] flags, final boolean check)
      throws QueryException {

    final byte[] key = Token.concat(pattern, '\b', flags);
    synchronized(patterns) {
      RegExpr regExpr = patterns.get(key);
      if(regExpr == null) {
        regExpr = parse(pattern, flags, check);
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
   * @param check check result for empty strings
   * @return the pattern
   * @throws QueryException query exception
   */
  private RegExpr parse(final byte[] regex, final byte[] modifiers, final boolean check)
      throws QueryException {

    // process modifiers
    int flags = 0;
    boolean strip = false, java = false;
    for(final byte mod : modifiers) {
      if(mod == 'i') flags |= CASE_INSENSITIVE | UNICODE_CASE;
      else if(mod == 'm') flags |= MULTILINE;
      else if(mod == 's') flags |= DOTALL;
      else if(mod == 'q') flags |= LITERAL;
      else if(mod == 'x') strip = true;
      else if(mod == 'j' || mod == '!') java = true;
      else if(mod != ';') throw REGFLAG_X.get(info, (char) mod);
    }

    try {
      // Java syntax, literal query: no need to change anything
      final Pattern pattern;
      int groups = 0;
      if(java || (flags & LITERAL) != 0) {
        pattern = Pattern.compile(string(regex), flags);
      } else {
        final RegExParser parser = new RegExParser(regex, strip, (flags & DOTALL) != 0,
            (flags & MULTILINE) != 0);
        final String string = parser.parse().toString();

        pattern = Pattern.compile(string, flags);
        if(check) {
          // Circumvent Java RegEx behavior ("If MULTILINE mode is activated"...):
          // http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#lt
          final Pattern p = (pattern.flags() & Pattern.MULTILINE) == 0 ? pattern :
            Pattern.compile(pattern.pattern());
          if(p.matcher("").matches()) throw REGEMPTY_X.get(info, string);
        }
        groups = parser.groups();
      }

      final RegExpr regExpr = new RegExpr();
      regExpr.pattern = pattern;
      regExpr.groups = groups;
      return regExpr;

    } catch(final PatternSyntaxException | ParseException | TokenMgrError ex) {
      Util.debug(ex);
      throw REGINVALID_X.get(info, regex);
    }
  }
}
