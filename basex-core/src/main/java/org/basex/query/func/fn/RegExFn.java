package org.basex.query.func.fn;

import static java.util.regex.Pattern.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.*;
import org.basex.query.util.regex.parse.*;
import org.basex.util.*;
import org.basex.util.Token;

/**
 * Regular expression functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class RegExFn extends StandardFunc {
  /** Regex characters. */
  static final byte[] REGEX_CHARS = Token.token("\\^$.|?*+()[]{}");

  /**
   * Returns a regular expression pattern.
   * @param pattern pattern
   * @param flags flags (can be {@code null})
   * @param qc query context
   * @return pattern modifier
   * @throws QueryException query exception
   */
  final Pattern pattern(final byte[] pattern, final byte[] flags, final QueryContext qc)
      throws QueryException {
    return regExpr(pattern, flags, qc).pattern;
  }

  /**
   * Returns a compiled regular expression.
   * @param pattern pattern
   * @param flags flags
   * @param qc query context
   * @return compiled regular expression
   * @throws QueryException query exception
   */
  final RegExpr regExpr(final byte[] pattern, final byte[] flags, final QueryContext qc)
      throws QueryException {

    final byte[] key = Token.concat(pattern, '\b', flags);
    synchronized(qc.regex) {
      RegExpr regExpr = qc.regex.get(key);
      if(regExpr == null) {
        regExpr = parse(pattern, flags);
        qc.regex.put(key, regExpr);
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
        final RegExParser parser = new RegExParser(regex, strip, comments,
            (flags & DOTALL) != 0, (flags & MULTILINE) != 0, (flags & CASE_INSENSITIVE) != 0);
        pattern = Pattern.compile(parser.parse().toString(), flags);
      }
      return new RegExpr(pattern);
    } catch(final PatternSyntaxException | ParseException | TokenMgrError ex) {
      Util.debug(ex);
      throw REGINVALID_X.get(info, regex);
    }
  }
}
