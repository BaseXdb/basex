package org.basex.query.func.string;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;
import static org.basex.util.similarity.Levenshtein.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Functions for comparing strings.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class StringFn extends StandardFunc {
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
   * @throws QueryException query exception
   */
  final int[] cps(final AStr value, final FTOpt opt) throws QueryException {
    if(opt == null) return value.codepoints(info);

    final FTLexer lexer = new FTLexer(opt).all().init(value.string(info));
    final TokenBuilder tb = new TokenBuilder();
    while(lexer.hasNext()) tb.add(lexer.nextToken());
    return Token.cps(tb.finish());
  }

  /**
   * Returns the tokens of a string. Without full-text options, the string is split on whitespace.
   * @param value value
   * @param opt full-text options ({@code null}: split on whitespace)
   * @return tokens
   * @throws QueryException query exception
   */
  final String[] tokens(final AStr value, final FTOpt opt) throws QueryException {
    if(opt == null) return TokenRatio.tokens(value.codepoints(info));

    final FTLexer lexer = new FTLexer(opt).init(value.string(info));
    final StringList tokens = new StringList();
    while(lexer.hasNext()) tokens.add(Token.string(lexer.nextToken()));
    return tokens.finish();
  }

  /**
   * Checks if a string is too long to be compared with the Levenshtein distance.
   * @param cps codepoints
   * @throws QueryException query exception
   */
  final void checkLength(final int[] cps) throws QueryException {
    if(cps.length > MAX_LENGTH) throw STRING_BOUNDS_X.get(info, MAX_LENGTH);
  }
}
