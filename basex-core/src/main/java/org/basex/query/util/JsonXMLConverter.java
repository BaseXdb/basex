package org.basex.query.util;

import org.basex.build.file.*;
import org.basex.build.file.JsonProp.Spec;
import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class JsonXMLConverter extends JsonConverter {
  /** JSON properties. */
  protected final JsonProp jprop;
  /** Spec to use. */
  protected final Spec spec;
  /** Flag for interpreting character escape sequences. */
  protected final boolean unescape;

  /**
   * Constructor.
   * @param jp json properties
   * @param ii input info
   */
  protected JsonXMLConverter(final JsonProp jp, final InputInfo ii) {
    super(ii);
    jprop = jp;
    spec = jp.spec();
    unescape = jp.is(JsonProp.UNESCAPE);
  }

  @Override
  public abstract ANode convert(final String in) throws QueryException;
}
