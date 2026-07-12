package org.basex.query.func.string;

import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * String comparison options: same options as in the Full-Text Module, but with different defaults.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class StringOptions extends Options {
  /** Option: case. */
  public static final EnumOption<FTCase> CASE = new EnumOption<>("case", FTCase.SENSITIVE);
  /** Option: diacritics. */
  public static final EnumOption<FTDiacritics> DIACRITICS =
      new EnumOption<>("diacritics", FTDiacritics.SENSITIVE);
  /** Option: stemming. */
  public static final BooleanOption STEMMING = new BooleanOption("stemming", false);
  /** Option: language. */
  public static final StringOption LANGUAGE = new StringOption("language");
}
