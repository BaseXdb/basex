package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends FTIndexOptions {
  /** Option: case. */
  public static final EnumOption<FTCase> CASE = new EnumOption<FTCase>("case", FTCase.class);
  /** Option: case. */
  public static final EnumOption<FTDiacritics> DIACRITICS =
      new EnumOption<FTDiacritics>("diacritics", FTDiacritics.INSENSITIVE);
  /** Option: stemming. */
  public static final BooleanOption STEMMING = new BooleanOption("stemming", false);
  /** Option: language. */
  public static final StringOption LANGUAGE = new StringOption("language", "en");
}
