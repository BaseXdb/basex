package org.basex.query.func.fn;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
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
    public static final BooleanOption DTD_VALIDATION =
        new BooleanOption(CommonOptions.DTD_VALIDATION, false);
    /** Whether any xi:include elements in the input are to be processed. */
    public static final BooleanOption XINCLUDE = new BooleanOption(CommonOptions.XINCLUDE, false);
    /** XSD validation. */
    public static final StringOption XSD_VALIDATION =
        new StringOption(CommonOptions.XSD_VALIDATION, CommonOptions.SKIP);
    /** Flag for using xsi:schemaLocation. */
    public static final BooleanOption USE_XSI_SCHEMA_LOCATION =
        new BooleanOption(CommonOptions.USE_XSI_SCHEMA_LOCATION, false);
    /** Whether secondary external resources may be fetched. */
    public static final BooleanOption TRUST_EXTERNAL =
        new BooleanOption(CommonOptions.TRUST_EXTERNAL);

    /** Custom option (see {@link MainOptions#INTPARSE}). */
    public static final BooleanOption INTPARSE = new BooleanOption(CommonOptions.INTPARSE, false);
    /** Custom option (see {@link MainOptions#DTD}). */
    public static final BooleanOption DTD = new BooleanOption(CommonOptions.DTD, true);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return parse(qc, false, toOptions(arg(1), new ParseXmlOptions(), qc));
  }
}
