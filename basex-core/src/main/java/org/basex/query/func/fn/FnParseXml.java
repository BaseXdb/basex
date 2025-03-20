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
    public static final BooleanOption DTD_VALIDATION = new BooleanOption("dtd-validation", false);
    /** XSD validation. */
    public static final StringOption XSD_VALIDATION = new StringOption("xsd-validation",
        MainOptions.SKIP);

    /** Custom option (see {@link MainOptions#INTPARSE}). */
    public static final BooleanOption INTPARSE = new BooleanOption("intparse", false);
    /** Custom option (see {@link MainOptions#DTD}). */
    public static final BooleanOption DTD = new BooleanOption("dtd", true);
    /** Custom option (see {@link MainOptions#XINCLUDE}). */
    public static final BooleanOption XINCLUDE = new BooleanOption("xinclude", false);
    /** Custom option (see {@link MainOptions#CATALOG}). */
    public static final StringOption CATALOG = new StringOption("catalog", "");
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ParseXmlOptions options = toOptions(arg(1), new ParseXmlOptions(), qc);
    return parseXml(qc, false, options);
  }
}
