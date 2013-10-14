package org.basex.util.ft;

import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.options.*;

/**
 * Full-text distance options.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FTDistanceOptions extends Options {
  /** Option: min. */
  public static final NumberOption MIN = new NumberOption("min", 1);
  /** Option: max. */
  public static final NumberOption MAX = new NumberOption("max", Integer.MAX_VALUE);
  /** Option: unit. */
  public static final StringOption UNIT = new StringOption("unit", FTUnit.WORDS.toString());

  /**
   * Constructor, specifying initial options.
   * @param opts options string
   * @throws BaseXException database exception
   */
  public FTDistanceOptions(final String opts) throws BaseXException {
    super(opts);
  }

  /**
   * Returns the specification.
   * @return spec
   * @throws QueryException query exception
   */
  public FTUnit unit() throws QueryException {
    final String unit = get(UNIT);
    for(final FTUnit u : FTUnit.values()) if(u.toString().equals(unit)) return u;
    throw INVALIDOPT.thrw(null, "Unit '" + unit + "' is not supported.");
  }
}
