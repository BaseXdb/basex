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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class RegEx extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<Pattern> patterns = new TokenObjMap<>();

  /**
   * Returns a regular expression pattern.
   * @param pattern input pattern
   * @param modifier modifier item
   * @param qc query context
   * @param check check result for empty strings
   * @return pattern modifier
   * @throws QueryException query exception
   */
  Pattern pattern(final Expr pattern, final Expr modifier, final QueryContext qc,
                  final boolean check) throws QueryException {

    final byte[] pat = toToken(pattern, qc);
    final byte[] mod = modifier != null ? toToken(modifier, qc) : null;
    final TokenBuilder tb = new TokenBuilder(pat);
    if(mod != null) tb.add(0).add(mod);
    final byte[] key = tb.finish();
    Pattern p = patterns.get(key);
    if(p == null) {
      p = RegExParser.parse(pat, mod, info, check);
      patterns.put(key, p);
    }
    return p;
  }
}
