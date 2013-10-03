package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class JsonXMLConverter extends JsonConverter {
  /** Spec to use. */
  protected final Spec spec;
  /** Flag for interpreting character escape sequences. */
  protected final boolean unescape;

  /**
   * Constructor.
   * @param sp JSON spec to use
   * @param unesc unescape flag
   * @param ii input info
   */
  protected JsonXMLConverter(final Spec sp, final boolean unesc, final InputInfo ii) {
    super(ii);
    spec = sp;
    unescape = unesc;
  }

  @Override
  public abstract ANode convert(final String in) throws QueryException;
}
