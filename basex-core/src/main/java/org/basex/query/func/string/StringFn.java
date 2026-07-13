package org.basex.query.func.string;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;
import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;
import org.basex.util.options.*;
import org.basex.util.similarity.*;

/**
 * Common functionality of the string functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class StringFn extends StandardFunc {
  /** N-gram options. */
  public static class NgramOptions extends StringOptions {
    /** Option: n-gram length. */
    public static final NumberOption N = new NumberOption("n", 2);
    /** Option: pad the input with boundary characters. */
    public static final BooleanOption PADDING = new BooleanOption("padding", false);
  }

  /**
   * Returns full-text options for normalizing the input.
   * @param expr expression that yields the string options
   * @param qc query context
   * @return options, or {@code null} if the input is to be compared literally
   * @throws QueryException query exception
   */
  final FTOpt ftOpt(final Expr expr, final QueryContext qc) throws QueryException {
    return ftOpt(toOptions(expr, new StringOptions(), qc));
  }

  /**
   * Returns full-text options for normalizing the input.
   * @param options string options
   * @return options, or {@code null} if the input is to be compared literally
   */
  static FTOpt ftOpt(final StringOptions options) {
    final FTCase cs = options.get(StringOptions.CASE);
    final FTDiacritics dc = options.get(StringOptions.DIACRITICS);
    final boolean st = options.get(StringOptions.STEMMING);
    final String ln = options.get(StringOptions.LANGUAGE);
    if(cs == FTCase.SENSITIVE && dc == FTDiacritics.SENSITIVE && !st && ln == null) return null;

    final FTOpt opt = new FTOpt();
    opt.set(DC, dc == FTDiacritics.SENSITIVE);
    opt.set(ST, st);
    opt.cs = cs;
    if(ln != null) opt.ln = Language.get(ln);
    return opt;
  }

  /**
   * Returns the codepoints of a string, normalized with the specified options.
   * @param value value
   * @param opt full-text options ({@code null}: no normalization)
   * @return codepoints
   */
  static int[] cps(final byte[] value, final FTOpt opt) {
    if(opt == null) return Token.cps(value);

    final FTLexer lexer = new FTLexer(opt).all().init(value);
    final TokenBuilder tb = new TokenBuilder();
    while(lexer.hasNext()) tb.add(lexer.nextToken());
    return Token.cps(tb.finish());
  }

  /**
   * Returns a string item for a codepoints array.
   * @param cps codepoints array
   * @return string
   */
  static Str str(final int[] cps) {
    return Str.get(new String(cps, 0, cps.length));
  }

  /**
   * Returns the tokens of a string. Without full-text options, the string is split on whitespace.
   * @param value value
   * @param opt full-text options ({@code null}: split on whitespace)
   * @return tokens
   */
  static String[] tokens(final byte[] value, final FTOpt opt) {
    if(opt == null) return TokenRatio.tokens(Token.cps(value));

    final FTLexer lexer = new FTLexer(opt).init(value);
    final StringList tokens = new StringList();
    while(lexer.hasNext()) tokens.add(Token.string(lexer.nextToken()));
    return tokens.finish();
  }

  /**
   * Returns the n-gram length.
   * @param options n-gram options
   * @return length
   * @throws QueryException query exception
   */
  final int n(final NgramOptions options) throws QueryException {
    final int n = options.get(NgramOptions.N);
    if(n < 1) throw STRING_NGRAM_X.get(info, n);
    return n;
  }

  /**
   * Checks if a string is too long to be compared with the Levenshtein distance.
   * @param length string length
   * @throws QueryException query exception
   */
  final void checkLength(final int length) throws QueryException {
    if(length > MAX_LENGTH) throw STRING_BOUNDS_X.get(info, MAX_LENGTH);
  }
}
