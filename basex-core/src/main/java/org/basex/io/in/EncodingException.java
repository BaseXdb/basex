package org.basex.io.in;

import org.basex.util.*;

/**
 * This class indicates exceptions during the decoding of the input stream.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class EncodingException extends InputException {
  /**
   * Constructor.
   * @param cp code points
   */
  EncodingException(final int cp) {
    super("Invalid XML character (#" + cp + ')');
  }

  /**
   * Constructor.
   * @param ex exception
   */
  EncodingException(final Exception ex) {
    super("Unsupported encoding: " + Util.message(ex));
  }
}
