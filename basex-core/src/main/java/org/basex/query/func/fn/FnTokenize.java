package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnTokenize extends RegEx {
  /** Placeholder for default search. */
  private static final byte[] DEFAULT = Token.token("\\s+");
  /** Single space. */
  private static final byte[] SPACE = Token.token(" ");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc);
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

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0], ptrn = exprs.length == 2 ? exprs[1] : null;

    // tokenize(normalize-space(A), ' ')  ->  tokenize(A)
    if(NORMALIZE_SPACE.is(expr) && ptrn instanceof Str && eq(((Str) ptrn).string(), SPACE)) {
      final Expr arg = expr.args().length == 1 ? expr.arg(0) : ContextValue.get(cc, info);
      return cc.function(TOKENIZE, info, arg);
    }

    return this;
  }

  /**
   * Indicates if a default whitespace tokenization is to be performed.
   * @return result of check
   */
  public boolean whitespaces() {
    final int el = exprs.length;
    return el == 1 || el == 2 && exprs[1] instanceof Str &&
        Token.eq(((Str) exprs[1]).string(), DEFAULT);
  }
}
