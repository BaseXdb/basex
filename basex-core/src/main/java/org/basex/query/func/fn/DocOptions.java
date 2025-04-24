package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
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
  public static final BooleanOption DTD_VALIDATION = CommonOptions.DTD_VALIDATION;
  /** Whether external entities are permitted or rejected. */
  public static final BooleanOption ALLOW_EXTERNAL_ENTITIES = CommonOptions.ALLOW_EXTERNAL_ENTITIES;
  /** Limit on the maximum number of entity references that may be expanded. */
  public static final NumberOption ENTITY_EXPANSION_LIMIT = CommonOptions.ENTITY_EXPANSION_LIMIT;
  /** Remove whitespace-only text nodes. */
  public static final BooleanOption STRIP_SPACE = CommonOptions.STRIP_SPACE;
  /** Flag for using XInclude. */
  public static final BooleanOption XINCLUDE = CommonOptions.XINCLUDE;
  /** XSD validation. */
  public static final StringOption XSD_VALIDATION = CommonOptions.XSD_VALIDATION;
  /** Flag for using XInclude. */
  public static final BooleanOption XSI_SCHEMA_LOCATION = CommonOptions.XSI_SCHEMA_LOCATION;
  /** Whether two calls with same URI and options return the same node. */
  public static final BooleanOption STABLE = new BooleanOption("stable", true);

  /** Custom option (see {@link MainOptions#INTPARSE}). */
  public static final BooleanOption INTPARSE = CommonOptions.INTPARSE;
  /** Custom option (see {@link MainOptions#DTD}). */
  public static final BooleanOption DTD = CommonOptions.DTD;
  /** Custom option (see {@link MainOptions#STRIPNS}). */
  public static final BooleanOption STRIPNS = CommonOptions.STRIPNS;

  /**
   * Checks if the specified options qualify for database access.
   * @param info input info
   * @throws QueryException query exception
   */
  public void checkDbAccess(final InputInfo info) throws QueryException {
    if(!toString().isEmpty()) throw NO_OPTIONS_WITH_DB_X.get(info, this);
  }
}