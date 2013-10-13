package org.basex.query.util;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.util.json.*;
import org.basex.query.value.node.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonXMLConverter extends JsonConverter {
  /**
   * Constructor.
   * @param opts json options
   */
  protected JsonXMLConverter(final JsonOptions opts) {
    super(opts);
  }

  @Override
  public abstract ANode convert(final String in) throws QueryIOException;
}
