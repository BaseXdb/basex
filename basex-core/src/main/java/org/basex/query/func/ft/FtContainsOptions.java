package org.basex.query.func.ft;

import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * Full-text options: index options, extended by the tokenization options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtContainsOptions extends FtIndexOptions {
  /** Option: case. */
  public static final EnumOption<FTCase> CASE = FtLexerOptions.CASE;
  /** Option: diacritics. */
  public static final EnumOption<FTDiacritics> DIACRITICS = FtLexerOptions.DIACRITICS;
  /** Option: stemming. */
  public static final BooleanOption STEMMING = FtLexerOptions.STEMMING;
  /** Option: language. */
  public static final StringOption LANGUAGE = FtLexerOptions.LANGUAGE;
}
