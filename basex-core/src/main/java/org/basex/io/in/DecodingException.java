package org.basex.io.in;

/**
 * This class indicates exceptions that happen during the decoding of an input stream.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DecodingException extends InputException {
  /**
   * Constructor.
   * @param msg error message
   */
  DecodingException(final String msg) {
    super(msg);
  }

  /**
   * Constructor.
   * @param ex exception
   */
  DecodingException(final Exception ex) {
    super("Unsupported encoding: " + ex.getMessage());
  }
}
