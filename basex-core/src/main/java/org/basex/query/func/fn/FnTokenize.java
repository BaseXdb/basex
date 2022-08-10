package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnTokenize extends RegEx {
  /** Placeholder for default search. */
  private static final byte[] DEFAULT = Token.token("\\s+");
  /** Single space. */
  private static final byte[] SPACE = Token.token(" ");

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final byte[][] input = input(qc);

    final int vl = input[0].length;
    if(exprs.length < 3) {
      final int ch = patternChar(input[1]);
      if(ch != -1) {
        final int sl = input[1].length;
        return vl == 0 ? Empty.ITER : new Iter() {
          int start;

          @Override
          public Item next() {
            if(start == -1) return null;
            final int e = indexOf(input[0], ch, start);
            return e != -1 ? next(e, e + sl) : next(vl, -1);
          }

          private Str next(final int end, final int next) {
            final int b = start;
            start = next;
            return Str.get(substring(input[0], b, end));
          }
        };
      }
    }

    final Pattern p = pattern(input[1], exprs.length == 3 ? exprs[2] : null, qc, true);
    return vl == 0 ? Empty.ITER : new Iter() {
      final String string = string(input[0]);
      final Matcher matcher = p.matcher(string);
      int start;

      @Override
      public Item next() {
        return start == -1 ? null : matcher.find() ?
          next(matcher.start(), matcher.end()) : next(string.length(), -1);
      }

      private Str next(final int end, final int next) {
        final int b = start;
        start = next;
        return Str.get(token(string.substring(b, end)));
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[][] input = input(qc);

    final int vl = input[0].length;
    if(exprs.length < 3) {
      final int ch = patternChar(input[1]);
      if(ch != -1) return vl == 0 ? Empty.VALUE : StrSeq.get(split(input[0], ch, true));
    }

    final Pattern p = pattern(input[1], exprs.length == 3 ? exprs[2] : null, qc, true);
    if(vl == 0) return Empty.VALUE;

    final TokenList tl = new TokenList();
    final String string = string(input[0]);
    int start = 0;
    for(final Matcher matcher = p.matcher(string); matcher.find();) {
      tl.add(string.substring(start, matcher.start()));
      start = matcher.end();
    }
    return StrSeq.get(tl.add(string.substring(start)));
  }

  /**
   * Returns the input and pattern strings.
   * @param qc query context
   * @return strings
   * @throws QueryException query exception
   */
  private byte[][] input(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc);
    final boolean pattern = exprs.length > 1;
    return new byte[][] {
      pattern ? value : normalize(value),
      pattern ? toToken(exprs[1], qc) : Token.SPACE
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = exprs[0], pattern = exprs.length == 2 ? exprs[1] : null;

    // tokenize(normalize-space(A), ' ')  ->  tokenize(A)
    if(NORMALIZE_SPACE.is(value) && pattern instanceof Str && eq(((Str) pattern).string(), SPACE)) {
      final Expr arg = value.args().length == 1 ? value.arg(0) : ContextValue.get(cc, info);
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
    return el == 1 || el == 2 && exprs[1] instanceof Str && eq(((Str) exprs[1]).string(), DEFAULT);
  }
}
