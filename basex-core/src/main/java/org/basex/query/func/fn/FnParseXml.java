package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

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
    /** Whether any xi:include elements in the input are to be processed. */
    public static final BooleanOption XINCLUDE = CommonOptions.XINCLUDE;
    /** XSD validation. */
    public static final StringOption XSD_VALIDATION = CommonOptions.XSD_VALIDATION;
    /** Flag for using xsi:schemaLocation. */
    public static final BooleanOption USE_XSI_SCHEMA_LOCATION =
        CommonOptions.USE_XSI_SCHEMA_LOCATION;
    /** Whether external resources may be fetched. */
    public static final BooleanOption TRUSTED = CommonOptions.TRUSTED;

    /** Custom option (see {@link MainOptions#INTPARSE}). */
    public static final BooleanOption INTPARSE = CommonOptions.INTPARSE;
    /** Custom option (see {@link MainOptions#DTD}). */
    public static final BooleanOption DTD = CommonOptions.DTD;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ParseXmlOptions options = toOptions(arg(1), new ParseXmlOptions(), qc);
    final Boolean trustedOpt = options.get(ParseXmlOptions.TRUSTED);
    final boolean trusted = trustedOpt != null ? trustedOpt :
        qc.context.options.get(MainOptions.FNXMLTRUSTED);
    if(!trusted) {
      if(options.get(ParseXmlOptions.XINCLUDE)) throw EXTERNALRESOURCE_X.get(info, "'xinclude'");
      if(options.get(ParseXmlOptions.USE_XSI_SCHEMA_LOCATION) &&
          !CommonOptions.SKIP.equals(options.get(ParseXmlOptions.XSD_VALIDATION)))
          throw EXTERNALRESOURCE_X.get(info, "'use-xsi-schema-location'");
    }
    return parse(qc, false, options);
  }
}
