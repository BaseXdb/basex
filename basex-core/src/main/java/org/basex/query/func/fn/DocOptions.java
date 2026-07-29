package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.core.CommonOptions.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options for fn:doc and fn:doc-available.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class DocOptions extends Options {
  /** DTD validation. */
  public static final BooleanOption DTD_VALIDATION =
      new BooleanOption(CommonOptions.DTD_VALIDATION, false);
  /** Remove whitespace-only text nodes. */
  public static final EnumOption<StripSpace> STRIP_SPACE =
      new EnumOption<>(CommonOptions.STRIP_SPACE, StripSpace.CONDITIONAL);
  /** Flag for using XInclude. */
  public static final BooleanOption XINCLUDE = new BooleanOption(CommonOptions.XINCLUDE, false);
  /** XSD validation. */
  public static final StringOption XSD_VALIDATION =
      new StringOption(CommonOptions.XSD_VALIDATION, CommonOptions.SKIP);
  /** Flag for using xsi:schemaLocation. */
  public static final BooleanOption USE_XSI_SCHEMA_LOCATION =
      new BooleanOption(CommonOptions.USE_XSI_SCHEMA_LOCATION, false);
  /** Whether external resources may be fetched. */
  public static final BooleanOption TRUSTED = new BooleanOption(CommonOptions.TRUSTED);
  /** Whether two calls with same URI and options return the same node. */
  public static final BooleanOption STABLE = new BooleanOption("stable", true);

  /** Custom option (see {@link MainOptions#INTPARSE}). */
  public static final BooleanOption INTPARSE = new BooleanOption(CommonOptions.INTPARSE, false);
  /** Custom option (see {@link MainOptions#DTD}). */
  public static final BooleanOption DTD = new BooleanOption(CommonOptions.DTD, true);
  /** Custom option (see {@link MainOptions#STRIPNS}). */
  public static final BooleanOption STRIPNS = new BooleanOption(CommonOptions.STRIPNS, false);

  /**
   * Checks if the specified options qualify for database access.
   * @param info input info
   * @throws QueryException query exception
   */
  public void checkDbAccess(final InputInfo info) throws QueryException {
    if(!toString().isEmpty()) throw NO_OPTIONS_WITH_DB_X.get(info, this);
  }
}