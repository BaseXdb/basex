package org.basex.query.func.ft;

import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * Full-text index options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FtIndexOptions extends FtFuzzyOptions {
  /** Option: mode. */
  public static final EnumOption<FTMode> MODE = new EnumOption<>("mode", FTMode.ANY);
  /** Option: wildcards. */
  public static final BooleanOption WILDCARDS = new BooleanOption("wildcards");
  /** Option: ordered. */
  public static final BooleanOption ORDERED = new BooleanOption("ordered", false);
  /** Option: distance. */
  public static final OptionsOption<FTDistanceOptions> DISTANCE =
      new OptionsOption<>("distance", FTDistanceOptions.class);
  /** Option: window. */
  public static final OptionsOption<FTWindowOptions> WINDOW =
      new OptionsOption<>("window", FTWindowOptions.class);
  /** Option: scope. */
  public static final OptionsOption<FTScopeOptions> SCOPE =
      new OptionsOption<>("scope", FTScopeOptions.class);
  /** Option: content. */
  public static final EnumOption<FTContents> CONTENT =
      new EnumOption<>("content", FTContents.class);
}
