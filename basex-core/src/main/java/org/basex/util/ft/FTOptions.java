package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FTOptions extends Options {
  /** Option: wildcards. */
  public static final EnumOption<FTMode> MODE = new EnumOption<FTMode>("mode", FTMode.ANY);
  /** Option: fuzzy. */
  public static final BooleanOption FUZZY = new BooleanOption("fuzzy", false);
  /** Option: wildcards. */
  public static final BooleanOption WILDCARDS = new BooleanOption("wildcards", false);
  /** Option: distance. */
  public static final OptionsOption<FTDistanceOptions> DISTANCE =
      new OptionsOption<FTDistanceOptions>("distance", FTDistanceOptions.class);
}
