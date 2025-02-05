package org.basex.query.func.fn;

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
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parseXml(qc, false, toOptions(arg(1), new ParseXmlOptions(), qc));
  }

  /**
   * Options for fn:parse-xml.
   */
  public static final class ParseXmlOptions extends ParseXmlFragmentOptions {
    /** DTD validation. */
    public static final BooleanOption DTD_VALIDATION = new BooleanOption("dtd-validation");
    /** XSD validation. */
    public static final StringOption XSD_VALIDATION = new StringOption("xsd-validation", "skip");
  }
}
