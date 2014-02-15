package org.basex.util.ft;

import org.basex.util.options.*;

/**
 * Full-text window options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTWindowOptions extends Options {
  /** Option: unit. */
  public static final EnumOption<FTUnit> UNIT = new EnumOption<FTUnit>("unit", FTUnit.WORDS);
  /** Option: size. */
  public static final NumberOption SIZE = new NumberOption("size", Integer.MAX_VALUE);
}
