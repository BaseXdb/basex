package org.basex.util.ft;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Full-text distance options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FTDistanceOptions extends Options {
  /** Option: unit. */
  public static final EnumOption<FTUnit> UNIT = new EnumOption<FTUnit>("unit", FTUnit.WORDS);
  /** Option: min. */
  public static final NumberOption MIN = new NumberOption("min", 1);
  /** Option: max. */
  public static final NumberOption MAX = new NumberOption("max", Integer.MAX_VALUE);

  /**
   * Constructor.
   */
  public FTDistanceOptions() {
    super();
  }

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  public FTDistanceOptions(final String opts) throws BaseXException {
    super(opts);
  }
}
