package org.basex.query.func.crypto;

import org.basex.util.options.*;

/**
 * Options for decrypting data.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DecryptOptions extends Options {
  /** Return binary data. */
  public static final BooleanOption BINARY = new BooleanOption("binary", false);
}
