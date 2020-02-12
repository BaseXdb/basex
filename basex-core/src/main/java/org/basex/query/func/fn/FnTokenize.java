package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnTokenize extends RegEx {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toEmptyToken(exprs[0], qc);
    if(exprs.length < 2) return StrSeq.get(split(normalize(value), ' '));

    final Pattern pattern = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc, true);

    final TokenList tl = new TokenList();
    final String string = string(value);
    if(!string.isEmpty()) {
      final Matcher matcher = pattern.matcher(string);
      int start = 0;
      while(matcher.find()) {
        tl.add(string.substring(start, matcher.start()));
        start = matcher.end();
      }
      tl.add(string.substring(start));
    }
    return StrSeq.get(tl);
  }

  /**
   * Returns the input argument if no others are specified, and if it returns at most one item.
   * @return first argument
   */
  public Expr input() {
    // X must yield single result (otherwise, it may result in an error)
    return exprs.length == 1 && exprs[0].seqType().zeroOrOne() ? exprs[0] : null;
  }
}
