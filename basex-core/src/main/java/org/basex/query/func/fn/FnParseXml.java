package org.basex.query.func.fn;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnParseXml extends FnParseXmlFragment {
  /** Function options. */
  public static final class ParseXmlOptions extends ParseXmlFragmentOptions {
    /** DTD validation. */
    public static final BooleanOption DTD_VALIDATION = CommonOptions.DTD_VALIDATION;
    /** Whether external entities are permitted. */
    public static final BooleanOption ALLOW_EXTERNAL_ENTITIES =
        CommonOptions.ALLOW_EXTERNAL_ENTITIES;
    /** Limit on the maximum number of entity references that may be expanded. */
    public static final NumberOption ENTITY_EXPANSION_LIMIT = CommonOptions.ENTITY_EXPANSION_LIMIT;
    /** Whether any xi:include elements in the input are to be processed. */
    public static final BooleanOption XINCLUDE = CommonOptions.XINCLUDE;
    /** XSD validation. */
    public static final StringOption XSD_VALIDATION = CommonOptions.XSD_VALIDATION;

    /** Custom option (see {@link MainOptions#INTPARSE}). */
    public static final BooleanOption INTPARSE = CommonOptions.INTPARSE;
    /** Custom option (see {@link MainOptions#DTD}). */
    public static final BooleanOption DTD = CommonOptions.DTD;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ParseXmlOptions options = toOptions(arg(1), new ParseXmlOptions(), qc);
    return parse(qc, false, options);
  }
}
