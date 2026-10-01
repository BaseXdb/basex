package org.basex.query.func.fn;

import static java.util.regex.Pattern.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.function.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.*;
import org.basex.query.util.regex.parse.*;
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
   * Returns the string of a pattern that can be matched literally, i.e. without evaluating a
   * regular expression, or {@code null} otherwise.
   * @param pattern pattern
   * @param flags flags
   * @return literal string, or {@code null}
   */
  static byte[] literal(final byte[] pattern, final byte[] flags) {
    // empty pattern matches the empty string: leave to the regular expression engine
    if(pattern.length == 0) return null;
    // "q" flag: the whole pattern is literal
    if(flags.length == 1 && flags[0] == 'q') return pattern;
    // no flags: literal if the pattern contains no regular expression meta character
    if(flags.length == 0) {
      for(final byte b : pattern) {
        if(contains(REGEX_CHARS, b)) return null;
      }
      return pattern;
    }
    return null;
  }

  /**
   * Returns a function that maps string indexes to character positions.
   * @param string string
   * @return function
   */
  static IntUnaryOperator positions(final String string) {
    final int sl = string.length();
    if(string.codePointCount(0, sl) == sl) return i -> i + 1;
    final int[] positions = new int[sl + 1];
    int p = 1;
    for(int i = 0; i < sl; i++) {
      positions[i] = p;
      if(!Character.isHighSurrogate(string.charAt(i))) p++;
    }
    positions[sl] = p;
    return i -> positions[i];
  }

  /**
   * Compiles this regular expression to a {@link Pattern}.
   * @param regex regular expression to parse
   * @param modifiers modifiers
   * @return pattern
   * @throws QueryException query exception
   */
  private RegExpr parse(final byte[] regex, final byte[] modifiers)
      throws QueryException {

    // process modifiers (case variants for the 'i' flag are added to the pattern)
    int flags = 0;
    boolean insensitive = false, strip = false, comments = false;
    for(final byte mod : modifiers) {
      if(mod == 'i') insensitive = true;
      else if(mod == 'm') flags |= MULTILINE | UNIX_LINES;
      else if(mod == 's') flags |= DOTALL;
      else if(mod == 'q') flags |= LITERAL;
      else if(mod == 'x') strip = true;
      else if(mod == 'c') comments = true;
      else throw REGFLAG_X.get(info, (char) mod);
    }

    try {
      if((flags & LITERAL) == 0) {
        final RegExParser parser = new RegExParser(regex, strip, comments,
            (flags & DOTALL) != 0, (flags & MULTILINE) != 0, insensitive);
        String string = parser.parse().toString();
        // supplementary character after lookbehinds: makes Java count code points in them
        if(string.contains("(?<=") || string.contains("(?<!")) {
          string += "|(?!)" + Character.toString(0x10000);
        }
        return new RegExpr(Pattern.compile(string, flags), parser.groups());
      }
      if(insensitive) {
        final StringBuilder sb = new StringBuilder();
        string(regex).codePoints().forEach(cp -> sb.append(new Literal(cp, true)));
        return new RegExpr(Pattern.compile(sb.toString()), null);
      }
      return new RegExpr(Pattern.compile(string(regex), flags), null);
    } catch(final PatternSyntaxException | ParseException | TokenMgrError ex) {
      throw REGINVALID_X.get(info, regex).cause(ex);
    }
  }
}
