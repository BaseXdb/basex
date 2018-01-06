package org.basex.query.func.fn;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.parse.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Regular expression functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
abstract class RegEx extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<Pattern> patterns = new TokenObjMap<>();

  /**
   * Returns a regular expression pattern.
   * @param regex pattern
   * @param modifier modifier item
   * @param qc query context
   * @param check check result for empty strings
   * @return pattern modifier
   * @throws QueryException query exception
   */
  protected Pattern pattern(final Expr regex, final Expr modifier, final QueryContext qc,
      final boolean check) throws QueryException {

    final byte[] pat = toToken(regex, qc);
    final byte[] mod = modifier != null ? toToken(modifier, qc) : null;
    final TokenBuilder tb = new TokenBuilder(pat);
    if(mod != null) tb.add(0).add(mod);
    final byte[] key = tb.finish();

    Pattern pattern;
    synchronized(patterns) {
      pattern = patterns.get(key);
      if(pattern == null) {
        pattern = RegExParser.parse(pat, mod, info, check);
        patterns.put(key, pattern);
      }
    }
    return pattern;
  }
}
