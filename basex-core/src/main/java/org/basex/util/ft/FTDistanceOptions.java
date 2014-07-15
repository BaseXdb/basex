package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text distance options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTDistanceOptions extends Options {
  /** Option: unit. */
  public static final EnumOption<FTUnit> UNIT = new EnumOption<>("unit", FTUnit.WORDS);
  /** Option: min. */
  public static final NumberOption MIN = new NumberOption("min", 0);
  /** Option: max. */
  public static final NumberOption MAX = new NumberOption("max", Integer.MAX_VALUE);
}
