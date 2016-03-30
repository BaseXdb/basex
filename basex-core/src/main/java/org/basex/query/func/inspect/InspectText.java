package org.basex.query.func.inspect;

import static org.basex.util.Token.*;

/**
 * This class assembles text string and tokens required by the XQuery processor
 * implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface InspectText {
  /** Supported documentation tags. */
  byte[][] DOC_TAGS = tokens("description", "author", "version", "param",
      "return", "error", "deprecated", "see", "since");
  /** Documentation: description tag. */
  byte[] DOC_DESCRIPTION = token("description");
  /** Documentation: param tag. */
  byte[] DOC_PARAM = token("param");
  /** Documentation: return tag. */
  byte[] DOC_RETURN = token("return");
}
